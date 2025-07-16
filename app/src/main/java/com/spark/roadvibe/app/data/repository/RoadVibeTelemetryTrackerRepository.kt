package com.spark.roadvibe.app.data.repository

import android.location.Location
import com.spark.roadvibe.app.database.dao.RoadVibeTelemetryPointDao
import com.spark.roadvibe.app.database.model.RoadVibeTelemetryTrackPointEntity
import com.spark.roadvibe.lib.data.TelemetryPoint
import com.spark.roadvibe.lib.data.TelemetryRepository
import java.util.UUID

class RoadVibeTelemetryRepositoryImpl(private val pointDao: RoadVibeTelemetryPointDao) : TelemetryRepository {
    override suspend fun getTotal(): Int {
        return pointDao.getTotalTrackPoints()
    }

    override suspend fun getScope(scopeId: UUID, limit: Int): List<TelemetryPoint> {
        return pointDao.getTrackPoints(scopeId, limit).map { it.asDataModel() }
    }

    suspend fun insertLocationFromService(location: Location) {
        val point = RoadVibeTelemetryTrackPointEntity(
            ts = location.time,
            scId = UUID.randomUUID(), // или текущий активный scope
            gx = 0.0,
            gy = 0.0,
            gz = 0.0,
            ax = 0.0,
            ay = 0.0,
            az = 0.0,
            lat = location.latitude,
            lon = location.longitude,
            alt = location.altitude,
            sp = location.speed.toDouble(),
            xangle = 0.0,
            yangle = 0.0,
            zangle = 0.0,
            anglecos = 0.0
        )
        pointDao.insertOrReplacePoints(listOf(point))
    }


    override suspend fun getScopeIds(): List<Pair<UUID, Int>> {
        return pointDao.getEachTrackScopeId().map { Pair(it.scId, it.count) }
    }

    override suspend fun saveScope(scopeId: UUID, data: List<TelemetryPoint>) {
        pointDao.insertOrReplacePoints(data.map { it.asEntity(scopeId) })
    }

    override suspend fun remove(ids: List<Long>) {
        return pointDao.removePointsOrIgnore(ids)
    }
}

fun RoadVibeTelemetryTrackPointEntity.asDataModel() =
    TelemetryPoint(
        ts,
        gx,
        gy,
        gz,
        ax,
        ay,
        az,
        lat,
        lon,
        alt,
        sp,
        xangle,
        yangle,
        zangle,
        anglecos
    )

fun TelemetryPoint.asEntity(scopeId: UUID) =
    RoadVibeTelemetryTrackPointEntity(
        ts,
        scopeId,
        gx,
        gy,
        gz,
        ax,
        ay,
        az,
        lat,
        lon,
        alt,
        sp,
        xangle,
        yangle,
        zangle,
        anglecos
    )