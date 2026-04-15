package com.delwin.expnx.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class Category(val displayName: String, val icon: ImageVector) {
    FOOD("Food", Icons.Default.Restaurant),
    TRANSPORT("Transport", Icons.Default.DirectionsCar),
    SHOPPING("Shopping", Icons.Default.ShoppingBag),
    HEALTH("Health", Icons.Default.MedicalServices),
    ENTERTAINMENT("Entertainment", Icons.Default.Movie),
    BILLS("Bills", Icons.Default.ReceiptLong),
    OTHER("Other", Icons.Default.Category)
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: Category,
    val description: String,
    val date: Long // Timestamp
)
