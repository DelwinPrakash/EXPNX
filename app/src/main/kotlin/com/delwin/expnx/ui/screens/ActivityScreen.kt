package com.delwin.expnx.ui.screens

import androidx.compose.animation.Crossfade
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
import com.delwin.expnx.ui.screens.activity.CategoriesSubtab
import com.delwin.expnx.ui.screens.activity.RecurringSubtab
import com.delwin.expnx.ui.screens.activity.TransactionsSubtab
import com.delwin.expnx.ui.theme.*

enum class ActivityTab(val title: String) {
    TRANSACTIONS("Transactions"),
    CATEGORIES("Categories"),
    RECURRING("Recurring")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(ActivityTab.TRANSACTIONS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity", color = CreamText, style = MaterialTheme.typography.titleLarge) },
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
            // Segmented Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActivityTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) BurntOrangeAccent else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSelected) NearBlack else MutedCream,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Tab Content
            Crossfade(
                targetState = selectedTab,
                modifier = Modifier.fillMaxSize(),
                label = "ActivityTabCrossfade"
            ) { tab ->
                when (tab) {
                    ActivityTab.TRANSACTIONS -> TransactionsSubtab(viewModel)
                    ActivityTab.CATEGORIES -> CategoriesSubtab(viewModel)
                    ActivityTab.RECURRING -> RecurringSubtab(viewModel)
                }
            }
        }
    }
}
