package com.spark.roadvibe.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.spark.roadvibe.lib.RoadVibeService
import com.spark.roadvibe.lib.data.RvsBeginRecordStatus
import com.spark.roadvibe.lib.data.RvsState
import com.spark.roadvibe.lib.remote.data.RemoteState
import com.spark.roadvibe.lib.sensor.data.SensorType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.time.Duration.Companion.seconds

class MainViewModel(
    private val roadVibeService: RoadVibeService,
//    private val locationService: ApplicationLocation
) : ViewModel(), LifecycleEventObserver {
    private lateinit var sensorsObservationJob: Job
    private lateinit var remoteCheckJob: Job
    private lateinit var roadvibeServiceCheckStatusJob: Job
    private var firstTimestamp: Long = Long.MIN_VALUE
    private var needToReInit: Boolean = false
    private var isToStateChangeRequested: RecordingState? = null;
    private var lastLogTime = 0L
    private var logIntervalMs = 30_000L

    internal val liveAccSeries = MutableLiveData<List<Entry>>()
    internal val liveGyrSeries = MutableLiveData<List<Entry>>()
    internal val liveRotationSeries = MutableLiveData<List<Entry>>()
    internal val serverStatusLiveData = MutableLiveData<RemoteState>()
    internal val recordingLiveData = MutableLiveData<RecordingState>()
    internal val locationRequiredLiveData = MutableLiveData<Boolean>()
    internal val stateChanging = MutableLiveData(false)
    internal val distanceReached = MutableLiveData<Double>()
    internal val loading = MutableLiveData(false)
    internal val regionNotSupported = MutableLiveData(false)
    internal val location = roadVibeService.location.asLiveData()

    init {
        createRoadVibe()

        location.observeForever{
            Log.d("TEST_LOCATION", "Location from service: ${it.lat}, ${it.lon}")
        }
    }

    internal fun updateRecordState(state: RecordingState) {
        when (state) {
            RecordingState.PAUSE -> {
                isToStateChangeRequested = RecordingState.PAUSE
                roadVibeService.pauseRecord()
            }
            RecordingState.STOP -> {
                needToReInit = false
                roadVibeService.finishRecord()
                distanceReached.postValue(0.0)
                isToStateChangeRequested = RecordingState.STOP
            }

            else -> {
                try {
                    val result = roadVibeService.beginRecord()
                    if (result == RvsBeginRecordStatus.WAITING) {
                        loading.postValue(true)
                        needToReInit = true
                    } else if (result != RvsBeginRecordStatus.ALREADYRECORDING){
                        isToStateChangeRequested = RecordingState.RECORD
                    }
                } catch (ex: IllegalStateException) {
                    Log.w("RoadVibe Application", ex.message!!)
                    locationRequiredLiveData.postValue(true)
                }
            }
        }
        stateChanging.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()

        sensorsObservationJob.cancel()
        remoteCheckJob.cancel()
        roadvibeServiceCheckStatusJob.cancel()
    }

    private fun processSensorData(position: Float, sensors: HashMap<SensorType, DoubleArray>) {
        for (i in sensors) {
            val entries = LinkedList<Entry>()
            for ((index, j) in i.value.withIndex()) {
                entries.add(index, Entry(position, j.toString().toFloat()))
            }

            if (i.key == SensorType.Accelerometer) {
                val sumAxis = i.value.sum()
                entries.add(3, Entry(position, sumAxis.toString().toFloat()))
            }

            when (i.key) {
                SensorType.Accelerometer -> liveAccSeries.postValue(entries)
                SensorType.Gyroscope -> liveGyrSeries.postValue(entries);
                SensorType.CurrentRotation -> liveRotationSeries.postValue(entries)
                else -> throw Error("unexpected")
            }
        }
    }

    private fun isRecording(): Boolean {
        return recordingLiveData.value == RecordingState.RECORD
    }

    private fun createRoadVibe() {

        val flow = roadVibeService.sensors.onEach { s ->
            val currentTime = System.currentTimeMillis()

            var currentPosition = 0.0F
            if (firstTimestamp == Long.MIN_VALUE) {
                firstTimestamp = s.timestamp
            } else {
                currentPosition = (s.timestamp - firstTimestamp).toFloat()
            }

            // Ограничиваем частоту логирования
            if (currentTime - lastLogTime >= logIntervalMs) {
                lastLogTime = currentTime
                val gyroscope = s.data[SensorType.Gyroscope]?.joinToString(prefix = "[", postfix = "]")
                val accel = s.data[SensorType.Accelerometer]?.joinToString(prefix = "[", postfix = "]")
                val rotation = s.data[SensorType.CurrentRotation]?.joinToString(prefix = "[", postfix = "]")
                Log.d("TEST_SENSORS", "timestamp: ${s.timestamp}, data: {Gyroscope=$gyroscope, Accelerometer=$accel, CurrentRotation=$rotation}")
            }

            processSensorData(currentPosition, s.data)
        }




        val remoteStateFlow = roadVibeService.remoteStatus.onEach { s ->
            Log.d("ROADVIBE_UPLOAD", "Статус отправки: $s")
            serverStatusLiveData.postValue(s)
        }

        roadvibeServiceCheckStatusJob = viewModelScope.launch {
            while (isActive) {
                recordingLiveData.postValue(roadVibeService.state.asAppState())
                regionNotSupported.postValue(roadVibeService.state == RvsState.UNSUPPORTED)
                if (isToStateChangeRequested != null) {
                    val applicationState = recordingLiveData.value
                    val serviceState = roadVibeService.state.asAppState()
                    if (isToStateChangeRequested == serviceState && applicationState == serviceState){
                        isToStateChangeRequested = null
                        stateChanging.postValue(false)
                    }
                }
                delay(5.seconds)
            }
        }

        sensorsObservationJob = flow.launchIn(viewModelScope)
        remoteCheckJob = remoteStateFlow.launchIn(viewModelScope)

        sensorsObservationJob.start()
        remoteCheckJob.start()
        roadvibeServiceCheckStatusJob.start()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        // not yet
    }
}

internal enum class RecordingState {
    STOP, PAUSE, RECORD, PREPARING, UNSUPPORTED
}

internal fun RvsState.asAppState(): RecordingState {
    return when (this) {
        RvsState.STOPPED -> RecordingState.STOP
        RvsState.PAUSED -> RecordingState.PAUSE
        RvsState.PREPARING -> RecordingState.PREPARING
        RvsState.UNSUPPORTED -> RecordingState.UNSUPPORTED
        else -> RecordingState.RECORD
    }
}

