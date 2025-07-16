package ru.gorev.example.roadvibe.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.spark.roadvibe.lib.location.ApplicationLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

//sample of ApplicationLocation implementation
class LocationService(context: Context) : ApplicationLocation {
    private val locationManager: LocationManager
    init {
        locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private lateinit var _location: Flow<Location>
    private lateinit var previousLocation: Location

    override val location: Flow<Location>
        @SuppressLint("MissingPermission")
        get() {
            if (::_location.isInitialized){
                return _location
            }

            _location = channelFlow {
                val locationListener = object : LocationListener {
                    override fun onLocationChanged(p0: Location) {
                        processLocation(p0)
                    }

                    override fun onLocationChanged(locations: MutableList<Location>) {
                        processLocation(locations[-1])
                    }

                    private fun processLocation(p0: Location) {
                        if (!::previousLocation.isInitialized) {
                            channel.trySend(p0)
                            previousLocation = p0
                        } else if (p0.hasAccuracy() && p0.accuracy <= previousLocation.accuracy) {
                            previousLocation = p0
                            channel.trySend(p0)
                        }
                    }
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 0.5F, locationListener)
                awaitClose {
                    locationManager.removeUpdates(locationListener)
                }
            }

            return _location
        }
}