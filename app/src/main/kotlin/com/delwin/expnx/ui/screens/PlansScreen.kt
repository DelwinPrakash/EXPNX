package com.delwin.expnx.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.screens.plans.BillsTab
import com.delwin.expnx.ui.screens.plans.BudgetsTab
import com.delwin.expnx.ui.screens.plans.GoalsTab
import com.delwin.expnx.ui.theme.*

enum class PlanTab {
    BUDGETS, GOALS, BILLS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(PlanTab.BUDGETS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plans", color = CreamText, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NearBlack)
            )
        },
        containerColor = NearBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Custom Segmented Control / TabRow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(SurfaceDark, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlanTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) OliveAccent else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) NearBlack else MutedCream,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Tab Content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                when (targetTab) {
                    PlanTab.BUDGETS -> BudgetsTab(viewModel)
                    PlanTab.GOALS -> GoalsTab()
                    PlanTab.BILLS -> BillsTab()
                }
            }
        }
    }
}
