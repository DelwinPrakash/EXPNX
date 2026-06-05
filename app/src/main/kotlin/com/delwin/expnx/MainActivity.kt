package com.delwin.expnx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
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

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EXPNXTheme {
                val navController = rememberNavController()
                val viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
                var showAddSheet by remember { mutableStateOf(false) }
                
                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        if (currentDestination?.route != Screen.Notifications.route) {
                            NavigationBar(
                                containerColor = SurfaceDark,
                                contentColor = CreamText,
                                tonalElevation = 8.dp
                            ) {
                                bottomNavItems.forEach { screen ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    
                                    val scale by animateFloatAsState(
                                        targetValue = if (selected) 1.2f else 1.0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )

                                    NavigationBarItem(
                                        icon = {
                                            Box(modifier = Modifier.scale(scale)) {
                                                Icon(screen.icon, contentDescription = null)
                                            }
                                        },
                                        label = {
                                            AnimatedVisibility(
                                                visible = selected,
                                                enter = fadeIn() + expandVertically(),
                                                exit = fadeOut() + shrinkVertically()
                                            ) {
                                                Text(
                                                    text = screen.title,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        },
                                        selected = selected,
                                        onClick = {
                                            if (screen.route == Screen.Add.route) {
                                                showAddSheet = true
                                            } else {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = NearBlack,
                                            selectedTextColor = OliveAccent,
                                            indicatorColor = OliveAccent,
                                            unselectedIconColor = MutedCream,
                                            unselectedTextColor = MutedCream
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(NearBlack),
                        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 8 } },
                        exitTransition = { fadeOut(animationSpec = tween(300)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutVertically(animationSpec = tween(300)) { it / 8 } }
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onSeeAllClick = {
                                    navController.navigate(Screen.Activity.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNotificationClick = {
                                    navController.navigate(Screen.Notifications.route)
                                }
                            )
                        }
                        composable(Screen.Activity.route) {
                            ActivityScreen(viewModel)
                        }
                        composable(Screen.Add.route) {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                        composable(Screen.Plans.route) {
                            PlansScreen(viewModel)
                        }
                        composable(Screen.Insights.route) {
                            InsightsScreen(viewModel)
                        }
                        composable(Screen.Notifications.route) {
                            NotificationsScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }

                if (showAddSheet) {
                    AddScreen(viewModel = viewModel, onDismiss = { showAddSheet = false })
                }
            }
        }
    }
}
