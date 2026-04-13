package com.delwin.expnx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel, navController: NavController) {
    val totalSpent by viewModel.totalSpentThisMonth.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val budget = 5000.0 // Hardcoded for now, could be dynamic
    
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker", color = WarmCream) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OlivePrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = WarmTan,
                contentColor = DarkForest,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        containerColor = WarmCream
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = WarmCream),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OlivePrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.labelSmall,
                            color = OlivePrimary
                        )
                        Text(
                            text = "$${String.format("%.2f", totalSpent)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = BurntOrange
                        )
                        Text(
                            text = "of $${String.format("%.0f", budget)} spent",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkForest.copy(alpha = 0.7f)
                        )
                    }
                    
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = (totalSpent / budget).toFloat().coerceIn(0f, 1f),
                            modifier = Modifier.size(100.dp),
                            color = if (totalSpent > budget) Color.Red else OlivePrimary,
                            trackColor = OlivePrimary.copy(alpha = 0.1f),
                            strokeWidth = 10.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${((totalSpent / budget) * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkForest
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkForest,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(recentExpenses) { expense ->
                    ExpenseItem(expense)
                }
                
                if (recentExpenses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions yet",
                                color = DarkForest.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = WarmCream,
            contentColor = DarkForest
        ) {
            AddExpenseSheet(
                onSave = { amount, category, desc, date ->
                    viewModel.saveExpense(amount, category, desc, date)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }
}
