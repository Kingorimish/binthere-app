package com.example.binthere.data.local.dao


import androidx.room.*
import com.example.binthere.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: CleanupAlertEntity)

    @Query("SELECT * FROM cleanup_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<CleanupAlertEntity>>

    @Query("SELECT COUNT(*) FROM cleanup_alerts WHERE landmark = :landmark AND issueType = :issueType AND status = 'Pending'")
    suspend fun countDuplicatePendingAlerts(landmark: String, issueType: String): Int

    @Update
    suspend fun updateAlert(alert: CleanupAlertEntity)

    @Delete
    suspend fun deleteAlert(alert: CleanupAlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: PickupScheduleEntity)

    @Query("SELECT * FROM pickup_schedules ORDER BY pickupDate ASC")
    fun getAllSchedules(): Flow<List<PickupScheduleEntity>>

    @Update
    suspend fun updateSchedule(schedule: PickupScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: PickupScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollectionPoints(points: List<CollectionPointEntity>)

    @Query("SELECT * FROM collection_points ORDER BY zone ASC")
    fun getAllCollectionPoints(): Flow<List<CollectionPointEntity>>
}