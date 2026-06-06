package com.delwin.expnx.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import com.delwin.expnx.ui.screens.plans.Bill
import com.delwin.expnx.ui.screens.plans.Goal
import com.delwin.expnx.ui.screens.NotificationItem

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val billDao: BillDao,
    private val goalDao: GoalDao,
    private val notificationDao: NotificationDao
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    val allCategoryBudgets: Flow<List<CategoryBudget>> = expenseDao.getAllCategoryBudgets()

    val allBills: Flow<List<Bill>> = billDao.getAllBills().map { list -> list.map { it.toBill() } }

    suspend fun getAllBillsOnce(): List<Bill> = billDao.getAllBillsOnce().map { it.toBill() }

    suspend fun insertBill(bill: Bill) = billDao.insertBill(bill.toEntity())

    suspend fun updateBill(bill: Bill) = billDao.updateBill(bill.toEntity())

    suspend fun deleteBill(bill: Bill) = billDao.deleteBill(bill.toEntity())

    suspend fun deleteBillById(billId: String) = billDao.deleteBillById(billId)

    suspend fun insertCategoryBudget(categoryBudget: CategoryBudget) =
        expenseDao.insertCategoryBudget(categoryBudget)

    suspend fun deleteCategoryBudget(categoryBudget: CategoryBudget) =
        expenseDao.deleteCategoryBudget(categoryBudget)

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

    fun getTotalSpentByCategory(startDate: Long, endDate: Long): Flow<Double?> {
        return expenseDao.getTotalSpentInRange(startDate, endDate)
    }

    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals().map { list -> list.map { it.toGoal() } }

    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal.toEntity())

    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal.toEntity())

    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal.toEntity())

    suspend fun deleteGoalById(goalId: String) = goalDao.deleteGoalById(goalId)

    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications().map { list ->
        list.map { it.toNotificationItem() }
    }

    suspend fun insertNotification(notification: NotificationItem, timestamp: Long, iconName: String) =
        notificationDao.insertNotification(notification.toEntity(timestamp, iconName))

    suspend fun deleteNotificationById(id: String) = notificationDao.deleteNotificationById(id)

    suspend fun markNotificationAsRead(id: String) = notificationDao.markAsRead(id)

    suspend fun markNotificationAsAdded(id: String) = notificationDao.markAsAdded(id)
}
