package com.delwin.expnx.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*
import com.delwin.expnx.data.Expense
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: AppViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val totalSpentThisMonth by viewModel.totalSpentThisMonth.collectAsState()
    val budget by viewModel.budget.collectAsState()
    var selectedTimeRange by remember { mutableStateOf(1) } // 0: Weekly, 1: Monthly, 2: Yearly
    var showRecommendationSheet by remember { mutableStateOf(false) }
    var selectedRecommendationId by remember { mutableStateOf<String?>(null) }

    val aiInsights by viewModel.aiInsights.collectAsState()

    val activeRecommendations = remember(aiInsights) {
        val list = aiInsights?.recommendations
        if (!list.isNullOrEmpty()) {
            list.map { geminiRec ->
                val icon = when (geminiRec.iconName) {
                    "Warning" -> Icons.Default.Warning
                    "Star" -> Icons.Default.Star
                    "Info" -> Icons.Default.Info
                    else -> Icons.Default.Info
                }
                val tint = when (geminiRec.iconName) {
                    "Warning" -> BurntOrangeAccent
                    "Star" -> TanAccent
                    "Info" -> OliveAccent
                    else -> OliveAccent
                }
                RecommendationDetail(
                    id = geminiRec.id,
                    title = geminiRec.title,
                    subtitle = geminiRec.subtitle,
                    message = geminiRec.message,
                    actionText = geminiRec.actionText,
                    icon = icon,
                    iconTint = tint,
                    summary = geminiRec.summary,
                    metrics = geminiRec.metrics.map { it.label to it.value },
                    tips = geminiRec.tips
                )
            }
        } else {
            aiRecommendationsList
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "EXPNX Intelligence", 
                        color = CreamText, 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack)
            )
        },
        containerColor = NearBlack
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Spending Trends
            item { SpendingTrendsSection(allExpenses, selectedTimeRange) { selectedTimeRange = it } }

            // AI Recommendations
            item {
                AIRecommendationsSection(activeRecommendations) { recId ->
                    selectedRecommendationId = recId
                    showRecommendationSheet = true
                }
            }

            // Predictions
            item {
                val expectedBalance = aiInsights?.expected_end_of_month_balance ?: "₹45,200"
                val forecasted = aiInsights?.forecasted_spending ?: "₹12,500"
                val upcoming = aiInsights?.upcoming_expense_prediction ?: "₹4,000"
                val riskAlert = aiInsights?.cash_flow_risk_alert ?: "Low Risk"
                PredictionsSection(
                    expectedEndOfMonthBalance = expectedBalance,
                    forecastedSpending = forecasted,
                    upcomingExpensePrediction = upcoming,
                    cashFlowRiskAlert = riskAlert
                )
            }

            // Financial Health
            item { FinancialHealthSection(totalSpentThisMonth, budget, allExpenses) }

            // Reports
            // item { ReportsSection() }
        }
    }

    if (showRecommendationSheet && selectedRecommendationId != null) {
        val selectedRec = activeRecommendations.find { it.id == selectedRecommendationId }
        if (selectedRec != null) {
            RecommendationBottomSheet(
                recommendation = selectedRec,
                onDismiss = { showRecommendationSheet = false }
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = CreamText,
        modifier = modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
    )
}

@Composable
fun SpendingTrendsSection(
    expenses: List<Expense>,
    selectedTimeRange: Int,
    onTimeRangeSelected: (Int) -> Unit
) {
    val aggregatedValues = remember(expenses, selectedTimeRange) {
        when (selectedTimeRange) {
            0 -> getWeeklySpending(expenses)
            2 -> getYearlySpending(expenses)
            else -> getMonthlySpending(expenses)
        }
    }
    
    val maxVal = remember(aggregatedValues) { aggregatedValues.maxOrNull() ?: 0.0 }
    val cleanMax = remember(maxVal) { roundUpToCleanMax(maxVal) }
    
    Column {
        SectionTitle("Spending Trends")
        
        // Time Range Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val ranges = listOf("Weekly", "Monthly", "Yearly")
            ranges.forEachIndexed { index, label ->
                val isSelected = selectedTimeRange == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) OliveDim else Color.Transparent)
                        .clickable { onTimeRangeSelected(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) CreamText else MutedCream,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Placeholder Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 16.dp)
                .background(SurfaceDark, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                val yLabels = remember(cleanMax) {
                    listOf(
                        formatAmount(cleanMax),
                        formatAmount(cleanMax * 0.75),
                        formatAmount(cleanMax * 0.5),
                        formatAmount(cleanMax * 0.25),
                        formatAmount(0.0)
                    )
                }
                
                // Y-Axis
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        yLabels.forEach { label ->
                            Text(
                                text = label,
                                color = MutedCream,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Chart & X-Axis
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val path = Path()
                        val steps = aggregatedValues.size - 1
                        val stepX = if (steps > 0) size.width / steps else size.width
                        
                        // Draw grid lines
                        val gridLinesCount = 4
                        for (i in 0..gridLinesCount) {
                            val y = size.height * (i.toFloat() / gridLinesCount)
                            drawLine(
                                color = MutedCream.copy(alpha = 0.08f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val points = aggregatedValues.mapIndexed { i, valAtI ->
                            val ratio = if (cleanMax > 0.0) (valAtI / cleanMax) else 0.0
                            val x = i * stepX
                            val y = size.height - (size.height * 0.1f) - (size.height * 0.8f * ratio.toFloat())
                            Offset(x, y)
                        }

                        if (points.isNotEmpty()) {
                            path.moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                val prevPt = points[i - 1]
                                val currPt = points[i]
                                val controlX = (prevPt.x + currPt.x) / 2
                                path.cubicTo(
                                    controlX, prevPt.y,
                                    controlX, currPt.y,
                                    currPt.x, currPt.y
                                )
                            }
                        }
                        
                        drawPath(
                            path = path,
                            color = TanAccent,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        
                        points.forEach { pt ->
                            drawCircle(color = BurntOrangeAccent, radius = 6.dp.toPx(), center = pt)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val xLabels = when (selectedTimeRange) {
                        0 -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        2 -> listOf("Jan", "", "Mar", "", "May", "", "Jul", "", "Sep", "", "Nov", "")
                        else -> listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        xLabels.forEach { label ->
                            Text(
                                text = label,
                                color = MutedCream,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIRecommendationsSection(
    recommendations: List<RecommendationDetail>,
    onActionClick: (String) -> Unit
) {
    Column {
        SectionTitle("AI Recommendations")
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(recommendations.size) { index ->
                val rec = recommendations[index]
                RecommendationCard(
                    icon = rec.icon,
                    iconTint = rec.iconTint,
                    message = rec.message,
                    actionText = rec.actionText,
                    onActionClick = { onActionClick(rec.id) }
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(
    icon: ImageVector,
    iconTint: Color,
    message: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = CreamText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .height(60.dp)
                    .padding(top = 2.dp, bottom = 10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = actionText,
                color = OliveAccent,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onActionClick() }
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun PredictionsSection(
    expectedEndOfMonthBalance: String,
    forecastedSpending: String,
    upcomingExpensePrediction: String,
    cashFlowRiskAlert: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Predictions", modifier = Modifier.padding(horizontal = 0.dp))
        
        val riskTint = when (cashFlowRiskAlert) {
            "Low Risk" -> OliveAccent
            "Medium Risk" -> TanAccent
            "High Risk" -> RedReveal
            else -> OliveAccent
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PredictionItem("Expected end-of-month balance", expectedEndOfMonthBalance, Icons.Default.AccountBalanceWallet)
            PredictionItem("Forecasted spending", forecastedSpending, Icons.Default.TrendingUp)
            PredictionItem("Upcoming expense prediction", upcomingExpensePrediction, Icons.Default.Event)
            PredictionItem("Cash-flow risk alerts", cashFlowRiskAlert, Icons.Default.VerifiedUser, tint = riskTint)
        }
    }
}

@Composable
fun PredictionItem(title: String, value: String, icon: ImageVector, tint: Color = MutedCream) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NearBlack, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = CreamText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            color = CreamText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.wrapContentWidth()
        )
    }
}

@Composable
fun FinancialHealthSection(
    totalSpentThisMonth: Double,
    budget: Double?,
    expenses: List<Expense>
) {
    val effectiveBudget = budget ?: 50000.0 // Default fallback budget
    
    val spentRatio = if (effectiveBudget > 0) (totalSpentThisMonth / effectiveBudget) else 0.0
    val score = ((1.0 - spentRatio) * 100).toInt().coerceIn(0, 100)
    
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val targetProgress = if (isVisible) (score / 100f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
    )
    
    val status = when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Fair"
        else -> "Needs Work"
    }
    
    val savingRatio = ((1.0 - spentRatio) * 100).toInt().coerceAtLeast(0)
    
    val currentMonthExpenses = expenses.filter { expense ->
        val calendar = java.util.Calendar.getInstance()
        val currentMonth = calendar.get(java.util.Calendar.MONTH)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        calendar.timeInMillis = expense.date
        calendar.get(java.util.Calendar.MONTH) == currentMonth && calendar.get(java.util.Calendar.YEAR) == currentYear
    }
    
    val billsSpent = currentMonthExpenses.filter { it.category == com.delwin.expnx.data.Category.BILLS }.sumOf { it.amount }
    val debtRatio = if (totalSpentThisMonth > 0) ((billsSpent / totalSpentThisMonth) * 100).toInt().coerceIn(0, 100) else 0
    
    val budgetDiscipline = when {
        score >= 70 -> "High"
        score >= 40 -> "Medium"
        else -> "Low"
    }
    
    val recurringBurden = when {
        debtRatio > 40 -> "High"
        debtRatio > 20 -> "Medium"
        else -> "Low"
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Financial Health", modifier = Modifier.padding(horizontal = 0.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(SurfaceDark, OliveDim.copy(alpha = 0.2f))), RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Outline for the remaining portion
                        drawArc(
                            color = NearBlack,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        // Inner track
                        drawArc(
                            color = SurfaceDark,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        // Progress
                        drawArc(
                            brush = Brush.linearGradient(listOf(OliveAccent, TanAccent)),
                            startAngle = 135f,
                            sweepAngle = 270f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${(animatedProgress * 100).toInt()}", color = CreamText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        val statusColor = when (status) {
                            "Excellent" -> OliveAccent
                            "Good" -> TanAccent
                            "Fair" -> BurntOrangeAccent
                            else -> RedReveal
                        }
                        Text(status, color = statusColor, style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    HealthComponent("Saving Ratio", "$savingRatio%", Icons.Default.Savings)
                    HealthComponent("Debt Ratio", "$debtRatio%", Icons.Default.MoneyOff)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    HealthComponent("Budget Discipline", budgetDiscipline, Icons.Default.TrackChanges)
                    HealthComponent("Recurring Burden", recurringBurden, Icons.Default.Loop)
                }
            }
        }
    }
}

@Composable
fun HealthComponent(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(120.dp)) {
        Icon(icon, contentDescription = null, tint = MutedCream, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = CreamText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, color = MutedCream, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ReportsSection() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Reports", modifier = Modifier.padding(horizontal = 0.dp))
        
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(Modifier.weight(1f).fillMaxHeight(), "Weekly\nReport", Icons.Default.InsertChart) {
                android.widget.Toast.makeText(context, "Generating Weekly Report...", android.widget.Toast.LENGTH_SHORT).show()
            }
            ReportCard(Modifier.weight(1f).fillMaxHeight(), "Monthly\nSummary", Icons.Default.PieChart) {
                android.widget.Toast.makeText(context, "Generating Monthly Summary...", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(Modifier.weight(1f).fillMaxHeight(), "AI-Generated\nPDF", Icons.Default.PictureAsPdf) {
                android.widget.Toast.makeText(context, "Creating AI-Generated PDF...", android.widget.Toast.LENGTH_SHORT).show()
            }
            ReportCard(Modifier.weight(1f).fillMaxHeight(), "Export\nCSV", Icons.Default.FileDownload) {
                android.widget.Toast.makeText(context, "Exporting to CSV...", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun ReportCard(modifier: Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(NearBlack, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = CreamText, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = CreamText,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// AI Recommendation Detail Structures & Premium Content
data class RecommendationDetail(
    val id: String,
    val title: String,
    val subtitle: String,
    val message: String,
    val actionText: String,
    val icon: ImageVector,
    val iconTint: Color,
    val summary: String,
    val metrics: List<Pair<String, String>>,
    val tips: List<String>
)

val aiRecommendationsList = listOf(
    RecommendationDetail(
        id = "gathering_data",
        title = "Gathering Financial Data",
        subtitle = "Initial Analysis Setup",
        message = "Our AI is preparing your first insights...",
        actionText = "Wait",
        icon = Icons.Default.Info,
        iconTint = OliveAccent,
        summary = "We are currently analyzing your initial expenses, budgets, and goals. Please continue logging your financial activities. Our AI engine will soon provide customized recommendations and spending alerts.",
        metrics = listOf(
            "Status" to "Processing",
            "Data Points" to "Gathering..."
        ),
        tips = listOf(
            "Log a few more transactions to help our AI understand your spending habits.",
            "Set up specific budgets for key categories."
        )
    ),
    RecommendationDetail(
        id = "savings_potential",
        title = "Future Savings Opportunities",
        subtitle = "Predictive Analytics",
        message = "Unlock tailored saving strategies soon",
        actionText = "Learn More",
        icon = Icons.Default.Star,
        iconTint = TanAccent,
        summary = "Based on your ongoing transaction history, EXPNX will identify recurring subscriptions, unnecessary expenditures, and provide actionable tips to help you save more money effectively.",
        metrics = listOf(
            "Potential Savings" to "Calculating...",
            "Efficiency" to "Pending"
        ),
        tips = listOf(
            "Keep your recurring bills updated in the Bills tab.",
            "Regularly check your category breakdowns."
        )
    ),
    RecommendationDetail(
        id = "spending_trends",
        title = "Spending Behavior Trends",
        subtitle = "Pattern Recognition",
        message = "Discover your spending habits over time",
        actionText = "Explore",
        icon = Icons.Default.Warning,
        iconTint = BurntOrangeAccent,
        summary = "Once sufficient data is available, this space will highlight anomalies like weekend spending spikes or over-budget categories, helping you stay aware of your financial behavior.",
        metrics = listOf(
            "Trend Status" to "Analyzing...",
            "Risk Level" to "Pending"
        ),
        tips = listOf(
            "Consistency is key—record all your daily purchases.",
            "Your personalized trend alerts will appear here once ready."
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationBottomSheet(
    recommendation: RecommendationDetail,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MutedCream.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NearBlack, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = recommendation.icon,
                            contentDescription = null,
                            tint = recommendation.iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recommendation.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CreamText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = recommendation.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCream,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GlassSurface)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CreamText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = GlassBorder)

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Description Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = recommendation.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CreamText,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Metrics Grid/Row
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Key Metrics",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedCream
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        recommendation.metrics.forEach { (label, value) ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                colors = CardDefaults.cardColors(containerColor = NearBlack),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedCream
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            value.startsWith("+") -> RedReveal
                                            value.startsWith("-") || value.contains("Save") -> OliveAccent
                                            else -> CreamText
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Tips/Action Steps
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Actionable Insights & Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedCream
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recommendation.tips.forEach { tip ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(GlassSurface, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(recommendation.iconTint, RoundedCornerShape(50))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CreamText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getWeeklySpending(expenses: List<Expense>): List<Double> {
    val weeklyValues = DoubleArray(7) { 0.0 }
    val now = Calendar.getInstance()
    val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
    val daysToMonday = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
    
    val startOfWeekCal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, daysToMonday)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfWeek = startOfWeekCal.timeInMillis
    val endOfWeek = startOfWeek + 7L * 24 * 60 * 60 * 1000 - 1

    for (expense in expenses) {
        if (expense.date in startOfWeek..endOfWeek) {
            val expCal = Calendar.getInstance().apply { timeInMillis = expense.date }
            val expDayOfWeek = expCal.get(Calendar.DAY_OF_WEEK)
            val index = if (expDayOfWeek == Calendar.SUNDAY) 6 else expDayOfWeek - 2
            if (index in 0..6) {
                weeklyValues[index] += expense.amount
            }
        }
    }
    return weeklyValues.toList()
}

private fun getMonthlySpending(expenses: List<Expense>): List<Double> {
    val monthlyValues = DoubleArray(4) { 0.0 }
    val startOfMonthCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfMonth = startOfMonthCal.timeInMillis
    val endOfMonthCal = Calendar.getInstance().apply {
        timeInMillis = startOfMonth
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val endOfMonth = endOfMonthCal.timeInMillis

    for (expense in expenses) {
        if (expense.date in startOfMonth..endOfMonth) {
            val expCal = Calendar.getInstance().apply { timeInMillis = expense.date }
            val dayOfMonth = expCal.get(Calendar.DAY_OF_MONTH)
            val index = when {
                dayOfMonth <= 7 -> 0
                dayOfMonth <= 14 -> 1
                dayOfMonth <= 21 -> 2
                else -> 3
            }
            monthlyValues[index] += expense.amount
        }
    }
    return monthlyValues.toList()
}

private fun getYearlySpending(expenses: List<Expense>): List<Double> {
    val yearlyValues = DoubleArray(12) { 0.0 }
    val startOfYearCal = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfYear = startOfYearCal.timeInMillis
    val endOfYearCal = Calendar.getInstance().apply {
        timeInMillis = startOfYear
        set(Calendar.MONTH, Calendar.DECEMBER)
        set(Calendar.DAY_OF_MONTH, 31)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val endOfYear = endOfYearCal.timeInMillis

    for (expense in expenses) {
        if (expense.date in startOfYear..endOfYear) {
            val expCal = Calendar.getInstance().apply { timeInMillis = expense.date }
            val month = expCal.get(Calendar.MONTH)
            if (month in 0..11) {
                yearlyValues[month] += expense.amount
            }
        }
    }
    return yearlyValues.toList()
}

private fun roundUpToCleanMax(value: Double): Double {
    if (value <= 0) return 1000.0
    val log = Math.log10(value)
    val power = Math.pow(10.0, Math.ceil(log) - 1.0)
    val ratio = value / power
    val roundedRatio = when {
        ratio <= 1.0 -> 1.0
        ratio <= 2.0 -> 2.0
        ratio <= 4.0 -> 4.0
        ratio <= 5.0 -> 5.0
        ratio <= 8.0 -> 8.0
        else -> 10.0
    }
    return roundedRatio * power
}

private fun formatAmount(value: Double): String {
    return when {
        value >= 100000.0 -> {
            val l = value / 100000.0
            val rounded = Math.round(l * 10.0) / 10.0
            val clean = if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
            "₹${clean}L"
        }
        value >= 1000.0 -> {
            val k = value / 1000.0
            val rounded = Math.round(k * 10.0) / 10.0
            val clean = if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
            "₹${clean}k"
        }
        else -> {
            "₹${value.toInt()}"
        }
    }
}

