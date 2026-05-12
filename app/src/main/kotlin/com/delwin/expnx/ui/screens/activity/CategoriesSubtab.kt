package com.delwin.expnx.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@Composable
fun CategoriesSubtab(viewModel: AppViewModel) {
    val allExpenses by viewModel.allExpenses.collectAsState()

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
            val spent = allExpenses.filter { it.category == category }.sumOf { it.amount }
            
            // For mock budget progress, let's just make it a random percentage for demo purposes, 
            // or based on a fixed mock budget per category.
            val mockBudget = 5000.0
            val progress = (spent / mockBudget).coerceIn(0.0, 1.0).toFloat()
            val isOverBudget = spent > mockBudget

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Open Category Detail Screen with Trend Chart */ },
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
                                Text("8 transactions", style = MaterialTheme.typography.bodySmall, color = MutedCream)
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${String.format("%.2f", spent)}", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                                if (isOverBudget) {
                                    Text("Over budget", style = MaterialTheme.typography.bodySmall, color = RedReveal)
                                } else {
                                    Text("of ₹${String.format("%.0f", mockBudget)}", style = MaterialTheme.typography.bodySmall, color = MutedCream)
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
}
