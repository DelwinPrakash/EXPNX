package com.delwin.expnx.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import com.delwin.expnx.ui.AppViewModel
import com.delwin.expnx.ui.theme.*
import kotlinx.coroutines.launch

data class Goal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val icon: ImageVector,
    val targetAmount: Double,
    val savedAmount: Double,
    val predictedDate: String,
    val monthlySuggestion: Double
)

@Composable
fun GoalsTab(viewModel: AppViewModel) {
    val scrollState = rememberScrollState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    var showEditGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<Goal?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val goalsList by viewModel.goalsList.collectAsState()
    val aiInsights by viewModel.aiInsights.collectAsState()

    val availableIcons = listOf(
        Icons.Default.Savings,
        Icons.Default.FlightTakeoff,
        Icons.Default.LaptopMac,
        Icons.Default.DirectionsCar,
        Icons.Default.Home,
        Icons.Default.SportsEsports,
        Icons.Default.School,
        Icons.Default.CardGiftcard,
        Icons.Default.Favorite
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Suggestion
            if (goalsList.isNotEmpty()) {
                val goalRecommendationText = aiInsights?.goal_recommendation ?: run {
                    val goalTitle = goalsList.first().title
                    "You can reach your '$goalTitle' goal earlier by reducing non-essential category expenses."
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1594A853)), // Subtle Olive Accent
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3094A853))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = OliveAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = goalRecommendationText,
                            color = CreamText,
                            style = MaterialTheme.typography.bodyMedium
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
                Text("Active Goals", color = CreamText, style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = { showAddGoalDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = OliveAccent,
                        contentColor = NearBlack
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (goalsList.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(GlassSurface, RoundedCornerShape(24.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = OliveAccent.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Goals Set Yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = CreamText,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track your savings objectives by creating your first financial goal.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCream,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                    Button(
                        onClick = { showAddGoalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = OliveAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = NearBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Your First Goal",
                            color = NearBlack,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            goalsList.forEach { goal ->
                key(goal.id) {
                    SwipeableGoalRow(
                        onDelete = {
                            scope.launch {
                                viewModel.removeGoal(goal.id)
                                val result = snackbarHostState.showSnackbar(
                                    message = "'${goal.title}' deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.addGoal(goal)
                                }
                            }
                        },
                        onEdit = {
                            goalToEdit = goal
                            showEditGoalDialog = true
                        }
                    ) {
                        GoalCard(
                            title = goal.title,
                            icon = goal.icon,
                            targetAmount = goal.targetAmount,
                            savedAmount = goal.savedAmount,
                            predictedDate = goal.predictedDate,
                            monthlySuggestion = goal.monthlySuggestion
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetAmount by remember { mutableStateOf("") }
        var savedAmount by remember { mutableStateOf("") }
        var predictedDate by remember { mutableStateOf("") }
        var monthlySuggestion by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf(availableIcons[0]) }
        val iconScrollState = rememberScrollState()

        Dialog(
            onDismissRequest = { showAddGoalDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Fixed header
                    Text(
                        "Add New Goal",
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
                    )

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Select Icon", color = MutedCream, style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(iconScrollState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            availableIcons.forEach { icon ->
                                val isSelected = selectedIcon == icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) OliveAccent else GlassSurface)
                                        .clickable { selectedIcon = icon }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) NearBlack else CreamText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Goal Name", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = targetAmount,
                            onValueChange = { targetAmount = it },
                            label = { Text("Target Amount (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = savedAmount,
                            onValueChange = { savedAmount = it },
                            label = { Text("Already Saved (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = predictedDate,
                            onValueChange = { predictedDate = it },
                            label = { Text("Target Date (e.g. Oct 2026)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = monthlySuggestion,
                            onValueChange = { monthlySuggestion = it },
                            label = { Text("Suggested Monthly Contribution (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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

                    // Fixed buttons at bottom
                    HorizontalDivider(color = GlassBorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddGoalDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MutedCream)
                        ) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                if (title.isNotBlank() && targetAmount.toDoubleOrNull() != null) {
                                    viewModel.addGoal(
                                        Goal(
                                            title = title,
                                            icon = selectedIcon,
                                            targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                            savedAmount = savedAmount.toDoubleOrNull() ?: 0.0,
                                            predictedDate = predictedDate.ifBlank { "TBD" },
                                            monthlySuggestion = monthlySuggestion.toDoubleOrNull() ?: 0.0
                                        )
                                    )
                                    showAddGoalDialog = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = OliveAccent)
                        ) { Text("Add") }
                    }
                }
            }
        }
    }



    if (showEditGoalDialog && goalToEdit != null) {
        val currentGoal = goalToEdit!!
        var title by remember { mutableStateOf(currentGoal.title) }
        var targetAmount by remember { mutableStateOf(currentGoal.targetAmount.toString()) }
        var savedAmount by remember { mutableStateOf(currentGoal.savedAmount.toString()) }
        var predictedDate by remember { mutableStateOf(currentGoal.predictedDate) }
        var monthlySuggestion by remember { mutableStateOf(currentGoal.monthlySuggestion.toString()) }
        var selectedIcon by remember { mutableStateOf(currentGoal.icon) }
        val iconScrollState = rememberScrollState()

        Dialog(
            onDismissRequest = { showEditGoalDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Fixed header
                    Text(
                        "Edit Goal",
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
                    )

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Select Icon", color = MutedCream, style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(iconScrollState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            availableIcons.forEach { icon ->
                                val isSelected = selectedIcon == icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) OliveAccent else GlassSurface)
                                        .clickable { selectedIcon = icon }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) NearBlack else CreamText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Goal Name", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = targetAmount,
                            onValueChange = { targetAmount = it },
                            label = { Text("Target Amount (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = savedAmount,
                            onValueChange = { savedAmount = it },
                            label = { Text("Already Saved (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = predictedDate,
                            onValueChange = { predictedDate = it },
                            label = { Text("Target Date (e.g. Oct 2026)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = monthlySuggestion,
                            onValueChange = { monthlySuggestion = it },
                            label = { Text("Suggested Monthly Contribution (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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

                    // Fixed buttons at bottom
                    HorizontalDivider(color = GlassBorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showEditGoalDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MutedCream)
                        ) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                val targetAmt = targetAmount.toDoubleOrNull()
                                if (title.isNotBlank() && targetAmt != null) {
                                    viewModel.updateGoal(
                                        Goal(
                                            id = currentGoal.id,
                                            title = title,
                                            icon = selectedIcon,
                                            targetAmount = targetAmt,
                                            savedAmount = savedAmount.toDoubleOrNull() ?: 0.0,
                                            predictedDate = predictedDate.ifBlank { "TBD" },
                                            monthlySuggestion = monthlySuggestion.toDoubleOrNull() ?: 0.0
                                        )
                                    )
                                    showEditGoalDialog = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = OliveAccent)
                        ) { Text("Save") }
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
fun GoalCard(
    title: String,
    icon: ImageVector,
    targetAmount: Double,
    savedAmount: Double,
    predictedDate: String,
    monthlySuggestion: Double,
    modifier: Modifier = Modifier
) {
    val progress = (savedAmount / targetAmount).coerceIn(0.0, 1.0).toFloat()

    Card(
        modifier = modifier.fillMaxWidth(),
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
                        .size(48.dp)
                        .background(GlassSurface, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = CreamText)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = CreamText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Target: ₹${String.format("%.0f", targetAmount)}", color = MutedCream, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    color = OliveAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = OliveAccent,
                trackColor = NearBlack
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Saved", color = MutedCream, style = MaterialTheme.typography.labelSmall)
                    Text("₹${String.format("%.0f", savedAmount)}", color = CreamText, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Est. Completion", color = MutedCream, style = MaterialTheme.typography.labelSmall)
                    Text(predictedDate, color = CreamText, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = GlassBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MutedCream, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Suggested monthly contribution: ₹${String.format("%.0f", monthlySuggestion)}", color = MutedCream, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableGoalRow(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isDeleting = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val isEditing = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
            
            val backgroundColor by animateColorAsState(
                targetValue = when {
                    isDeleting -> RedReveal
                    isEditing -> TanAccent
                    else -> Color.Transparent
                },
                animationSpec = tween(300)
            )
            
            val iconScale by animateFloatAsState(
                targetValue = if (isDeleting || isEditing) 1.2f else 0.0f,
                animationSpec = tween(300)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (isDeleting) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Icon(
                    imageVector = if (isDeleting) Icons.Default.Delete else Icons.Default.Edit,
                    contentDescription = if (isDeleting) "Delete" else "Edit",
                    tint = if (isDeleting) CreamText else NearBlack,
                    modifier = Modifier.scale(iconScale)
                )
            }
        },
        modifier = Modifier.padding(vertical = 4.dp),
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        content = { content() }
    )
}

