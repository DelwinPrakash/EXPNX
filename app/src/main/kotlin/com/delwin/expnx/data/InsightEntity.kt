package com.delwin.expnx.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_insights")
data class InsightEntity(
    @PrimaryKey val id: Int = 1,
    val generalInsight: String,
    val categoryInsightsJson: String,
    val lastUpdated: Long
)
