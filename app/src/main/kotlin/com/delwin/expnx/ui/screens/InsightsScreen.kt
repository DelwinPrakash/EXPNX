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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: AppViewModel) {
    var selectedTimeRange by remember { mutableStateOf(1) } // 0: Weekly, 1: Monthly, 2: Yearly

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
            item { AIRecommendationsSection() }

            // Predictions
            item { PredictionsSection() }

            // Financial Health
            item { FinancialHealthSection() }

            // Reports
            item { ReportsSection() }
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
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .background(SurfaceDark, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                val steps = 6
                val stepX = size.width / steps
                
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
        }
    }
}

@Composable
fun AIRecommendationsSection() {
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
                    actionText = "Review"
                )
            }
            item {
                RecommendationCard(
                    icon = Icons.Default.Star,
                    iconTint = TanAccent,
                    message = "You may save ₹3,000 by reducing subscriptions",
                    actionText = "View Subs"
                )
            }
            item {
                RecommendationCard(
                    icon = Icons.Default.Info,
                    iconTint = OliveAccent,
                    message = "Weekend spending spikes detected",
                    actionText = "Analyze"
                )
            }
        }
    }
}

@Composable
fun RecommendationCard(icon: ImageVector, iconTint: Color, message: String, actionText: String) {
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
                fontWeight = FontWeight.Bold
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
