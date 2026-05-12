package com.delwin.expnx.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: AppViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()

    var selectedMonth by remember { mutableStateOf<String?>(null) }
    var showMonthMenu by remember { mutableStateOf(false) }

    val availableMonths = remember(allExpenses) {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        allExpenses
            .sortedByDescending { it.date }
            .map { format.format(Date(it.date)) }
            .distinct()
    }

    val filteredExpenses = remember(allExpenses, selectedMonth) {
        if (selectedMonth == null) {
            allExpenses
        } else {
            val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            allExpenses.filter { format.format(Date(it.date)) == selectedMonth }
        }
    }
    
    // Calculate category totals
    val categoryTotals = remember(filteredExpenses) {
        Category.values().map { category ->
            category to filteredExpenses.filter { it.category == category }.sumOf { it.amount }
        }.filter { it.second > 0 }.sortedByDescending { it.second }
    }
    
    val maxTotal = remember(categoryTotals) {
        categoryTotals.maxOfOrNull { it.second } ?: 1.0
    }

    var animateBars by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateBars = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", color = CreamText, style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { showMonthMenu = !showMonthMenu }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Filter by Month", tint = CreamText)
                    }
                    DropdownMenu(
                        expanded = showMonthMenu,
                        onDismissRequest = { showMonthMenu = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Months", color = CreamText) },
                            onClick = {
                                selectedMonth = null
                                showMonthMenu = false
                            }
                        )
                        availableMonths.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month, color = CreamText) },
                                onClick = {
                                    selectedMonth = month
                                    showMonthMenu = false
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Spending Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = MutedCream,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            if (categoryTotals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data to display", color = MutedCream)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(categoryTotals) { (category, total) ->
                        val targetProgress = if (animateBars) (total / maxTotal).toFloat() else 0f
                        val progress by animateFloatAsState(
                            targetValue = targetProgress,
                            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                        )

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = OliveAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = CreamText
                                    )
                                }
                                Text(
                                    text = "₹${String.format("%.2f", total)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CreamText
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                            ) {
                                val progressBarWidth = size.width * progress
                                drawRoundRect(
                                    color = OliveDim.copy(alpha = 0.3f),
                                    size = size,
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                                drawRoundRect(
                                    brush = Brush.linearGradient(listOf(TanAccent, BurntOrangeAccent)),
                                    size = Size(width = progressBarWidth, height = size.height),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
