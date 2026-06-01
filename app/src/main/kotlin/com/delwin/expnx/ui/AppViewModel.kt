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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class DashboardUiState(
    val totalSpent: Double = 0.0,
    val budget: Double = 5000.0, // Default budget
    val recentExpenses: List<Expense> = emptyList()
)

class AppViewModel(
    private val repository: ExpenseRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

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

    private val _billsList = MutableStateFlow<List<Bill>>(
        listOf(
            Bill(
                title = "Electricity Bill",
                provider = "BESCOM",
                icon = Icons.Default.Bolt,
                amount = 1250.0,
                dueDate = "Tomorrow",
                isPaid = false,
                autoPay = true,
                category = BillCategory.THIS_WEEK
            ),
            Bill(
                title = "Internet",
                provider = "JioFiber",
                icon = Icons.Default.Wifi,
                amount = 999.0,
                dueDate = "In 3 Days",
                isPaid = false,
                autoPay = true,
                category = BillCategory.THIS_WEEK
            ),
            Bill(
                title = "Car EMI",
                provider = "HDFC Bank",
                icon = Icons.Default.DirectionsCar,
                amount = 8500.0,
                dueDate = "May 25",
                isPaid = false,
                autoPay = false,
                category = BillCategory.OVERDUE
            ),
            Bill(
                title = "Gym Membership",
                provider = "Cult.fit",
                icon = Icons.Default.FitnessCenter,
                amount = 1700.0,
                dueDate = "May 28",
                isPaid = false,
                autoPay = false,
                category = BillCategory.OVERDUE
            ),
            Bill(
                title = "Netflix",
                provider = "Streaming",
                icon = Icons.Default.LiveTv,
                amount = 649.0,
                dueDate = "May 02",
                isPaid = true,
                autoPay = true,
                category = BillCategory.LATER_THIS_MONTH
            )
        )
    )
    val billsList: StateFlow<List<Bill>> = _billsList.asStateFlow()

    fun addBill(bill: Bill) {
        _billsList.value = _billsList.value + bill
    }

    fun removeBill(billId: String) {
        _billsList.value = _billsList.value.filterNot { it.id == billId }
    }

    fun updateBill(updatedBill: Bill) {
        _billsList.value = _billsList.value.map { if (it.id == updatedBill.id) updatedBill else it }
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
