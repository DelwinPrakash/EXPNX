package com.delwin.expnx.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Transactions : Screen("transactions", "Activity", Icons.Default.List)
    object Add : Screen("add", "Add", Icons.Default.AddCircle)
    object Categories : Screen("categories", "Plans", Icons.Default.DateRange)
    object Statistics : Screen("statistics", "Insights", Icons.Default.Insights)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Add,
    Screen.Categories,
    Screen.Statistics
)
