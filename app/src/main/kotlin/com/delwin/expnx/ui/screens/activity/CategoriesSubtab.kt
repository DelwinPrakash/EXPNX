package com.delwin.expnx.ui.screens.activity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.delwin.expnx.data.Category
import com.delwin.expnx.data.Expense
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.components.ExpenseItem
import com.delwin.expnx.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CategoriesSubtab(viewModel: AppViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val categoryBudgets by viewModel.categoryBudgets.collectAsState()

    var showCategoryDetail by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Insights Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TanAccent.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TanAccent.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Insights, contentDescription = "Insights", tint = TanAccent)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("AI Spending Insight", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                        Text("You've spent 20% less on Food this month. Keep it up!", style = MaterialTheme.typography.bodyMedium, color = MutedCream)
                    }
                }
            }
        }

        items(Category.values()) { category ->
            val categoryBudget = categoryBudgets.find { it.category == category }
            val budgetLimit = categoryBudget?.budgetAmount ?: 5000.0
            val spent = allExpenses.filter { it.category == category }.sumOf { it.amount }
            val categoryExpenses = allExpenses.filter { it.category == category }
            val transactionsCount = categoryExpenses.size
            val progress = (spent / budgetLimit).coerceIn(0.0, 1.0).toFloat()
            val isOverBudget = spent > budgetLimit

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        selectedCategory = category
                        showCategoryDetail = true
                    },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(NearBlack, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(category.icon, contentDescription = null, tint = BurntOrangeAccent)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(category.displayName, style = MaterialTheme.typography.titleMedium, color = CreamText)
                                Text("$transactionsCount transaction${if (transactionsCount == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall, color = MutedCream)
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${String.format("%.2f", spent)}", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                                if (isOverBudget) {
                                    Text("Over budget", style = MaterialTheme.typography.bodySmall, color = RedReveal)
                                } else {
                                    Text("of ₹${String.format("%.0f", budgetLimit)}", style = MaterialTheme.typography.bodySmall, color = MutedCream)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = "Details", tint = MutedCream)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (isOverBudget) RedReveal else TanAccent,
                        trackColor = NearBlack,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }

    if (showCategoryDetail && selectedCategory != null) {
        val selectedCategoryBudget = categoryBudgets.find { it.category == selectedCategory }
        val limit = selectedCategoryBudget?.budgetAmount ?: 5000.0
        val spentAmount = allExpenses.filter { it.category == selectedCategory }.sumOf { it.amount }
        
        CategoryDetailBottomSheet(
            category = selectedCategory!!,
            budgetLimit = limit,
            spent = spentAmount,
            allExpenses = allExpenses,
            onDismiss = { showCategoryDetail = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailBottomSheet(
    category: Category,
    budgetLimit: Double,
    spent: Double,
    allExpenses: List<Expense>,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val categoryExpenses = allExpenses
        .filter { it.category == category }
        .sortedByDescending { it.date }
    
    // Calculate daily spending over last 7 days
    val dailySpend = remember(allExpenses, category) {
        (0..6).map { daysAgo ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = cal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            
            allExpenses
                .filter { it.category == category && it.date in dayStart..dayEnd }
                .sumOf { it.amount }
        }.reversed()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MutedCream.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Fixed Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NearBlack, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = BurntOrangeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CreamText
                        )
                        Text(
                            text = "Category Insights",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCream
                        )
                    }
                }
                
                IconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = GlassSurface,
                        contentColor = CreamText
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = GlassBorder)

            // Scrollable Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Section 1: Trend Chart
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Spending Trend (Last 7 Days)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedCream
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NearBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val maxVal = (dailySpend.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
                                val points = dailySpend.mapIndexed { idx, valAmt ->
                                    val x = (size.width / 6f) * idx
                                    val y = size.height - (size.height * (valAmt / maxVal).toFloat())
                                    Offset(x, y)
                                }

                                // Draw spline (bezier path)
                                val path = Path()
                                if (points.isNotEmpty()) {
                                    path.moveTo(points[0].x, points[0].y)
                                    for (i in 1 until points.size) {
                                        val prev = points[i - 1]
                                        val curr = points[i]
                                        val ctrlX = (prev.x + curr.x) / 2
                                        path.cubicTo(
                                            ctrlX, prev.y,
                                            ctrlX, curr.y,
                                            curr.x, curr.y
                                        )
                                    }
                                    
                                    // Draw gradient background under path
                                    val fillPath = Path().apply {
                                        addPath(path)
                                        lineTo(points.last().x, size.height)
                                        lineTo(points.first().x, size.height)
                                        close()
                                    }
                                    
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                BurntOrangeAccent.copy(alpha = 0.25f),
                                                Color.Transparent
                                            )
                                        )
                                    )

                                    // Draw spline line stroke
                                    drawPath(
                                        path = path,
                                        color = BurntOrangeAccent,
                                        style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )

                                    // Draw glowing data points
                                    points.forEachIndexed { index, pt ->
                                        val amt = dailySpend[index]
                                        if (amt > 0) {
                                            // Outer glow ring
                                            drawCircle(
                                                color = BurntOrangeAccent.copy(alpha = 0.3f),
                                                radius = 8.dp.toPx(),
                                                center = pt
                                            )
                                            // Inner point
                                            drawCircle(
                                                color = CreamText,
                                                radius = 4.dp.toPx(),
                                                center = pt
                                            )
                                        }
                                    }
                                }
                            }

                            // Date labels under the chart
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .offset(y = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val formatter = SimpleDateFormat("E", Locale.getDefault())
                                (0..6).map { daysAgo ->
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                                    cal.time
                                }.reversed().forEach { date ->
                                    Text(
                                        text = formatter.format(date),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedCream,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Budget Status
                val progress = (spent / budgetLimit).coerceIn(0.0, 1.0).toFloat()
                val isOver = spent > budgetLimit
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Budget Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedCream
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GlassSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Spent", style = MaterialTheme.typography.labelMedium, color = MutedCream)
                                    Text("₹${String.format("%,.2f", spent)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CreamText)
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isOver) RedReveal.copy(alpha = 0.15f) else OliveAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isOver) "Over Budget" else "Within Budget",
                                        color = if (isOver) RedReveal else OliveAccent,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            // Remaining / Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Limit", style = MaterialTheme.typography.labelSmall, color = MutedCream)
                                    Text("₹${String.format("%,.0f", budgetLimit)}", style = MaterialTheme.typography.bodyMedium, color = CreamText, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isOver) "Exceeded by" else "Remaining", style = MaterialTheme.typography.labelSmall, color = MutedCream)
                                    Text(
                                        text = "₹${String.format("%,.2f", kotlin.math.abs(budgetLimit - spent))}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isOver) RedReveal else TanAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Progress Bar
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = if (isOver) RedReveal else TanAccent,
                                trackColor = NearBlack,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }
                }

                // Section 3: AI Recommendations
                val recommendation = remember(spent, budgetLimit, category) {
                    val pct = spent / budgetLimit
                    when {
                        pct > 1.0 -> "Recommendation: You've exceeded your spending budget in ${category.displayName}. We recommend freezing non-essential category spends for the rest of the month."
                        pct >= 0.8 -> "Alert: You've consumed ${(pct * 100).toInt()}% of your ${category.displayName} budget limit. Consider trimming down unnecessary costs here to prevent going over."
                        pct <= 0.3 -> "Outstanding: Your ${category.displayName} expenses are well-contained, using just ${(pct * 100).toInt()}% of your limit. Keep it up!"
                        else -> "Discipline: Your spending velocity for ${category.displayName} is healthy. Keep tracking daily payments to sustain this momentum."
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "AI Spending Insight",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedCream
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TanAccent.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TanAccent.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = "AI recommendations",
                                tint = TanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CreamText
                            )
                        }
                    }
                }

                // Section 4: Recent Transactions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedCream
                    )
                    
                    if (categoryExpenses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found in this category",
                                color = MutedCream,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            categoryExpenses.take(5).forEach { expense ->
                                ExpenseItem(expense = expense)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
