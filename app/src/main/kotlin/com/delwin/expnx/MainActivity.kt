package com.delwin.expnx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.delwin.expnx.ui.*
import com.delwin.expnx.ui.Screen
import com.delwin.expnx.ui.screens.*
import com.delwin.expnx.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EXPNXTheme {
                val navController = rememberNavController()
                val viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
                
                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        NavigationBar(
                            containerColor = OlivePrimary,
                            contentColor = WarmCream
                        ) {
                            bottomNavItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DarkForest,
                                        selectedTextColor = DarkForest,
                                        indicatorColor = WarmTan,
                                        unselectedIconColor = WarmCream,
                                        unselectedTextColor = WarmCream
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(viewModel, navController)
                        }
                        composable(Screen.Transactions.route) {
                            TransactionsScreen(viewModel)
                        }
                        composable(Screen.Categories.route) {
                            CategoriesScreen(viewModel)
                        }
                        composable(Screen.Statistics.route) {
                            StatisticsScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
