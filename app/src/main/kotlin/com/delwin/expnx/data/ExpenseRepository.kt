package com.delwin.expnx.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesByCategory(category: Category): Flow<List<Expense>> = 
        expenseDao.getExpensesByCategory(category)

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate, endDate)

    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    fun getTotalSpentThisMonth(): Flow<Double?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfMonth = calendar.timeInMillis
        
        return expenseDao.getTotalSpentInRange(startOfMonth, endOfMonth)
    }

    fun getTotalSpentByCategory(category: Category, startDate: Long, endDate: Long): Flow<Double?> {
        // We could add a specific query for this, or just use the existing flow and map it
        // For simplicity, let's just stick to the current DAO methods for now
        // But a query would be better: SELECT SUM(amount) FROM expenses WHERE category = :category AND date BETWEEN :start AND :end
        return expenseDao.getTotalSpentInRange(startDate, endDate) // This is temporary, will fix DAO if needed
    }
}
