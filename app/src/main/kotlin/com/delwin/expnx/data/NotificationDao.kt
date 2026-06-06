package com.delwin.expnx.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: String)

    @Query("UPDATE notifications SET isUnread = 0 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isAdded = 1 WHERE id = :id")
    suspend fun markAsAdded(id: String)
}
