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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.screens.plans.Bill
import com.delwin.expnx.ui.screens.plans.BillCategory
import com.delwin.expnx.ui.theme.*

@Composable
fun RecurringSubtab(viewModel: AppViewModel) {
    val billsList by viewModel.billsList.collectAsState()

    // Dynamic calculations
    val totalCommitments = billsList.sumOf { it.amount }
    val missedBills = billsList.filter { !it.isPaid && it.category == BillCategory.OVERDUE }
    val upcomingBills = billsList.filter { !it.isPaid && it.category != BillCategory.OVERDUE }
    val activeSubscriptions = billsList.filter { it.autoPay || it.isPaid }

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
                    Text(
                        text = "You have ₹${String.format("%,.0f", totalCommitments)} in fixed commitments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedCream
                    )
                }
            }
        }

        // Missed Payments
        if (missedBills.isNotEmpty()) {
            item {
                Column {
                    Text("Missed Payments", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    missedBills.forEachIndexed { index, bill ->
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
                                    Text(bill.title, style = MaterialTheme.typography.titleMedium, color = CreamText)
                                    Text("Due ${bill.dueDate}", style = MaterialTheme.typography.bodySmall, color = RedReveal)
                                }
                                Text("₹${String.format("%,.0f", bill.amount)}", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (index < missedBills.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Upcoming
        if (upcomingBills.isNotEmpty()) {
            item {
                Column {
                    Text("Upcoming (Next 7 Days)", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    upcomingBills.forEachIndexed { index, bill ->
                        RecurringItemMock(title = bill.title, subtitle = bill.dueDate, amount = bill.amount)
                        if (index < upcomingBills.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Detected Subscriptions
        if (activeSubscriptions.isNotEmpty()) {
            item {
                Column {
                    Text("Detected Subscriptions", style = MaterialTheme.typography.titleMedium, color = CreamText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    activeSubscriptions.forEachIndexed { index, bill ->
                        RecurringItemMock(title = bill.title, subtitle = if (bill.isPaid) "Paid" else "Active", amount = bill.amount)
                        if (index < activeSubscriptions.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
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
