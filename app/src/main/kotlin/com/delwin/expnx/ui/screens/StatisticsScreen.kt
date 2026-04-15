package com.delwin.expnx.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: AppViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    
    // Calculate category totals
    val categoryTotals = remember(allExpenses) {
        Category.values().map { category ->
            category to allExpenses.filter { it.category == category }.sumOf { it.amount }
        }.filter { it.second > 0 }.sortedByDescending { it.second }
    }
    
    val maxTotal = remember(categoryTotals) {
        categoryTotals.maxOfOrNull { it.second } ?: 1.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", color = WarmCream) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OlivePrimary)
            )
        },
        containerColor = WarmCream
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Spending Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkForest,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (categoryTotals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No data to display", color = DarkForest.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categoryTotals) { (category, total) ->
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
                                        tint = OlivePrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = DarkForest
                                    )
                                }
                                Text(
                                    text = "$${String.format("%.2f", total)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BurntOrange
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                            ) {
                                val progressBarWidth = size.width * (total / maxTotal).toFloat()
                                drawRoundRect(
                                    color = OlivePrimary.copy(alpha = 0.1f),
                                    size = size,
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                                drawRoundRect(
                                    color = WarmTan,
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
