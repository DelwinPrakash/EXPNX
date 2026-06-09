package com.delwin.expnx.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.delwin.expnx.data.Category
import com.delwin.expnx.data.CategoryBudget
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsTab(viewModel: AppViewModel) {
    val totalSpent by viewModel.totalSpentThisMonth.collectAsState()
    val budget by viewModel.budget.collectAsState()
    val categoryBudgetsList by viewModel.categoryBudgets.collectAsState()
    val categorySpending by viewModel.categorySpendThisMonth.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()

    val actualBudget = budget ?: 0.0
    val progress = if (actualBudget > 0.0) {
        (totalSpent / actualBudget).coerceIn(0.0, 1.0).toFloat()
    } else 0f

    val scrollState = rememberScrollState()
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // Monthly Budget Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Monthly Overview", color = MutedCream, style = MaterialTheme.typography.labelLarge)

                if (actualBudget == 0.0) {
                    Text(
                        "No monthly budget set yet. Set one in Settings.",
                        color = MutedCream,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "₹${String.format("%.0f", totalSpent)}",
                            color = CreamText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "/ ₹${String.format("%.0f", actualBudget)}",
                            color = MutedCream,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val progressColor = when {
                        progress < 0.5f -> OliveAccent
                        progress < 0.8f -> TanAccent
                        else -> RedReveal
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = SurfaceDark
                    )

                    Text(
                        text = "${(progress * 100).toInt()}% Spent",
                        color = progressColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // AI Suggested Budgets card
        val budgetRecommendationText = aiInsights?.budget_recommendation ?: "Based on your spending behavior, optimizing your category budgets will help improve your savings velocity."
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x15D67B2A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x30D67B2A))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = BurntOrangeAccent, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Smart Recommendation", color = CreamText, fontWeight = FontWeight.Bold)
                    Text(
                        text = budgetRecommendationText,
                        color = MutedCream,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Category Budgets", color = CreamText, style = MaterialTheme.typography.titleMedium)
            IconButton(
                onClick = { showAddBudgetDialog = true },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = OliveAccent,
                    contentColor = NearBlack
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category Budget", modifier = Modifier.size(20.dp))
            }
        }

        if (categoryBudgetsList.isEmpty()) {
            Text(
                "No category budgets added yet.",
                color = MutedCream,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            categoryBudgetsList.forEach { categoryBudget ->
                key(categoryBudget.category) {
                    val spentFromDb = categorySpending[categoryBudget.category] ?: 0.0
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                scope.launch {
                                    viewModel.removeCategoryBudget(categoryBudget.category)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "${categoryBudget.category.displayName} budget deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addCategoryBudget(categoryBudget)
                                    }
                                }
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val isDeleting = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                            val backgroundColor by animateColorAsState(
                                targetValue = if (isDeleting) RedReveal else Color.Transparent,
                                animationSpec = tween(300)
                            )
                            val iconScale by animateFloatAsState(
                                targetValue = if (isDeleting) 1.2f else 0.0f,
                                animationSpec = tween(300)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(backgroundColor)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = CreamText,
                                    modifier = Modifier.scale(iconScale)
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true
                    ) {
                        CategoryBudgetCard(
                            category = categoryBudget.category,
                            budget = categoryBudget.budgetAmount,
                            spent = spentFromDb
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddBudgetDialog) {
        var selectedCategory by remember { mutableStateOf(Category.FOOD) }
        var budgetLimit by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = { showAddBudgetDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Text(
                        "Add Category Budget",
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Select Category", color = MutedCream, style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Category.values().forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) OliveAccent else GlassSurface)
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) NearBlack else CreamText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = cat.displayName,
                                            color = if (isSelected) NearBlack else CreamText,
                                            fontWeight = FontWeight.Medium,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = budgetLimit,
                            onValueChange = { budgetLimit = it },
                            label = { Text("Budget Limit (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    HorizontalDivider(color = GlassBorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddBudgetDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MutedCream)
                        ) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                val limit = budgetLimit.toDoubleOrNull()
                                if (limit != null && limit > 0.0) {
                                    viewModel.addCategoryBudget(
                                        CategoryBudget(
                                            category = selectedCategory,
                                            budgetAmount = limit
                                        )
                                    )
                                    showAddBudgetDialog = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = OliveAccent)
                        ) { Text("Add") }
                    }
                }
            }
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter)
    ) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = SurfaceDark,
            contentColor = CreamText,
            actionColor = TanAccent,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
}

@Composable
fun CategoryBudgetCard(category: Category, budget: Double, spent: Double) {
    val progress = (spent / budget).coerceIn(0.0, 1.0).toFloat()
    val isOverBudget = spent > budget

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(GlassSurface, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(category.icon, contentDescription = null, tint = CreamText)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.displayName, color = CreamText, fontWeight = FontWeight.Medium)
                    Text("₹${String.format("%.0f", budget)} Limit", color = MutedCream, style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${String.format("%.0f", spent)}", color = CreamText, fontWeight = FontWeight.Bold)
                    Text(
                        if (isOverBudget) "Over budget" else "${(progress * 100).toInt()}%",
                        color = if (isOverBudget) RedReveal else OliveAccent,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOverBudget) RedReveal else OliveAccent,
                trackColor = NearBlack
            )

            if (isOverBudget) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = RedReveal, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "You've exceeded your budget for ${category.displayName.lowercase()}.",
                        color = RedReveal,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}