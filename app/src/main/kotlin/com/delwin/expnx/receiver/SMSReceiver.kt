package com.delwin.expnx.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.delwin.expnx.MainActivity
import java.util.regex.Pattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.delwin.expnx.data.NotificationEntity

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (message in messages) {
                    val body = message.messageBody ?: continue
                    val amount = parseDebitAmount(body)
                    if (amount != null) {
                        showNotification(context, amount)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun parseDebitAmount(body: String): Double? {
        val lower = body.lowercase()
        val hasDebitWord = lower.contains("debit") || lower.contains("spent") || 
                           lower.contains("withdrawn") || lower.contains("txn") || 
                           lower.contains("transaction")
        if (!hasDebitWord) return null

        val pattern = Pattern.compile("(?:rs\\.?|inr|₹)\\s*([\\d,]+(?:\\.\\d{2})?)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "") ?: ""
            return amountStr.toDoubleOrNull()
        }
        return null
    }

    private suspend fun showNotification(context: Context, amount: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sms_expense_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SMS Expense Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts for bank transactions that can be added to expenses"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis().toInt()

        val database = com.delwin.expnx.data.ExpenseDatabase.getDatabase(context)
        val entity = NotificationEntity(
            id = notificationId.toString(),
            title = "Unsaved Expense Alert",
            description = "A transaction of ₹${String.format("%.2f", amount)} was detected. Tap to add it to your expenses.",
            timestamp = System.currentTimeMillis(),
            iconName = "Warning",
            isUnread = true,
            amount = amount
        )
        database.notificationDao().insertNotification(entity)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("amount", amount)
            putExtra("notification_id", notificationId.toString())
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val addIntent = Intent(context, AddExpenseReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
            putExtra("amount", amount)
        }
        val addPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setContentTitle("New Debit Alert")
            .setContentText("Spent ₹${String.format("%.2f", amount)}? Add it to your expenses.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_add,
                "Add to Expenses",
                addPendingIntent
            )
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
