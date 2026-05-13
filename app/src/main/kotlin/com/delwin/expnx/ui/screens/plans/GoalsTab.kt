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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.ui.theme.*

@Composable
fun GoalsTab() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Suggestion
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x1594A853)), // Subtle Olive Accent
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3094A853))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = OliveAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "You can reach your 'Emergency Fund' goal 2 months earlier by reducing dining expenses by 10%.",
                    color = CreamText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Text("Active Goals", color = CreamText, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))

        GoalCard(
            title = "Emergency Fund",
            icon = Icons.Default.Savings,
            targetAmount = 100000.0,
            savedAmount = 65000.0,
            predictedDate = "Oct 2026",
            monthlySuggestion = 5000.0
        )

        GoalCard(
            title = "Japan Vacation",
            icon = Icons.Default.FlightTakeoff,
            targetAmount = 250000.0,
            savedAmount = 50000.0,
            predictedDate = "May 2027",
            monthlySuggestion = 15000.0
        )

        GoalCard(
            title = "New Laptop",
            icon = Icons.Default.LaptopMac,
            targetAmount = 120000.0,
            savedAmount = 100000.0,
            predictedDate = "Jul 2026",
            monthlySuggestion = 10000.0
        )

        GoalCard(
            title = "Car Downpayment",
            icon = Icons.Default.DirectionsCar,
            targetAmount = 500000.0,
            savedAmount = 50000.0,
            predictedDate = "Dec 2027",
            monthlySuggestion = 25000.0
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun GoalCard(
    title: String,
    icon: ImageVector,
    targetAmount: Double,
    savedAmount: Double,
    predictedDate: String,
    monthlySuggestion: Double
) {
    val progress = (savedAmount / targetAmount).coerceIn(0.0, 1.0).toFloat()

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
                        .size(48.dp)
                        .background(GlassSurface, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = CreamText)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = CreamText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Target: ₹${String.format("%.0f", targetAmount)}", color = MutedCream, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    color = OliveAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = OliveAccent,
                trackColor = NearBlack
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Saved", color = MutedCream, style = MaterialTheme.typography.labelSmall)
                    Text("₹${String.format("%.0f", savedAmount)}", color = CreamText, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Est. Completion", color = MutedCream, style = MaterialTheme.typography.labelSmall)
                    Text(predictedDate, color = CreamText, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = GlassBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MutedCream, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Suggested monthly contribution: ₹${String.format("%.0f", monthlySuggestion)}", color = MutedCream, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
