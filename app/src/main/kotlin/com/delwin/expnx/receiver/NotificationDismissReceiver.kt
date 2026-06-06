package com.delwin.expnx.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.delwin.expnx.ExpenseApplication
import com.delwin.expnx.data.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val id = intent.getStringExtra("id") ?: System.currentTimeMillis().toString()
                val amount = intent.getDoubleExtra("amount", 0.0)
                if (amount > 0.0) {
                    val database = com.delwin.expnx.data.ExpenseDatabase.getDatabase(context)
                    val notificationDao = database.notificationDao()
                    
                    val entity = NotificationEntity(
                        id = id,
                        title = "Unsaved Expense Alert",
                        description = "A transaction of ₹${String.format("%.2f", amount)} was detected. Tap to add it to your expenses.",
                        timestamp = System.currentTimeMillis(),
                        iconName = "Warning",
                        isUnread = true,
                        amount = amount
                    )
                    notificationDao.insertNotification(entity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
