package com.delwin.expnx.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.data.Expense
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: AppViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val filteredExpenses = if (selectedCategory == null) {
        allExpenses
    } else {
        allExpenses.filter { it.category == selectedCategory }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("All Transactions", color = WarmCream) },
                actions = {
                    IconButton(onClick = { showFilterMenu = !showFilterMenu }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = WarmCream)
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories", color = WarmCream) },
                            onClick = {
                                selectedCategory = null
                                showFilterMenu = false
                            }
                        )
                        Category.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName, color = WarmCream) },
                                onClick = {
                                    selectedCategory = category
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OlivePrimary)
            )
        },
        containerColor = WarmCream
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(filteredExpenses, key = { it.id }) { expense ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
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
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.Settled -> Color.Transparent
                                else -> Color.Red.copy(alpha = 0.5f)
                            }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    ExpenseItem(expense)
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
                            text = if (selectedCategory != null) "No transactions for this category" else "No transactions yet",
                            color = DarkForest.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
