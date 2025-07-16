package com.spark.roadvibe.app.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.spark.roadvibe.app.database.dao.RoadVibeTelemetryPointDao
import com.spark.roadvibe.app.database.model.RoadVibeTelemetryTrackPointEntity

@Database(
    entities = [
        RoadVibeTelemetryTrackPointEntity::class
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 4)
    ]
)
abstract class RVADatabase : RoomDatabase() {
    abstract fun trackPointDao(): RoadVibeTelemetryPointDao
}