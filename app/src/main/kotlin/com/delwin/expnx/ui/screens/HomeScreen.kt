package com.delwin.expnx.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delwin.expnx.data.Category
import com.delwin.expnx.data.Expense
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.components.SetBudgetDialog
import com.delwin.expnx.ui.components.ExpenseItem
import com.delwin.expnx.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel, onSeeAllClick: () -> Unit = {}) {
    val totalSpent by viewModel.totalSpentThisMonth.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val budget by viewModel.budget.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    
    var showSetBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NearBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                HeaderSection()
            }
            
            item {
                SummaryCard(budget = budget, totalSpent = totalSpent, onEditBudget = { showSetBudgetDialog = true })
            }
            
            item {
                AIInsightCard(totalSpent = totalSpent, budget = budget)
            }

            item {
                BudgetProgressSection(allExpenses = allExpenses)
            }
            
            item {
                UpcomingPaymentsSection()
            }
            
            item {
                RecentTransactionsHeader(onSeeAllClick)
            }
            
            items(recentExpenses.take(5)) { expense ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ExpenseItem(expense)
                }
            }
            
            if (recentExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions yet", color = MutedCream)
                    }
                }
            }
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

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (currentHour) {
            in 0..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            else -> "Good evening,"
        }
        
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineSmall,
            color = CreamText,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { /* Mock */ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = CreamText)
            }
            // IconButton(onClick = { /* Mock */ }) {
            //     Icon(Icons.Default.Search, contentDescription = "Search", tint = CreamText)
            // }
        }
    }
}

@Composable
fun SummaryCard(budget: Double?, totalSpent: Double, onEditBudget: () -> Unit){
    // Start progress at 0f and animate to actual value
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val targetProgress = if (isVisible && budget != null && budget > 0) {
        (totalSpent / budget).toFloat().coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
    )

    // Glassmorphism Summary Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        onClick = onEditBudget,
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
                        progress = { animatedProgress },
                        modifier = Modifier.size(100.dp),
                        color = if (budget != null && totalSpent > budget) RedReveal else OliveAccent,
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
}

@Composable
fun AIInsightCard(totalSpent: Double, budget: Double?) {
    val insightText: String
    val insightIcon: androidx.compose.ui.graphics.vector.ImageVector
    val insightColor: Color

    if (budget == null || budget == 0.0) {
        insightText = "Set a budget to get personalized AI insights."
        insightIcon = Icons.Default.Lightbulb
        insightColor = TanAccent
    } else {
        val percentage = (totalSpent / budget) * 100
        when {
            percentage >= 90 -> {
                insightText = "Overspending Alert: You've used ${percentage.roundToInt()}% of your budget. Consider cutting non-essentials."
                insightIcon = Icons.Default.Warning
                insightColor = RedReveal
            }
            percentage >= 75 -> {
                insightText = "Budget Warning: Approaching your limit. You have ₹${String.format("%,.0f", budget - totalSpent)} left."
                insightIcon = Icons.Default.Info
                insightColor = BurntOrangeAccent
            }
            else -> {
                insightText = "Smart Recommendation: You're on track! Keep up the good spending habits."
                insightIcon = Icons.Default.CheckCircle
                insightColor = OliveAccent
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = insightColor.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, insightColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(insightIcon, contentDescription = null, tint = insightColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = insightText,
                style = MaterialTheme.typography.bodySmall,
                color = CreamText,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun BudgetProgressSection(allExpenses: List<Expense>) {
    val categoryTotals = allExpenses
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(3)

    if (categoryTotals.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Top Categories", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            
            val maxSpend = categoryTotals.maxOfOrNull { it.second } ?: 1.0
            
            categoryTotals.forEach { (category, total) ->
                val progress = (total / maxSpend).toFloat().coerceIn(0f, 1f)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SurfaceDark, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(category.icon, contentDescription = category.displayName, tint = MutedCream, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category.displayName, style = MaterialTheme.typography.bodyMedium, color = CreamText)
                            Text("₹${String.format("%,.0f", total)}", style = MaterialTheme.typography.bodyMedium, color = CreamText)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = OliveAccent,
                            trackColor = SurfaceDark
                        )
                    }
                }
            }
        }
    }
}

data class PaymentMock(val name: String, val amount: Double, val daysLeft: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun UpcomingPaymentsSection() {
    val payments = listOf(
        PaymentMock("Netflix", 199.0, 2, Icons.Default.Movie),
        PaymentMock("Electricity", 1450.0, 5, Icons.Default.FlashOn)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            "Upcoming Payments", 
            style = MaterialTheme.typography.titleMedium, 
            color = CreamText, 
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(payments) { payment ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.width(140.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(NearBlack, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(payment.icon, contentDescription = null, tint = BurntOrangeAccent, modifier = Modifier.size(18.dp))
                            }
                            Text("In ${payment.daysLeft}d", style = MaterialTheme.typography.labelSmall, color = RedReveal)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(payment.name, style = MaterialTheme.typography.bodyMedium, color = CreamText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹${String.format("%,.0f", payment.amount)}", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsHeader(onSeeAllClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.SemiBold)
        Text("See All", style = MaterialTheme.typography.labelMedium, color = TanAccent, modifier = Modifier.clickable { onSeeAllClick() })
    }
}
