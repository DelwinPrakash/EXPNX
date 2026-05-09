package com.delwin.expnx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: AppViewModel) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", color = CreamText, style = MaterialTheme.typography.titleLarge) },
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(Category.values()) { category ->
                val spent = filteredExpenses.filter { it.category == category }.sumOf { it.amount }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(GlassSurface, Color(0x05FFFFFF))))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = BurntOrangeAccent,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = CreamText
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "₹${String.format("%.2f", spent)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = BurntOrangeAccent,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = selectedMonth ?: "All time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCream
                            )
                        }
                    }
                }
            }
        }
    }
}
