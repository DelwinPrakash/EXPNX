package com.delwin.expnx.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.delwin.expnx.ExpenseApplication
import com.delwin.expnx.data.Category
import com.delwin.expnx.data.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddExpenseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationId = intent.getIntExtra("notification_id", -1)
                val amount = intent.getDoubleExtra("amount", 0.0)
                if (amount > 0.0) {
                    val app = context.applicationContext as ExpenseApplication
                    val repository = app.container.expenseRepository
                    
                    val expense = Expense(
                        amount = amount,
                        category = Category.OTHER,
                        description = "SMS Debit Alert",
                        date = System.currentTimeMillis()
                    )
                    repository.insertExpense(expense)
                    
                    if (notificationId != -1) {
                        repository.markNotificationAsAdded(notificationId.toString())
                    }
                }

                if (notificationId != -1) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notificationId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
