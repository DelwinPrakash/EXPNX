package com.delwin.expnx.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.data.Expense
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*
import kotlinx.coroutines.launch
import com.delwin.expnx.ui.components.ExpenseItem

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
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceDark,
                    contentColor = CreamText,
                    actionColor = TanAccent,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Transactions", color = CreamText, style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { showFilterMenu = !showFilterMenu }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = CreamText)
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories", color = CreamText) },
                            onClick = {
                                selectedCategory = null
                                showFilterMenu = false
                            }
                        )
                        Category.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName, color = CreamText) },
                                onClick = {
                                    selectedCategory = category
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack)
            )
        },
        containerColor = NearBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
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
                        val isDismissing = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                        val backgroundColor by animateColorAsState(
                            targetValue = if (isDismissing) RedReveal else Color.Transparent,
                            animationSpec = tween(300)
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (isDismissing) 1.2f else 0.0f,
                            animationSpec = tween(300)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(backgroundColor)
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = CreamText,
                                modifier = Modifier.scale(iconScale)
                            )
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                    enableDismissFromStartToEnd = false
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
                            color = MutedCream
                        )
                    }
                }
            }
        }
    }
}
