package com.delwin.expnx.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Activity : Screen("activity", "Activity", Icons.Default.List)
    object Add : Screen("add", "Add", Icons.Default.AddCircle)
    object Plans : Screen("plans", "Plans", Icons.Default.DateRange)
    object Insights : Screen("insights", "Insights", Icons.Default.Insights)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Activity,
    Screen.Add,
    Screen.Plans,
    Screen.Insights
)
