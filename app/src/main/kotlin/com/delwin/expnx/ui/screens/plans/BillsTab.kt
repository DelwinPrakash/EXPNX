package com.delwin.expnx.ui.screens.plans

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.delwin.expnx.ui.theme.*
import kotlinx.coroutines.launch

enum class BillCategory(val displayName: String) {
    THIS_WEEK("This Week"),
    LATER_THIS_MONTH("Later this Month")
}

data class Bill(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val provider: String,
    val icon: ImageVector,
    val amount: Double,
    val dueDate: String,
    val isPaid: Boolean,
    val autoPay: Boolean,
    val category: BillCategory = BillCategory.THIS_WEEK
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsTab() {
    val scrollState = rememberScrollState()
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showEditBillDialog by remember { mutableStateOf(false) }
    var billToEdit by remember { mutableStateOf<Bill?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val billsList = remember {
        mutableStateListOf(
            Bill(
                title = "Electricity Bill",
                provider = "BESCOM",
                icon = Icons.Default.Bolt,
                amount = 1250.0,
                dueDate = "Tomorrow",
                isPaid = false,
                autoPay = true,
                category = BillCategory.THIS_WEEK
            ),
            Bill(
                title = "Internet",
                provider = "JioFiber",
                icon = Icons.Default.Wifi,
                amount = 999.0,
                dueDate = "In 3 Days",
                isPaid = false,
                autoPay = true,
                category = BillCategory.THIS_WEEK
            ),
            Bill(
                title = "Car EMI",
                provider = "HDFC Bank",
                icon = Icons.Default.DirectionsCar,
                amount = 8500.0,
                dueDate = "May 25",
                isPaid = false,
                autoPay = false,
                category = BillCategory.LATER_THIS_MONTH
            ),
            Bill(
                title = "Gym Membership",
                provider = "Cult.fit",
                icon = Icons.Default.FitnessCenter,
                amount = 1700.0,
                dueDate = "May 28",
                isPaid = false,
                autoPay = false,
                category = BillCategory.LATER_THIS_MONTH
            ),
            Bill(
                title = "Netflix",
                provider = "Streaming",
                icon = Icons.Default.LiveTv,
                amount = 649.0,
                dueDate = "May 02",
                isPaid = true,
                autoPay = true,
                category = BillCategory.LATER_THIS_MONTH
            )
        )
    }

    val availableIcons = listOf(
        Icons.Default.Bolt,
        Icons.Default.Wifi,
        Icons.Default.DirectionsCar,
        Icons.Default.FitnessCenter,
        Icons.Default.LiveTv,
        Icons.Default.Receipt,
        Icons.Default.CreditCard,
        Icons.Default.Smartphone,
        Icons.Default.Home
    )

    // Dynamic summary calculations
    val upcomingAmount = billsList.filter { !it.isPaid }.sumOf { it.amount }
    val overdueAmount = billsList.filter { !it.isPaid && it.dueDate.equals("Overdue", ignoreCase = true) }.sumOf { it.amount }

    val onDeleteBill: (Bill) -> Unit = { bill ->
        scope.launch {
            val index = billsList.indexOfFirst { it.id == bill.id }
            if (index != -1) {
                val removedBill = billsList.removeAt(index)
                val result = snackbarHostState.showSnackbar(
                    message = "${bill.title} deleted",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    if (index <= billsList.size) {
                        billsList.add(index, removedBill)
                    } else {
                        billsList.add(removedBill)
                    }
                }
            }
        }
    }

    val onEditBill: (Bill) -> Unit = { bill ->
        billToEdit = bill
        showEditBillDialog = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "Upcoming",
                    amount = "₹${String.format("%.0f", upcomingAmount)}",
                    icon = Icons.Default.Event,
                    color = OliveAccent,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Overdue",
                    amount = "₹${String.format("%.0f", overdueAmount)}",
                    icon = Icons.Default.Warning,
                    color = RedReveal,
                    modifier = Modifier.weight(1f)
                )
            }

            // Section Title & Add Bill Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bills & Subscriptions", color = CreamText, style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = { showAddBillDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = OliveAccent,
                        contentColor = NearBlack
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Bill",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // This Week Section (Unpaid)
            val thisWeekBills = billsList.filter { it.category == BillCategory.THIS_WEEK && !it.isPaid }
            if (thisWeekBills.isNotEmpty()) {
                Text("This Week", color = CreamText, style = MaterialTheme.typography.titleSmall)
                thisWeekBills.forEach { bill ->
                    key(bill.id) {
                        SwipeableBillRow(
                            onDelete = { onDeleteBill(bill) },
                            onEdit = { onEditBill(bill) }
                        ) {
                            BillCard(
                                title = bill.title,
                                provider = bill.provider,
                                icon = bill.icon,
                                amount = bill.amount,
                                dueDate = bill.dueDate,
                                isPaid = bill.isPaid,
                                autoPay = bill.autoPay,
                                onClick = {
                                    val idx = billsList.indexOfFirst { it.id == bill.id }
                                    if (idx != -1) {
                                        billsList[idx] = bill.copy(isPaid = !bill.isPaid)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Later this Month Section (Unpaid)
            val laterThisMonthBills = billsList.filter { it.category == BillCategory.LATER_THIS_MONTH && !it.isPaid }
            if (laterThisMonthBills.isNotEmpty()) {
                Text("Later this Month", color = CreamText, style = MaterialTheme.typography.titleSmall)
                laterThisMonthBills.forEach { bill ->
                    key(bill.id) {
                        SwipeableBillRow(
                            onDelete = { onDeleteBill(bill) },
                            onEdit = { onEditBill(bill) }
                        ) {
                            BillCard(
                                title = bill.title,
                                provider = bill.provider,
                                icon = bill.icon,
                                amount = bill.amount,
                                dueDate = bill.dueDate,
                                isPaid = bill.isPaid,
                                autoPay = bill.autoPay,
                                onClick = {
                                    val idx = billsList.indexOfFirst { it.id == bill.id }
                                    if (idx != -1) {
                                        billsList[idx] = bill.copy(isPaid = !bill.isPaid)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Paid Section
            val paidBills = billsList.filter { it.isPaid }
            if (paidBills.isNotEmpty()) {
                Text("Paid", color = MutedCream, style = MaterialTheme.typography.titleSmall)
                paidBills.forEach { bill ->
                    key(bill.id) {
                        SwipeableBillRow(
                            onDelete = { onDeleteBill(bill) },
                            onEdit = { onEditBill(bill) }
                        ) {
                            BillCard(
                                title = bill.title,
                                provider = bill.provider,
                                icon = bill.icon,
                                amount = bill.amount,
                                dueDate = bill.dueDate,
                                isPaid = bill.isPaid,
                                autoPay = bill.autoPay,
                                onClick = {
                                    val idx = billsList.indexOfFirst { it.id == bill.id }
                                    if (idx != -1) {
                                        billsList[idx] = bill.copy(isPaid = !bill.isPaid)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
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

    if (showAddBillDialog) {
        var title by remember { mutableStateOf("") }
        var provider by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf("") }
        var autoPay by remember { mutableStateOf(false) }
        var isPaid by remember { mutableStateOf(false) }
        var category by remember { mutableStateOf(BillCategory.THIS_WEEK) }
        var selectedIcon by remember { mutableStateOf(availableIcons[0]) }
        val iconScrollState = rememberScrollState()

        Dialog(
            onDismissRequest = { showAddBillDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // Fixed header — never scrolls
                    Text(
                        "Add New Bill",
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
                    )

                    // Scrollable content in the middle
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
                            label = { Text("Bill Name", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = provider,
                            onValueChange = { provider = it },
                            label = { Text("Provider (e.g. BESCOM)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date (e.g. Tomorrow, May 25)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Due Period", color = MutedCream, style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BillCategory.values().forEach { cat ->
                                val isSelected = category == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) OliveAccent else GlassSurface)
                                        .clickable { category = cat }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        color = if (isSelected) NearBlack else CreamText,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AutoPay Enabled", color = CreamText, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = autoPay, onCheckedChange = { autoPay = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NearBlack, checkedTrackColor = OliveAccent,
                                    uncheckedThumbColor = MutedCream, uncheckedTrackColor = GlassSurface
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mark as Paid", color = CreamText, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isPaid, onCheckedChange = { isPaid = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NearBlack, checkedTrackColor = OliveAccent,
                                    uncheckedThumbColor = MutedCream, uncheckedTrackColor = GlassSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Fixed buttons at bottom — never scrolls
                    HorizontalDivider(color = GlassBorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAddBillDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MutedCream)
                        ) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                if (title.isNotBlank() && amount.toDoubleOrNull() != null) {
                                    billsList.add(
                                        Bill(
                                            title = title,
                                            provider = provider.ifBlank { "Unknown" },
                                            icon = selectedIcon,
                                            amount = amount.toDoubleOrNull() ?: 0.0,
                                            dueDate = dueDate.ifBlank { "TBD" },
                                            isPaid = isPaid,
                                            autoPay = autoPay,
                                            category = category
                                        )
                                    )
                                    showAddBillDialog = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = OliveAccent)
                        ) { Text("Add") }
                    }
                }
            }
        }
    }

    if (showEditBillDialog && billToEdit != null) {
        var title by remember(billToEdit) { mutableStateOf(billToEdit!!.title) }
        var provider by remember(billToEdit) { mutableStateOf(billToEdit!!.provider) }
        var amount by remember(billToEdit) { mutableStateOf(billToEdit!!.amount.toString()) }
        var dueDate by remember(billToEdit) { mutableStateOf(billToEdit!!.dueDate) }
        var autoPay by remember(billToEdit) { mutableStateOf(billToEdit!!.autoPay) }
        var isPaid by remember(billToEdit) { mutableStateOf(billToEdit!!.isPaid) }
        var category by remember(billToEdit) { mutableStateOf(billToEdit!!.category) }
        var selectedIcon by remember(billToEdit) { mutableStateOf(billToEdit!!.icon) }
        val iconScrollState = rememberScrollState()

        Dialog(
            onDismissRequest = { showEditBillDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceDark
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // Fixed header — never scrolls
                    Text(
                        "Edit Bill",
                        color = CreamText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp)
                    )

                    // Scrollable content in the middle
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
                            label = { Text("Bill Name", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = provider,
                            onValueChange = { provider = it },
                            label = { Text("Provider (e.g. BESCOM)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount (₹)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date (e.g. Tomorrow, May 25)", color = MutedCream, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CreamText, unfocusedTextColor = CreamText,
                                focusedBorderColor = OliveAccent, unfocusedBorderColor = GlassBorder,
                                focusedLabelColor = OliveAccent, unfocusedLabelColor = MutedCream
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Due Period", color = MutedCream, style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BillCategory.values().forEach { cat ->
                                val isSelected = category == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) OliveAccent else GlassSurface)
                                        .clickable { category = cat }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        color = if (isSelected) NearBlack else CreamText,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AutoPay Enabled", color = CreamText, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = autoPay, onCheckedChange = { autoPay = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NearBlack, checkedTrackColor = OliveAccent,
                                    uncheckedThumbColor = MutedCream, uncheckedTrackColor = GlassSurface
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mark as Paid", color = CreamText, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isPaid, onCheckedChange = { isPaid = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NearBlack, checkedTrackColor = OliveAccent,
                                    uncheckedThumbColor = MutedCream, uncheckedTrackColor = GlassSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Fixed buttons at bottom — never scrolls
                    HorizontalDivider(color = GlassBorder)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showEditBillDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MutedCream)
                        ) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                if (title.isNotBlank() && amount.toDoubleOrNull() != null) {
                                    val idx = billsList.indexOfFirst { it.id == billToEdit!!.id }
                                    if (idx != -1) {
                                        billsList[idx] = billToEdit!!.copy(
                                            title = title,
                                            provider = provider.ifBlank { "Unknown" },
                                            icon = selectedIcon,
                                            amount = amount.toDoubleOrNull() ?: 0.0,
                                            dueDate = dueDate.ifBlank { "TBD" },
                                            isPaid = isPaid,
                                            autoPay = autoPay,
                                            category = category
                                        )
                                    }
                                    showEditBillDialog = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = OliveAccent)
                        ) { Text("Save") }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableBillRow(
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

@Composable
fun SummaryCard(
    title: String,
    amount: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = MutedCream, style = MaterialTheme.typography.labelMedium)
            }
            Text(amount, color = CreamText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BillCard(
    title: String,
    provider: String,
    icon: ImageVector,
    amount: Double,
    dueDate: String,
    isPaid: Boolean,
    autoPay: Boolean,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isPaid) Color.Transparent else SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isPaid) androidx.compose.foundation.BorderStroke(1.dp, GlassBorder) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isPaid) Color.Transparent else GlassSurface, 
                        RoundedCornerShape(12.dp)
                    )
                    .then(if (isPaid) Modifier.padding(1.dp) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (isPaid) MutedCream else CreamText)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    color = if (isPaid) MutedCream else CreamText, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(provider, color = MutedCream, style = MaterialTheme.typography.labelSmall)
                    if (autoPay) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0x1594A853), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("AutoPay", color = OliveAccent, fontSize = 10.sp)
                        }
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${String.format("%.0f", amount)}", 
                    color = if (isPaid) MutedCream else CreamText, 
                    fontWeight = FontWeight.Bold
                )
                if (isPaid) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OliveAccent, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paid", color = OliveAccent, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Text(
                        dueDate, 
                        color = if (dueDate == "Tomorrow" || dueDate == "Today") RedReveal else BurntOrangeAccent, 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
