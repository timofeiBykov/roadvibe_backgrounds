package com.spark.roadvibe.app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.spark.roadvibe.app.database.model.RoadVibeTelemetryTrackPointEntity
import com.spark.roadvibe.app.database.model.ScopeData
import java.util.UUID

@Dao
interface RoadVibeTelemetryPointDao {

    @Query(
        value = """
            SELECT COUNT(*) FROM roadvibe_telemetry_trackpoint
        """
    )
    fun getTotalTrackPoints(): Int

    @Query(
        value = """
            SELECT scId, COUNT(scId) as count FROM roadvibe_telemetry_trackpoint GROUP BY scId
        """
    )
    fun getEachTrackScopeId(): List<ScopeData>

    @Transaction
    @Query(
        value = """
            SELECT * FROM roadvibe_telemetry_trackpoint
            WHERE scId = :scopeId
            LIMIT :limit
        """
    )
    fun getTrackPoints(scopeId: UUID, limit: Int): List<RoadVibeTelemetryTrackPointEntity>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplacePoints(entities: List<RoadVibeTelemetryTrackPointEntity>): List<Long>

    @Query("DELETE FROM roadvibe_telemetry_trackpoint WHERE ts = :id")
    suspend fun removePoint(id: Long): Int

    @Transaction
    suspend fun removePointsOrIgnore(ids: List<Long>) {
        for (id in ids) {
            removePoint(id)
        }
    }

    @Query("UPDATE roadvibe_telemetry_trackpoint SET finished = 1 WHERE ts = (SELECT MAX(ts) from roadvibe_telemetry_trackpoint WHERE scId = :scopeId)")
    suspend fun updateFinishScope(scopeId: UUID)

    @Query(
        value = """
            SELECT scId, COUNT(scId) as count FROM roadvibe_telemetry_trackpoint WHERE (:finished is not null and scId in (SELECT scId from roadvibe_telemetry_trackpoint WHERE finished = :finished)) GROUP BY scId
        """
    )
    suspend fun getTrackScopes(finished: Boolean? = null): List<com.spark.roadvibe.lib.database.model.ScopeData>
}