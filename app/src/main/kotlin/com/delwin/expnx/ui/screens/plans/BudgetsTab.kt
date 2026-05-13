package com.delwin.expnx.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@Composable
fun BudgetsTab(viewModel: AppViewModel) {
    val totalSpent by viewModel.totalSpentThisMonth.collectAsState()
    val budget by viewModel.budget.collectAsState()
    val actualBudget = budget ?: 50000.0 // Default placeholder if none set
    val progress = (totalSpent / actualBudget).coerceIn(0.0, 1.0).toFloat()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Monthly Budget Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Monthly Overview", color = MutedCream, style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "₹${String.format("%.0f", totalSpent)}",
                        color = CreamText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "/ ₹${String.format("%.0f", actualBudget)}",
                        color = MutedCream,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val progressColor = when {
                    progress < 0.5f -> OliveAccent
                    progress < 0.8f -> TanAccent
                    else -> RedReveal
                }

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = SurfaceDark
                )

                Text(
                    text = "${(progress * 100).toInt()}% Spent",
                    color = progressColor,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // AI Suggested Budgets
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x15D67B2A)), // Subtle burnt orange
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x30D67B2A))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = BurntOrangeAccent, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Smart Recommendation", color = CreamText, fontWeight = FontWeight.Bold)
                    Text(
                        "Based on your last 3 months, increasing your Grocery budget by ₹2,000 and decreasing Entertainment by ₹1,500 will improve savings.",
                        color = MutedCream,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Category Budgets (Placeholder Data)
        Text("Category Budgets", color = CreamText, style = MaterialTheme.typography.titleMedium)

        CategoryBudgetCard(Category.FOOD, 15000.0, 12500.0)
        CategoryBudgetCard(Category.TRANSPORT, 5000.0, 2000.0)
        CategoryBudgetCard(Category.SHOPPING, 10000.0, 9500.0)
        CategoryBudgetCard(Category.ENTERTAINMENT, 8000.0, 8500.0) // Over budget example
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun CategoryBudgetCard(category: Category, budget: Double, spent: Double) {
    val progress = (spent / budget).coerceIn(0.0, 1.0).toFloat()
    val isOverBudget = spent > budget
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(GlassSurface, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(category.icon, contentDescription = null, tint = CreamText)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.displayName, color = CreamText, fontWeight = FontWeight.Medium)
                    Text("₹${String.format("%.0f", budget)} Limit", color = MutedCream, style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${String.format("%.0f", spent)}", color = CreamText, fontWeight = FontWeight.Bold)
                    Text(
                        if (isOverBudget) "Over budget" else "${(progress * 100).toInt()}%",
                        color = if (isOverBudget) RedReveal else OliveAccent,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOverBudget) RedReveal else OliveAccent,
                trackColor = NearBlack
            )
            
            if (isOverBudget) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = RedReveal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("You've exceeded your budget for ${category.displayName.lowercase()}.", color = RedReveal, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
