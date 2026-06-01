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
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: AppViewModel) {
    var selectedTimeRange by remember { mutableStateOf(1) } // 0: Weekly, 1: Monthly, 2: Yearly
    var showRecommendationSheet by remember { mutableStateOf(false) }
    var selectedRecommendationId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "AI Intelligence", 
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
            item { SpendingTrendsSection(selectedTimeRange) { selectedTimeRange = it } }

            // AI Recommendations
            item {
                AIRecommendationsSection { recId ->
                    selectedRecommendationId = recId
                    showRecommendationSheet = true
                }
            }

            // Predictions
            item { PredictionsSection() }

            // Financial Health
            item { FinancialHealthSection() }

            // Reports
            item { ReportsSection() }
        }
    }

    if (showRecommendationSheet && selectedRecommendationId != null) {
        val selectedRec = aiRecommendationsList.find { it.id == selectedRecommendationId }
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
fun SpendingTrendsSection(selectedTimeRange: Int, onTimeRangeSelected: (Int) -> Unit) {
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
                val yLabels = when (selectedTimeRange) {
                    0 -> listOf("₹10k", "₹7.5k", "₹5k", "₹2.5k", "₹0")
                    2 -> listOf("₹5L", "₹3.75L", "₹2.5L", "₹1.25L", "₹0")
                    else -> listOf("₹40k", "₹30k", "₹20k", "₹10k", "₹0")
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
                        val steps = 6
                        val stepX = size.width / steps
                        
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

                        val points = when (selectedTimeRange) {
                            0 -> listOf( // Weekly - Spike at weekends
                                Offset(0f, size.height * 0.7f),
                                Offset(stepX * 1.5f, size.height * 0.75f),
                                Offset(stepX * 3.5f, size.height * 0.3f), // Weekend spike
                                Offset(stepX * 5f, size.height * 0.8f),
                                Offset(size.width, size.height * 0.6f)
                            )
                            2 -> listOf( // Yearly - Holiday season peak
                                Offset(0f, size.height * 0.5f),
                                Offset(stepX * 2f, size.height * 0.6f),
                                Offset(stepX * 4f, size.height * 0.7f),
                                Offset(size.width, size.height * 0.2f)
                            )
                            else -> listOf( // Monthly (Default)
                                Offset(0f, size.height * 0.8f),
                                Offset(stepX * 2f, size.height * 0.5f),
                                Offset(stepX * 4f, size.height * 0.3f),
                                Offset(size.width, size.height * 0.2f)
                            )
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
                        2 -> listOf("Jan", "Mar", "May", "Jul", "Sep", "Nov")
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
fun AIRecommendationsSection(onActionClick: (String) -> Unit) {
    Column {
        SectionTitle("AI Recommendations")
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RecommendationCard(
                    icon = Icons.Default.Warning,
                    iconTint = BurntOrangeAccent,
                    message = "You spent 22% more on food this month",
                    actionText = "Review",
                    onActionClick = { onActionClick("food") }
                )
            }
            item {
                RecommendationCard(
                    icon = Icons.Default.Star,
                    iconTint = TanAccent,
                    message = "You may save ₹3,000 by reducing subscriptions",
                    actionText = "View Subs",
                    onActionClick = { onActionClick("subscriptions") }
                )
            }
            item {
                RecommendationCard(
                    icon = Icons.Default.Info,
                    iconTint = OliveAccent,
                    message = "Weekend spending spikes detected",
                    actionText = "Analyze",
                    onActionClick = { onActionClick("spikes") }
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
                modifier = Modifier.height(60.dp)
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
fun PredictionsSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Predictions", modifier = Modifier.padding(horizontal = 0.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PredictionItem("Expected end-of-month balance", "₹45,200", Icons.Default.AccountBalanceWallet)
            PredictionItem("Forecasted spending", "₹12,500", Icons.Default.TrendingUp)
            PredictionItem("Upcoming expense prediction", "₹4,000", Icons.Default.Event)
            PredictionItem("Cash-flow risk alerts", "Low Risk", Icons.Default.VerifiedUser, tint = OliveAccent)
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
fun FinancialHealthSection() {
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
                            sweepAngle = 270f * 0.85f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("85", color = CreamText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("Excellent", color = OliveAccent, style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    HealthComponent("Saving Ratio", "35%", Icons.Default.Savings)
                    HealthComponent("Debt Ratio", "12%", Icons.Default.MoneyOff)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    HealthComponent("Budget Discipline", "High", Icons.Default.TrackChanges)
                    HealthComponent("Recurring Burden", "Low", Icons.Default.Loop)
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
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle("Reports", modifier = Modifier.padding(horizontal = 0.dp))
        
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(Modifier.weight(1f).fillMaxHeight(), "Weekly\nReport", Icons.Default.InsertChart)
            ReportCard(Modifier.weight(1f).fillMaxHeight(), "Monthly\nSummary", Icons.Default.PieChart)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(Modifier.weight(1f).fillMaxHeight(), "AI-Generated\nPDF", Icons.Default.PictureAsPdf)
            ReportCard(Modifier.weight(1f).fillMaxHeight(), "Export\nCSV", Icons.Default.FileDownload)
        }
    }
}

@Composable
fun ReportCard(modifier: Modifier, title: String, icon: ImageVector) {
    Box(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(12.dp))
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
    val icon: ImageVector,
    val iconTint: Color,
    val summary: String,
    val metrics: List<Pair<String, String>>,
    val tips: List<String>
)

val aiRecommendationsList = listOf(
    RecommendationDetail(
        id = "food",
        title = "Food Expense Analysis",
        subtitle = "Budget Alert & Recommendation",
        icon = Icons.Default.Warning,
        iconTint = BurntOrangeAccent,
        summary = "Your food category spending is currently tracking 22% higher than your average monthly spend at this point. The main drivers are dining out and online food delivery services on weekends.",
        metrics = listOf(
            "Current Spend" to "₹9,780",
            "Budget Limit" to "₹8,000",
            "Deviation" to "+₹1,780 (+22%)"
        ),
        tips = listOf(
            "Dining out spikes on Friday and Saturday nights contribute to 45% of the excess spend.",
            "Delivery fees and taxes added ₹1,200 to your total this month.",
            "💡 Tip: Try meal prepping for weekends or limit delivery orders to once a week."
        )
    ),
    RecommendationDetail(
        id = "subscriptions",
        title = "Subscription Audit",
        subtitle = "Potential Savings & Insights",
        icon = Icons.Default.Star,
        iconTint = TanAccent,
        summary = "Our AI detected multiple recurring subscriptions, some of which show little to no recent usage. Cancelling or pausing these can immediately improve your monthly savings rate.",
        metrics = listOf(
            "Total Subs" to "6 Active",
            "Monthly Spend" to "₹4,850",
            "Potential Save" to "₹3,000/mo"
        ),
        tips = listOf(
            "Unused Streaming Service: ₹999/mo (No activity detected in the last 45 days).",
            "Premium Fitness App: ₹1,500/mo (Hardly used this month).",
            "💡 Tip: Set up a calendar reminder to review and cancel free trials before they auto-renew."
        )
    ),
    RecommendationDetail(
        id = "spikes",
        title = "Weekend Spending Trend",
        subtitle = "Behavioral Spikes Detected",
        icon = Icons.Default.Info,
        iconTint = OliveAccent,
        summary = "Over the last 4 weekends, your transactions show a recurring spending spike of 130% compared to weekdays. This behavior pattern is highly concentrated in Shopping and Entertainment categories.",
        metrics = listOf(
            "Weekday Avg" to "₹850/day",
            "Weekend Avg" to "₹2,400/day",
            "Top Peak" to "Saturdays (4-8 PM)"
        ),
        tips = listOf(
            "Non-essential shopping is the primary driver of the weekend spike.",
            "Entertainment accounts for 30% of weekend expenditures.",
            "💡 Tip: Use a 'Weekend Wallet' with a set budget, or delay non-essential purchases by 24 hours."
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
