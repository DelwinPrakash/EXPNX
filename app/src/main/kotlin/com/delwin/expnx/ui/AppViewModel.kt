package com.delwin.expnx.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.delwin.expnx.ExpenseApplication
import com.delwin.expnx.data.Category
import com.delwin.expnx.data.Expense
import com.delwin.expnx.data.ExpenseRepository
import com.delwin.expnx.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import com.delwin.expnx.data.CategoryBudget
import com.delwin.expnx.ui.screens.plans.Bill
import com.delwin.expnx.ui.screens.plans.BillCategory
import com.delwin.expnx.ui.screens.plans.Goal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.delwin.expnx.ui.screens.NotificationItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import com.delwin.expnx.data.network.GeminiService
import com.delwin.expnx.data.network.SpendingInsightResponse
import com.delwin.expnx.data.InsightEntity

data class DashboardUiState(
    val totalSpent: Double = 0.0,
    val budget: Double = 5000.0, // Default budget
    val recentExpenses: List<Expense> = emptyList()
)

class AppViewModel(
    private val repository: ExpenseRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.FlowPreview::class)
            kotlinx.coroutines.flow.combine(
                repository.allExpenses,
                repository.allCategoryBudgets,
                repository.allGoals,
                repository.allBills
            ) { _, _, _, _ -> true }
                .debounce(2500)
                .collect {
                    fetchAiInsights(force = true)
                }
        }
    }

    private val geminiService = GeminiService()
    private val gson = com.google.gson.Gson()

    private val _isLoadingInsights = MutableStateFlow(false)
    val isLoadingInsights = _isLoadingInsights.asStateFlow()

    private val _insightError = MutableStateFlow<String?>(null)
    val insightError = _insightError.asStateFlow()

    val aiInsights: StateFlow<SpendingInsightResponse?> = repository.allAiInsights
        .map { entity ->
            if (entity != null) {
                try {
                    val mapType = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                    val categoryInsights: Map<String, String> = gson.fromJson(entity.categoryInsightsJson, mapType)
                    val recsType = object : com.google.gson.reflect.TypeToken<List<com.delwin.expnx.data.network.GeminiRecommendation>>() {}.type
                    val recommendations: List<com.delwin.expnx.data.network.GeminiRecommendation>? = if (entity.recommendationsJson != null) {
                        gson.fromJson(entity.recommendationsJson, recsType)
                    } else null

                    SpendingInsightResponse(
                        general_insight = entity.generalInsight,
                        category_insights = categoryInsights,
                        budget_recommendation = entity.budgetRecommendation,
                        goal_recommendation = entity.goalRecommendation,
                        expected_end_of_month_balance = entity.expectedEndOfMonthBalance,
                        forecasted_spending = entity.forecastedSpending,
                        upcoming_expense_prediction = entity.upcomingExpensePrediction,
                        cash_flow_risk_alert = entity.cashFlowRiskAlert,
                        recommendations = recommendations
                    )
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun fetchAiInsights(force: Boolean = false) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currentInsight = repository.allAiInsights.firstOrNull()
            if (!force && currentInsight != null) {
                return@launch
            }

            _isLoadingInsights.value = true
            _insightError.value = null
            try {
                val expenses = allExpenses.value
                val budgets = categoryBudgets.value
                val totalBudget = budget.value ?: 5000.0

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val start = calendar.timeInMillis

                val currentMonthExpenses = expenses.filter { it.date >= start }
                val totalSpent = currentMonthExpenses.sumOf { it.amount }

                val categorySpend = currentMonthExpenses
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }

                val dataBuilder = StringBuilder()
                dataBuilder.append("Total Monthly Budget: ₹$totalBudget\n")
                dataBuilder.append("Total Spent This Month: ₹$totalSpent\n\n")
                dataBuilder.append("Category Spending & Budgets:\n")

                for (category in Category.values()) {
                    val catSpent = categorySpend[category] ?: 0.0
                    val catBudget = budgets.find { it.category == category }?.budgetAmount ?: 5000.0
                    dataBuilder.append("- ${category.displayName}: Spent ₹$catSpent out of budget ₹$catBudget\n")
                }

                val goals = goalsList.value
                dataBuilder.append("\nActive Financial Goals:\n")
                if (goals.isEmpty()) {
                    dataBuilder.append("- No active goals set yet.\n")
                } else {
                    for (goal in goals) {
                        dataBuilder.append("- ${goal.title}: Target ₹${goal.targetAmount}, Saved ₹${goal.savedAmount}, Target Date: ${goal.predictedDate}, Recommended Monthly Save: ₹${goal.monthlySuggestion}\n")
                    }
                }

                val bills = billsList.value
                dataBuilder.append("\nUpcoming & Active Bills:\n")
                if (bills.isEmpty()) {
                    dataBuilder.append("- No bills recorded yet.\n")
                } else {
                    for (bill in bills) {
                        dataBuilder.append("- ${bill.title} (${bill.provider}): ₹${bill.amount}, Due Date: ${bill.dueDate}, Paid: ${bill.isPaid}\n")
                    }
                }

                val promptContent = dataBuilder.toString()
                val response = geminiService.getSpendingInsights(promptContent)

                val entity = InsightEntity(
                    generalInsight = response.general_insight,
                    categoryInsightsJson = gson.toJson(response.category_insights),
                    budgetRecommendation = response.budget_recommendation,
                    goalRecommendation = response.goal_recommendation,
                    expectedEndOfMonthBalance = response.expected_end_of_month_balance,
                    forecastedSpending = response.forecasted_spending,
                    upcomingExpensePrediction = response.upcoming_expense_prediction,
                    cashFlowRiskAlert = response.cash_flow_risk_alert,
                    recommendationsJson = gson.toJson(response.recommendations),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.insertInsight(entity)
            } catch (e: java.net.UnknownHostException) {
                _insightError.value = "OFFLINE"
            } catch (e: Exception) {
                _insightError.value = "ERROR"
            } finally {
                _isLoadingInsights.value = false
            }
        }
    }

    data class PendingSms(val id: String?, val amount: Double)

    private val _pendingSms = MutableStateFlow<PendingSms?>(null)
    val pendingSms = _pendingSms.asStateFlow()

    fun setPendingSms(id: String?, amount: Double?) {
        _pendingSms.value = if (amount != null) PendingSms(id, amount) else null
    }

    val notifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpentThisMonth: StateFlow<Double> = repository.getTotalSpentThisMonth()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val recentExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budget: StateFlow<Double?> = userPreferencesRepository.monthlyBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categoryBudgets: StateFlow<List<CategoryBudget>> = repository.allCategoryBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categorySpendThisMonth: StateFlow<Map<Category, Double>> = allExpenses
        .map { expenses ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val end = calendar.timeInMillis

            expenses.filter { it.date in start..end }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun saveExpense(amount: Double, category: Category, description: String, date: Long) {
        viewModelScope.launch {
            repository.insertExpense(Expense(amount = amount, category = category, description = description, date = date))
            
            _pendingSms.value?.id?.let { id ->
                repository.markNotificationAsAdded(id)
            }
            _pendingSms.value = null
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun saveBudget(amount: Double) {
        viewModelScope.launch {
            userPreferencesRepository.saveBudget(amount)
        }
    }

    fun clearBudget() {
        viewModelScope.launch {
            userPreferencesRepository.clearBudget()
        }
    }

    fun getExpensesByCategory(category: Category): StateFlow<List<Expense>> {
        return repository.getExpensesByCategory(category)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getCategorySpendThisMonth(category: Category): StateFlow<Double> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis

        return repository.getExpensesByDateRange(start, end)
            .map { list -> list.filter { it.category == category }.sumOf { it.amount } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    }

    fun addCategoryBudget(categoryBudget: CategoryBudget) {
        viewModelScope.launch {
            repository.insertCategoryBudget(categoryBudget)
        }
    }

    fun removeCategoryBudget(category: Category) {
        viewModelScope.launch {
            repository.deleteCategoryBudget(CategoryBudget(category = category, budgetAmount = 0.0))
        }
    }

    val billsList: StateFlow<List<Bill>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBill(bill: Bill) {
        viewModelScope.launch {
            repository.insertBill(bill)
        }
    }

    fun removeBill(billId: String) {
        viewModelScope.launch {
            repository.deleteBillById(billId)
        }
    }

    fun updateBill(updatedBill: Bill) {
        viewModelScope.launch {
            repository.updateBill(updatedBill)
        }
    }

    val goalsList: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun removeGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoalById(goalId)
        }
    }

    fun updateGoal(updatedGoal: Goal) {
        viewModelScope.launch {
            repository.updateGoal(updatedGoal)
        }
    }

    fun deleteNotification(notification: NotificationItem) {
        viewModelScope.launch {
            repository.deleteNotificationById(notification.id)
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ExpenseApplication)
                AppViewModel(
                    repository = application.container.expenseRepository,
                    userPreferencesRepository = application.container.userPreferencesRepository
                )
            }
        }
    }
}
