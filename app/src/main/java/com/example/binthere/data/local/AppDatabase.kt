package com.example.binthere.data.local

import kotlinx.coroutines.launch

@androidx.room.Database(
    entities = [com.example.binthere.data.local.entities.CleanupAlertEntity::class, com.example.binthere.data.local.entities.PickupScheduleEntity::class, com.example.binthere.data.local.entities.CollectionPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun wasteDao(): com.example.binthere.data.local.dao.WasteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
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
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                _root_ide_package_.kotlinx.coroutines.CoroutineScope(_root_ide_package_.kotlinx.coroutines.Dispatchers.IO)
                    .launch {
                    database.wasteDao().insertCollectionPoints(
                        listOf(
                            _root_ide_package_.com.example.binthere.data.local.entities.CollectionPointEntity(
                                zone = "CBD",
                                pointName = "Muthurwa Market Main Skip",
                                operatingHours = "6:00 AM - 8:00 PM"
                            ),
                            _root_ide_package_.com.example.binthere.data.local.entities.CollectionPointEntity(
                                zone = "CBD",
                                pointName = "Jeevanjee Gardens North Point",
                                operatingHours = "7:00 AM - 6:00 PM"
                            ),
                            _root_ide_package_.com.example.binthere.data.local.entities.CollectionPointEntity(
                                zone = "CBD",
                                pointName = "City Market Waste Bay",
                                operatingHours = "6:00 AM - 7:00 PM"
                            ),
                            _root_ide_package_.com.example.binthere.data.local.entities.CollectionPointEntity(
                                zone = "Westlands",
                                pointName = "Westlands Bus Stage Skip",
                                operatingHours = "24 Hours"
                            ),
                            _root_ide_package_.com.example.binthere.data.local.entities.CollectionPointEntity(
                                zone = "Eastleigh",
                                pointName = "Garissa Lodge Collection Point",
                                operatingHours = "6:00 AM - 9:00 PM"
                            ),
                            _root_ide_package_.com.example.binthere.data.local.entities.CollectionPointEntity(
                                zone = "South B",
                                pointName = "South B Shopping Centre Bin",
                                operatingHours = "7:00 AM - 7:00 PM"
                            )
                        )
                    )
                }
            }
        }
    }
}