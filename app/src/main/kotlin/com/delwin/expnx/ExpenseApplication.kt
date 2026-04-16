package com.delwin.expnx

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.delwin.expnx.data.ExpenseDatabase
import com.delwin.expnx.data.ExpenseRepository
import com.delwin.expnx.data.UserPreferencesRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

class ExpenseApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

interface AppContainer {
    val expenseRepository: ExpenseRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(ExpenseDatabase.getDatabase(context).expenseDao())
    }
    
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }
}
