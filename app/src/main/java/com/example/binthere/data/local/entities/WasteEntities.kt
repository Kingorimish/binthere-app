package com.example.binthere.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cleanup_alerts")
data class CleanupAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val zone: String,
    val landmark: String,
    val issueType: String,
    val severity: String,
    val status: String = "Pending",
    val reportedByRole: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "pickup_schedules")
data class PickupScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val zone: String,
    val collectionPoint: String,
    val pickupDate: String,
    val pickupTime: String,
    val organizerRole: String,
    val notes: String
)

@Entity(tableName = "collection_points")
data class CollectionPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val zone: String,
    val pointName: String,
    val operatingHours: String
)