package com.example.binthere.data.local.entities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.binthere.data.local.dao.WasteDao
import com.example.binthere.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CleanupAlertEntity::class, PickupScheduleEntity::class, CollectionPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wasteDao(): WasteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "binthere_database"
                ).addCallback(DatabaseCallback()).build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.wasteDao().insertCollectionPoints(
                        listOf(
                            CollectionPointEntity(zone = "CBD", pointName = "Muthurwa Market Main Skip", operatingHours = "6:00 AM - 8:00 PM"),
                            CollectionPointEntity(zone = "CBD", pointName = "Jeevanjee Gardens North Point", operatingHours = "7:00 AM - 6:00 PM"),
                            CollectionPointEntity(zone = "CBD", pointName = "City Market Waste Bay", operatingHours = "6:00 AM - 7:00 PM"),
                            CollectionPointEntity(zone = "Westlands", pointName = "Westlands Bus Stage Skip", operatingHours = "24 Hours"),
                            CollectionPointEntity(zone = "Eastleigh", pointName = "Garissa Lodge Collection Point", operatingHours = "6:00 AM - 9:00 PM"),
                            CollectionPointEntity(zone = "South B", pointName = "South B Shopping Centre Bin", operatingHours = "7:00 AM - 7:00 PM")
                        )
                    )
                }
            }
        }
    }
}