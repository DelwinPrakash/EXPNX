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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import com.delwin.expnx.ui.theme.*
import kotlinx.coroutines.launch

data class Goal(
    val title: String,
    val icon: ImageVector,
    val targetAmount: Double,
    val savedAmount: Double,
    val predictedDate: String,
    val monthlySuggestion: Double
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalsTab() {
    val scrollState = rememberScrollState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    var selectedGoalForMenu by remember { mutableStateOf<Goal?>(null) }
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<Goal?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val goalsList = remember {
        mutableStateListOf(
            Goal("Emergency Fund", Icons.Default.Savings, 100000.0, 65000.0, "Oct 2026", 5000.0),
            Goal("Japan Vacation", Icons.Default.FlightTakeoff, 250000.0, 50000.0, "May 2027", 15000.0),
            Goal("New Laptop", Icons.Default.LaptopMac, 120000.0, 100000.0, "Jul 2026", 10000.0),
            Goal("Car Downpayment", Icons.Default.DirectionsCar, 500000.0, 50000.0, "Dec 2027", 25000.0)
        )
    }

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
                .padding(16.dp)
                .blur(if (selectedGoalForMenu != null) 12.dp else 0.dp)
                .graphicsLayer {
                    alpha = if (selectedGoalForMenu != null) 0.4f else 1.0f
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Suggestion
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
                        "You can reach your 'Emergency Fund' goal 2 months earlier by reducing dining expenses by 10%.",
                        color = CreamText,
                        style = MaterialTheme.typography.bodyMedium
                    )
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

            goalsList.forEach { goal ->
                GoalCard(
                    title = goal.title,
                    icon = goal.icon,
                    targetAmount = goal.targetAmount,
                    savedAmount = goal.savedAmount,
                    predictedDate = goal.predictedDate,
                    monthlySuggestion = goal.monthlySuggestion,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .combinedClickable(
                            onClick = { /* normal click is ignored or customizable */ },
                            onLongClick = {
                                selectedGoalForMenu = goal
                            }
                        )
                )
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
                                    goalsList.add(
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

    if (selectedGoalForMenu != null) {
        val focusedGoal = selectedGoalForMenu!!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { selectedGoalForMenu = null },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clickable(enabled = false) {}, // prevent click-through
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // High fidelity scaled focused card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = 1.02f
                            scaleY = 1.02f
                        }
                ) {
                    GoalCard(
                        title = focusedGoal.title,
                        icon = focusedGoal.icon,
                        targetAmount = focusedGoal.targetAmount,
                        savedAmount = focusedGoal.savedAmount,
                        predictedDate = focusedGoal.predictedDate,
                        monthlySuggestion = focusedGoal.monthlySuggestion
                    )
                }

                // WhatsApp iOS style vertical floating menu card
                Card(
                    modifier = Modifier.width(220.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column {
                        DropdownMenuItem(
                            text = { Text("Edit Goal", color = CreamText, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = OliveAccent) },
                            onClick = {
                                goalToEdit = focusedGoal
                                showEditGoalDialog = true
                                selectedGoalForMenu = null
                            }
                        )
                        HorizontalDivider(color = GlassBorder)
                        DropdownMenuItem(
                            text = { Text("Delete Goal", color = RedReveal, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedReveal) },
                            onClick = {
                                scope.launch {
                                    val deletedGoal = focusedGoal
                                    val deletedIndex = goalsList.indexOf(focusedGoal)
                                    if (deletedIndex != -1) {
                                        goalsList.removeAt(deletedIndex)
                                        selectedGoalForMenu = null
                                        val result = snackbarHostState.showSnackbar(
                                            message = "'${deletedGoal.title}' deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            goalsList.add(deletedIndex, deletedGoal)
                                        }
                                    }
                                }
                            }
                        )
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
                                    val index = goalsList.indexOf(currentGoal)
                                    if (index != -1) {
                                        goalsList[index] = Goal(
                                            title = title,
                                            icon = selectedIcon,
                                            targetAmount = targetAmt,
                                            savedAmount = savedAmount.toDoubleOrNull() ?: 0.0,
                                            predictedDate = predictedDate.ifBlank { "TBD" },
                                            monthlySuggestion = monthlySuggestion.toDoubleOrNull() ?: 0.0
                                        )
                                    }
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
