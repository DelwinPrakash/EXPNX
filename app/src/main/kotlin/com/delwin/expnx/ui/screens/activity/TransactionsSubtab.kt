package com.delwin.expnx.ui.screens.activity

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.data.Expense
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.components.ExpenseItem
import com.delwin.expnx.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsSubtab(viewModel: AppViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Smart Filters State
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedDateRange by remember { mutableStateOf("All time") }
    var selectedAmountRange by remember { mutableStateOf("All amounts") }
    val filters = listOf("All", "Date", "Category", "Amount range")

    // Filtered Expenses
    val filteredExpenses = remember(allExpenses, selectedFilter, selectedCategory, selectedDateRange, selectedAmountRange) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val oneWeekMillis = 7 * oneDayMillis

        allExpenses.filter { expense ->
            val matchesCategory = when (selectedFilter) {
                "Category" -> selectedCategory == null || expense.category == selectedCategory
                else -> true
            }

            val matchesDate = when (selectedFilter) {
                "Date" -> {
                    when (selectedDateRange) {
                        "Today" -> {
                            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                            sdf.format(Date(expense.date)) == sdf.format(Date(now))
                        }
                        "This Week" -> {
                            expense.date >= now - oneWeekMillis
                        }
                        "This Month" -> {
                            val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
                            sdf.format(Date(expense.date)) == sdf.format(Date(now))
                        }
                        else -> true
                    }
                }
                else -> true
            }

            val matchesAmount = when (selectedFilter) {
                "Amount range" -> {
                    when (selectedAmountRange) {
                        "Under ₹500" -> expense.amount < 500.0
                        "₹500 - ₹2,000" -> expense.amount in 500.0..2000.0
                        "Above ₹2,000" -> expense.amount > 2000.0
                        else -> true
                    }
                }
                else -> true
            }

            matchesCategory && matchesDate && matchesAmount
        }.sortedByDescending { it.date }
    }

    // Grouping by Month
    val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val groupedExpenses = filteredExpenses.groupBy { format.format(Date(it.date)) }

    // Edit Bottom Sheet State
    var showEditSheet by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Smart Filters Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = filter
                            if (filter == "All") {
                                selectedCategory = null
                                selectedDateRange = "All time"
                                selectedAmountRange = "All amounts"
                            }
                        },
                        label = { Text(filter, color = if (selectedFilter == filter) NearBlack else MutedCream) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TanAccent,
                            containerColor = SurfaceDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = GlassBorder,
                            enabled = true,
                            selected = selectedFilter == filter
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Sub-Filters Row
            if (selectedFilter != "All") {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (selectedFilter) {
                        "Category" -> {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("All Categories", color = if (selectedCategory == null) NearBlack else MutedCream) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OliveAccent,
                                        containerColor = SurfaceDark
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = GlassBorder,
                                        enabled = true,
                                        selected = selectedCategory == null
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                            items(Category.values()) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.displayName, color = if (selectedCategory == category) NearBlack else MutedCream) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OliveAccent,
                                        containerColor = SurfaceDark
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = GlassBorder,
                                        enabled = true,
                                        selected = selectedCategory == category
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                        "Date" -> {
                            val dateRanges = listOf("All time", "Today", "This Week", "This Month")
                            items(dateRanges) { range ->
                                FilterChip(
                                    selected = selectedDateRange == range,
                                    onClick = { selectedDateRange = range },
                                    label = { Text(range, color = if (selectedDateRange == range) NearBlack else MutedCream) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OliveAccent,
                                        containerColor = SurfaceDark
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = GlassBorder,
                                        enabled = true,
                                        selected = selectedDateRange == range
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                        "Amount range" -> {
                            val amountRanges = listOf("All amounts", "Under ₹500", "₹500 - ₹2,000", "Above ₹2,000")
                            items(amountRanges) { range ->
                                FilterChip(
                                    selected = selectedAmountRange == range,
                                    onClick = { selectedAmountRange = range },
                                    label = { Text(range, color = if (selectedAmountRange == range) NearBlack else MutedCream) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OliveAccent,
                                        containerColor = SurfaceDark
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = GlassBorder,
                                        enabled = true,
                                        selected = selectedAmountRange == range
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
            ) {
                groupedExpenses.forEach { (month, expensesForMonth) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NearBlack)
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.titleMedium,
                                color = CreamText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(expensesForMonth, key = { it.id }) { expense ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                when (it) {
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        // Delete Action
                                        scope.launch {
                                            viewModel.deleteExpense(expense)
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Expense deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.saveExpense(expense.amount, expense.category, expense.description, expense.date)
                                            }
                                        }
                                        true
                                    }
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        // Edit Action
                                        expenseToEdit = expense
                                        showEditSheet = true
                                        false // Don't dismiss the item, just show sheet
                                    }
                                    else -> false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val isDeleting = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                                val isEditing = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
                                
                                val backgroundColor by animateColorAsState(
                                    targetValue = when {
                                        isDeleting -> RedReveal
                                        isEditing -> TanAccent
                                        else -> Color.Transparent
                                    },
                                    animationSpec = tween(300)
                                )
                                
                                val iconScale by animateFloatAsState(
                                    targetValue = if (isDeleting || isEditing) 1.2f else 0.0f,
                                    animationSpec = tween(300)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(backgroundColor)
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = if (isDeleting) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Icon(
                                        imageVector = if (isDeleting) Icons.Default.Delete else Icons.Default.Edit,
                                        contentDescription = if (isDeleting) "Delete" else "Edit",
                                        tint = if (isDeleting) CreamText else NearBlack,
                                        modifier = Modifier.scale(iconScale)
                                    )
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp),
                            enableDismissFromStartToEnd = true, // Enable edit swipe
                            enableDismissFromEndToStart = true  // Enable delete swipe
                        ) {
                            ExpenseItem(expense)
                        }
                    }
                }
                
                if (filteredExpenses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found",
                                color = MutedCream
                            )
                        }
                    }
                }
            }
        }

        // Edit Bottom Sheet
        if (showEditSheet && expenseToEdit != null) {
            ModalBottomSheet(
                onDismissRequest = { showEditSheet = false },
                containerColor = SurfaceDark,
                scrimColor = Color.Black.copy(alpha = 0.5f)
            ) {
                EditExpenseContent(
                    expense = expenseToEdit!!,
                    onSave = { updatedExpense ->
                        // Here you would typically call a viewModel method to update. 
                        // For now we simulate an update via delete + save with new values.
                        scope.launch {
                            viewModel.deleteExpense(expenseToEdit!!)
                            viewModel.saveExpense(updatedExpense.amount, updatedExpense.category, updatedExpense.description, updatedExpense.date)
                        }
                        showEditSheet = false
                    },
                    onCancel = { showEditSheet = false }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = SurfaceDark,
                contentColor = CreamText,
                actionColor = TanAccent,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun EditExpenseContent(expense: Expense, onSave: (Expense) -> Unit, onCancel: () -> Unit) {
    // Basic edit fields (simplified for UI demonstration)
    var amountText by remember { mutableStateOf(expense.amount.toString()) }
    var descriptionText by remember { mutableStateOf(expense.description) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Edit Transaction", style = MaterialTheme.typography.titleLarge, color = CreamText)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount", color = MutedCream) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CreamText,
                unfocusedTextColor = CreamText,
                focusedBorderColor = TanAccent,
                unfocusedBorderColor = GlassBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = descriptionText,
            onValueChange = { descriptionText = it },
            label = { Text("Description", color = MutedCream) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CreamText,
                unfocusedTextColor = CreamText,
                focusedBorderColor = TanAccent,
                unfocusedBorderColor = GlassBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = MutedCream)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { 
                    val newAmount = amountText.toDoubleOrNull() ?: expense.amount
                    onSave(expense.copy(amount = newAmount, description = descriptionText))
                },
                colors = ButtonDefaults.buttonColors(containerColor = TanAccent)
            ) {
                Text("Save Changes", color = NearBlack)
            }
        }
    }
}
