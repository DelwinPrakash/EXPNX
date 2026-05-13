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
                var showAddExpenseSheet by remember { mutableStateOf(false) }
                
                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
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
                                            Text(screen.title)
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
                            HomeScreen(viewModel)
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
                    }
                }

                if (showAddSheet) {
                    AddBottomSheet(
                        onDismiss = { showAddSheet = false },
                        onAddExpenseClick = {
                            showAddSheet = false
                            showAddExpenseSheet = true
                        }
                    )
                }

                if (showAddExpenseSheet) {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { showAddExpenseSheet = false },
                        sheetState = sheetState,
                        containerColor = SurfaceDark,
                        contentColor = CreamText
                    ) {
                        AddExpenseSheet(
                            onSave = { amount, category, description, date ->
                                viewModel.saveExpense(amount, category, description, date)
                                showAddExpenseSheet = false
                            },
                            onCancel = { showAddExpenseSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBottomSheet(
    onDismiss: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = CreamText,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Create New",
                style = MaterialTheme.typography.titleLarge,
                color = CreamText,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            BottomSheetItem(
                icon = Icons.Default.ArrowDownward,
                title = "Add Expense",
                iconColor = RedReveal,
                onClick = onAddExpenseClick
            )
            BottomSheetItem(
                icon = Icons.Default.ArrowUpward,
                title = "Add Income",
                iconColor = OliveAccent,
                onClick = onDismiss
            )
            BottomSheetItem(
                icon = Icons.Default.SwapHoriz,
                title = "Transfer",
                iconColor = TanAccent,
                onClick = onDismiss
            )
            BottomSheetItem(
                icon = Icons.Default.DocumentScanner,
                title = "Scan Receipt",
                iconColor = BurntOrangeAccent,
                onClick = onDismiss
            )
            BottomSheetItem(
                icon = Icons.Default.Mic,
                title = "Voice Entry",
                iconColor = MutedCream,
                onClick = onDismiss
            )
        }
    }
}

@Composable
fun BottomSheetItem(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(GlassSurface, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = CreamText)
        }
    }
}
