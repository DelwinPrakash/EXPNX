package com.delwin.expnx.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delwin.expnx.data.Expense
import com.delwin.expnx.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseItem(expense: Expense, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = WarmCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OlivePrimary.copy(alpha = 0.2f)))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = expense.category.icon,
                    contentDescription = null,
                    tint = BurntOrange
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.category.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkForest
                )
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkForest.copy(alpha = 0.7f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = BurntOrange
                )
                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = OlivePrimary
                )
            }
        }
    }
}
