package com.delwin.expnx.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.components.SetBudgetDialog
import com.delwin.expnx.ui.components.ExpenseItem
import com.delwin.expnx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val totalSpent by viewModel.totalSpentThisMonth.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val budget by viewModel.budget.collectAsState()
    
    var showAddSheet by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }

    // Start progress at 0f and animate to actual value
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val targetProgress = if (isVisible && budget != null && budget!! > 0) {
        (totalSpent / budget!!).toFloat().coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overview", color = CreamText, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack)
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    // .shadow(16.dp, CircleShape, spotColor = TanAccent, ambientColor = TanAccent)
                    .background(
                        brush = Brush.radialGradient(listOf(TanAccent, BurntOrangeAccent)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { showAddSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = NearBlack, modifier = Modifier.size(32.dp))
                }
            }
        },
        containerColor = NearBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Glassmorphism Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(GlassSurface, Color(0x05FFFFFF))))
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Spent this month",
                                style = MaterialTheme.typography.labelSmall,
                                color = OliveAccent
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${String.format("%.2f", totalSpent)}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CreamText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (budget != null) {
                                Text(
                                    text = "from ₹${String.format("%.0f", budget)} budget",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedCream
                                )
                            } else {
                                Text(
                                    text = "No budget set",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedCream
                                )
                            }
                            TextButton(
                                onClick = { showSetBudgetDialog = true },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = if (budget != null) "Edit Budget" else "Set Budget",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TanAccent
                                )
                            }
                        }
                        
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = animatedProgress,
                                modifier = Modifier.size(100.dp),
                                color = if (budget != null && totalSpent > budget!!) RedReveal else OliveAccent,
                                trackColor = OliveDim.copy(alpha = 0.3f),
                                strokeWidth = 8.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = CreamText
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                color = MutedCream,
                modifier = Modifier.padding(bottom = 16.dp)
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
                                color = MutedCream
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
            containerColor = SurfaceDark,
            contentColor = CreamText
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
    
    if (showSetBudgetDialog) {
        SetBudgetDialog(
            initialBudget = budget,
            onSave = { 
                viewModel.saveBudget(it)
                showSetBudgetDialog = false
            },
            onClear = {
                viewModel.clearBudget()
                showSetBudgetDialog = false
            },
            onDismiss = { showSetBudgetDialog = false }
        )
    }
}
