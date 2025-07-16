package com.spark.roadvibe.app.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "roadvibe_telemetry_trackpoint")
data class RoadVibeTelemetryTrackPointEntity(
    @PrimaryKey
    val ts: Long,
    val scId: UUID?,
    val gx: Double,
    val gy: Double,
    val gz: Double,
    val ax: Double,
    val ay: Double,
    val az: Double,
    val lat: Double,
    val lon: Double,
    val alt: Double,
    val sp: Double,
    @ColumnInfo(defaultValue = "0")
    val xangle: Double,
    @ColumnInfo(defaultValue = "0")
    val yangle: Double,
    @ColumnInfo(defaultValue = "0")
    val zangle: Double,
    @ColumnInfo(defaultValue = "0")
    val anglecos: Double,
    val finished: Boolean? = null
)
