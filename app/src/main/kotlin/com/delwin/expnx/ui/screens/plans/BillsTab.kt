package com.delwin.expnx.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delwin.expnx.ui.theme.*

@Composable
fun BillsTab() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "Upcoming",
                amount = "₹12,450",
                icon = Icons.Default.Event,
                color = OliveAccent,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Overdue",
                amount = "₹0",
                icon = Icons.Default.Warning,
                color = RedReveal,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("This Week", color = CreamText, style = MaterialTheme.typography.titleMedium)

        BillCard(
            title = "Electricity Bill",
            provider = "BESCOM",
            icon = Icons.Default.Bolt,
            amount = 1250.0,
            dueDate = "Tomorrow",
            isPaid = false,
            autoPay = true
        )

        BillCard(
            title = "Internet",
            provider = "JioFiber",
            icon = Icons.Default.Wifi,
            amount = 999.0,
            dueDate = "In 3 Days",
            isPaid = false,
            autoPay = true
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Later this Month", color = CreamText, style = MaterialTheme.typography.titleMedium)

        BillCard(
            title = "Car EMI",
            provider = "HDFC Bank",
            icon = Icons.Default.DirectionsCar,
            amount = 8500.0,
            dueDate = "May 25",
            isPaid = false,
            autoPay = false
        )

        BillCard(
            title = "Gym Membership",
            provider = "Cult.fit",
            icon = Icons.Default.FitnessCenter,
            amount = 1700.0,
            dueDate = "May 28",
            isPaid = false,
            autoPay = false
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Paid", color = MutedCream, style = MaterialTheme.typography.titleMedium)

        BillCard(
            title = "Netflix",
            provider = "Streaming",
            icon = Icons.Default.LiveTv,
            amount = 649.0,
            dueDate = "May 02",
            isPaid = true,
            autoPay = true
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = MutedCream, style = MaterialTheme.typography.labelMedium)
            }
            Text(amount, color = CreamText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BillCard(
    title: String,
    provider: String,
    icon: ImageVector,
    amount: Double,
    dueDate: String,
    isPaid: Boolean,
    autoPay: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) Color.Transparent else SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isPaid) androidx.compose.foundation.BorderStroke(1.dp, GlassBorder) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isPaid) Color.Transparent else GlassSurface, 
                        RoundedCornerShape(12.dp)
                    )
                    .then(if (isPaid) Modifier.padding(1.dp) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (isPaid) MutedCream else CreamText)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    color = if (isPaid) MutedCream else CreamText, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(provider, color = MutedCream, style = MaterialTheme.typography.labelSmall)
                    if (autoPay) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0x1594A853), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("AutoPay", color = OliveAccent, fontSize = 10.sp)
                        }
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${String.format("%.0f", amount)}", 
                    color = if (isPaid) MutedCream else CreamText, 
                    fontWeight = FontWeight.Bold
                )
                if (isPaid) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OliveAccent, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paid", color = OliveAccent, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Text(
                        dueDate, 
                        color = if (dueDate == "Tomorrow" || dueDate == "Today") RedReveal else BurntOrangeAccent, 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
