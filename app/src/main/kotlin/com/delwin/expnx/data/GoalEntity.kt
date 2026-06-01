package com.delwin.expnx.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.delwin.expnx.ui.screens.plans.Goal

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val iconName: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val predictedDate: String,
    val monthlySuggestion: Double
)

fun GoalEntity.toGoal(): Goal {
    val icon = when (iconName) {
        "Savings" -> Icons.Default.Savings
        "FlightTakeoff" -> Icons.Default.FlightTakeoff
        "LaptopMac" -> Icons.Default.LaptopMac
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Home" -> Icons.Default.Home
        "SportsEsports" -> Icons.Default.SportsEsports
        "School" -> Icons.Default.School
        "CardGiftcard" -> Icons.Default.CardGiftcard
        "Favorite" -> Icons.Default.Favorite
        else -> Icons.Default.Savings
    }
    return Goal(
        id = id,
        title = title,
        icon = icon,
        targetAmount = targetAmount,
        savedAmount = savedAmount,
        predictedDate = predictedDate,
        monthlySuggestion = monthlySuggestion
    )
}

fun Goal.toEntity(): GoalEntity {
    val iconName = when (icon) {
        Icons.Default.Savings -> "Savings"
        Icons.Default.FlightTakeoff -> "FlightTakeoff"
        Icons.Default.LaptopMac -> "LaptopMac"
        Icons.Default.DirectionsCar -> "DirectionsCar"
        Icons.Default.Home -> "Home"
        Icons.Default.SportsEsports -> "SportsEsports"
        Icons.Default.School -> "School"
        Icons.Default.CardGiftcard -> "CardGiftcard"
        Icons.Default.Favorite -> "Favorite"
        else -> "Savings"
    }
    return GoalEntity(
        id = id,
        title = title,
        iconName = iconName,
        targetAmount = targetAmount,
        savedAmount = savedAmount,
        predictedDate = predictedDate,
        monthlySuggestion = monthlySuggestion
    )
}
