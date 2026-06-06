package com.delwin.expnx.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Expense::class, CategoryBudget::class, BillEntity::class, GoalEntity::class, NotificationEntity::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun billDao(): BillDao
    abstract fun goalDao(): GoalDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var Instance: ExpenseDatabase? = null

        fun getDatabase(context: Context): ExpenseDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ExpenseDatabase::class.java, "expense_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
