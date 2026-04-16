package com.delwin.expnx.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.delwin.expnx.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    initialBudget: Double?,
    onSave: (Double) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var budgetInput by remember { mutableStateOf(initialBudget?.let { String.format("%.0f", it) } ?: "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        titleContentColor = CreamText,
        textContentColor = MutedCream,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(text = if (initialBudget == null) "Set Monthly Budget" else "Edit Monthly Budget")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { 
                        budgetInput = it
                        isError = false
                    },
                    label = { Text("Monthly Budget", color = MutedCream) },
                    prefix = { Text("₹", color = CreamText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    supportingText = if (isError) { { Text("Please enter a valid positive number") } } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TanAccent,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetInput.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onSave(amount)
                    } else if (budgetInput.isBlank()) {
                        onClear()
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BurntOrangeAccent)
            ) {
                Text("Save", color = NearBlack)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (initialBudget != null) {
                        onClear()
                    } else {
                        onDismiss() // Skip
                    }
                }
            ) {
                Text(text = if (initialBudget != null) "Clear Budget" else "Skip", color = OliveAccent)
            }
        }
    )
}
