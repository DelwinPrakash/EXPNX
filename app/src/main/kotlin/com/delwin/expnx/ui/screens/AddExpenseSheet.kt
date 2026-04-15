package com.delwin.expnx.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.delwin.expnx.data.Category
import com.delwin.expnx.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    onSave: (Double, Category, String, Long) -> Unit,
    onCancel: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.OTHER) }
    var amountError by remember { mutableStateOf<String?>(null) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 50.dp) // Extra padding for bottom sheet handles
    ) {
        Text(
            text = "Add New Expense",
            style = MaterialTheme.typography.titleLarge,
            color = DarkForest,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { 
                amount = it
                amountError = null
            },
            label = { Text("Amount ($)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = amountError != null,
            supportingText = { amountError?.let { Text(it) } },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OlivePrimary,
                unfocusedBorderColor = OlivePrimary.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Category",
            style = MaterialTheme.typography.labelSmall,
            color = OlivePrimary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Category.values().forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarmTan,
                        selectedLabelColor = DarkForest,
                        selectedLeadingIconColor = DarkForest
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OlivePrimary,
                unfocusedBorderColor = OlivePrimary.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDatePicker = true },
            colors = ButtonDefaults.buttonColors(containerColor = WarmCream, contentColor = DarkForest),
            border = androidx.compose.foundation.BorderStroke(1.dp, OlivePrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            val date = datePickerState.selectedDateMillis?.let { Date(it) } ?: Date()
            Text("Date: ${java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)}")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkForest),
                border = androidx.compose.foundation.BorderStroke(1.dp, OlivePrimary)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0) {
                        amountError = "Enter a valid amount"
                    } else {
                        onSave(amountVal, selectedCategory, description, datePickerState.selectedDateMillis ?: System.currentTimeMillis())
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = OlivePrimary, contentColor = WarmCream)
            ) {
                Text("Save")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK", color = OlivePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = OlivePrimary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
