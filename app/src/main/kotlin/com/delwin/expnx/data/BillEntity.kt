package com.delwin.expnx.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.delwin.expnx.ui.screens.plans.Bill
import com.delwin.expnx.ui.screens.plans.BillCategory

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey val id: String,
    val title: String,
    val provider: String,
    val iconName: String,
    val amount: Double,
    val dueDate: String,
    val isPaid: Boolean,
    val autoPay: Boolean,
    val category: BillCategory
)

fun BillEntity.toBill(): Bill {
    val icon = when (iconName) {
        "Bolt" -> Icons.Default.Bolt
        "Wifi" -> Icons.Default.Wifi
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "LiveTv" -> Icons.Default.LiveTv
        "Receipt" -> Icons.Default.Receipt
        "CreditCard" -> Icons.Default.CreditCard
        "Smartphone" -> Icons.Default.Smartphone
        "Home" -> Icons.Default.Home
        else -> Icons.Default.Receipt
    }
    return Bill(
        id = id,
        title = title,
        provider = provider,
        icon = icon,
        amount = amount,
        dueDate = dueDate,
        isPaid = isPaid,
        autoPay = autoPay,
        category = category
    )
}

fun Bill.toEntity(): BillEntity {
    val iconName = when (icon) {
        Icons.Default.Bolt -> "Bolt"
        Icons.Default.Wifi -> "Wifi"
        Icons.Default.DirectionsCar -> "DirectionsCar"
        Icons.Default.FitnessCenter -> "FitnessCenter"
        Icons.Default.LiveTv -> "LiveTv"
        Icons.Default.Receipt -> "Receipt"
        Icons.Default.CreditCard -> "CreditCard"
        Icons.Default.Smartphone -> "Smartphone"
        Icons.Default.Home -> "Home"
        else -> "Receipt"
    }
    return BillEntity(
        id = id,
        title = title,
        provider = provider,
        iconName = iconName,
        amount = amount,
        dueDate = dueDate,
        isPaid = isPaid,
        autoPay = autoPay,
        category = category
    )
}
