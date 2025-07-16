package com.spark.roadvibe.app.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.spark.roadvibe.lib.location.ApplicationLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@SuppressLint("MissingPermission")
class GMSLocationObservationImpl(private val fusedLocationProviderClient: FusedLocationProviderClient) : ApplicationLocation {
    private lateinit var previousLocation: Location
    private val _flow = MutableSharedFlow<Location>()
    private val _flowDistance = MutableStateFlow<Double>(0.0)

    init {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            0
        ).build()
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                val location = p0.lastLocation ?: return
                if (!::previousLocation.isInitialized) {
                    _flow.tryEmit(location)
                    previousLocation = location
                } else if (location.hasAccuracy() && location.accuracy <= previousLocation.accuracy + ACCURACY_TOLERANCE) {
                    previousLocation = location
                    _flow.tryEmit(location)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override val location: Flow<Location>
        get() = _flow

    companion object{
        // Константа допустимого разброса по точности (в метрах)
        private val ACCURACY_TOLERANCE = 10 // Здесь можно задать допустимый разброс по точности в метрах
    }
}