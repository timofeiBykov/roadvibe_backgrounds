package ru.gorev.example.roadvibe

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.spark.roadvibe.lib.RoadVibeService
import com.spark.roadvibe.lib.androidContext
import com.spark.roadvibe.lib.androidLogger
import com.spark.roadvibe.lib.applicationCoroutineScope
import com.spark.roadvibe.lib.applicationLocation
import com.spark.roadvibe.lib.applicationTelemetryRepository
import com.spark.roadvibe.lib.data.RvsState
import com.spark.roadvibe.lib.data.TelemetryRepository
import com.spark.roadvibe.lib.infrastrucure.Level
import com.spark.roadvibe.lib.startRvs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import ru.gorev.example.roadvibe.location.LocationService
import java.util.concurrent.Executors

class MainActivityViewModel(
    context: Context
) : ViewModel() {
    private val roadVibeService: RoadVibeService
    private val recordStateMutableLiveData = MutableLiveData<RecordingState>()

    init {
        roadVibeService = createRoadVibeService(context)
    }

    internal val recordState
        get() = recordStateMutableLiveData

    fun startRecord() {
        try {
            roadVibeService.beginRecord()
            recordStateMutableLiveData.postValue(roadVibeService.state.toAppState())
        } catch (ex: Exception) {
            Log.e("example", "service exception")
        }
    }

    fun pauseRecord() {
        roadVibeService.pauseRecord()
        recordStateMutableLiveData.postValue(roadVibeService.state.toAppState())
    }

    fun stopRecord() {
        roadVibeService.finishRecord()
        roadVibeService.uploadTelemetry()
        recordStateMutableLiveData.postValue(roadVibeService.state.toAppState())
    }

    // creates full library side service
    private fun createRoadVibeService(context: Context): RoadVibeService {
        return startRvs {
            // required, for correct work
            androidContext(context)
            androidLogger(Level.VERBOSE) // also can done, without passing Level androidLogger(), default level is Level.INFO
            // required
        }
    }

    private fun createManagedRoadVibeService(context: Context, locationService: LocationService, repository: TelemetryRepository): RoadVibeService {
        val coroutineDispatcher =
            Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()

        return startRvs {
            androidContext(context)
            androidLogger()
            applicationLocation(locationService) // optional
            applicationTelemetryRepository(repository) // optional
            applicationCoroutineScope(CoroutineScope(coroutineDispatcher)) // optional
        }
    }
}

internal enum class RecordingState {
    RECORD, PAUSE, STOP
}

internal fun RvsState.toAppState(): RecordingState {
    return when (this) {
        RvsState.RECORDING -> RecordingState.RECORD
        RvsState.PAUSED -> RecordingState.PAUSE
        else -> RecordingState.STOP
    }
}