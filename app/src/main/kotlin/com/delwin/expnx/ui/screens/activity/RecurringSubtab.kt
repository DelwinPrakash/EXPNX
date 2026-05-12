package com.delwin.expnx.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@Composable
fun RecurringSubtab(viewModel: AppViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Recurring Insights
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recurring Insights", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You have ₹14,500 in fixed monthly commitments. This is 30% of your average income.", style = MaterialTheme.typography.bodyMedium, color = MutedCream)
                }
            }
        }

        // Missed Payments
        item {
            Column {
                Text("Missed Payments", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RedReveal.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedReveal.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(RedReveal.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RedReveal)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gym Membership", style = MaterialTheme.typography.titleMedium, color = CreamText)
                            Text("Due 2 days ago", style = MaterialTheme.typography.bodySmall, color = RedReveal)
                        }
                        Text("₹1,200", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Upcoming
        item {
            Column {
                Text("Upcoming (Next 7 Days)", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                RecurringItemMock("Netflix Subscription", "Tomorrow", 199.0)
                Spacer(modifier = Modifier.height(8.dp))
                RecurringItemMock("Electricity Bill", "In 3 days", 2450.0)
            }
        }

        // Detected Subscriptions
        item {
            Column {
                Text("Detected Subscriptions", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                RecurringItemMock("Spotify Premium", "Active", 119.0)
                Spacer(modifier = Modifier.height(8.dp))
                RecurringItemMock("Amazon Prime", "Active", 1499.0)
            }
        }
    }
}

@Composable
fun RecurringItemMock(title: String, subtitle: String, amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NearBlack, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Autorenew, contentDescription = null, tint = MutedCream)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = CreamText)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedCream)
            }
            Text("₹${String.format("%.2f", amount)}", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
        }
    }
}
