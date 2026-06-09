package com.delwin.expnx.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Query("SELECT * FROM ai_insights WHERE id = 1 LIMIT 1")
    fun getInsight(): Flow<InsightEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsight(insight: InsightEntity)

    @Query("DELETE FROM ai_insights")
    suspend fun clearInsights()
}
