package com.delwin.expnx.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.delwin.expnx.ui.screens.NotificationItem

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val iconName: String,
    val isUnread: Boolean,
    val amount: Double? = null,
    val isAdded: Boolean = false
)

fun NotificationEntity.toNotificationItem(): NotificationItem {
    val icon = when (iconName) {
        "AttachMoney" -> Icons.Default.AttachMoney
        "DateRange" -> Icons.Default.DateRange
        "Warning" -> Icons.Default.Warning
        "Info" -> Icons.Default.Info
        "Restaurant" -> Icons.Default.Restaurant
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "MedicalServices" -> Icons.Default.MedicalServices
        "Movie" -> Icons.Default.Movie
        "Receipt" -> Icons.Default.Receipt
        else -> Icons.Default.Info
    }
    
    val diff = System.currentTimeMillis() - timestamp
    val timeString = when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }

    return NotificationItem(
        id = id,
        title = title,
        description = description,
        time = timeString,
        icon = icon,
        isUnread = isUnread,
        amount = amount,
        isAdded = isAdded
    )
}

fun NotificationItem.toEntity(timestamp: Long, iconName: String): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        description = description,
        timestamp = timestamp,
        iconName = iconName,
        isUnread = isUnread,
        amount = amount,
        isAdded = isAdded
    )
}
