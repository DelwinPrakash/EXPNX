package com.delwin.expnx

import android.app.Application
import com.delwin.expnx.data.ExpenseDatabase
import com.delwin.expnx.data.ExpenseRepository

class ExpenseApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

interface AppContainer {
    val expenseRepository: ExpenseRepository
}

class AppDataContainer(private val context: android.content.Context) : AppContainer {
    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(ExpenseDatabase.getDatabase(context).expenseDao())
    }
}
