package com.delwin.expnx.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.delwin.expnx.ui.theme.EXPNXTheme

sealed class Screen(val route: String) {
    object Home : Screen("home")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    EXPNXTheme {
        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(route = Screen.Home.route) {
                HomeScreen()
            }
        }
    }
}
