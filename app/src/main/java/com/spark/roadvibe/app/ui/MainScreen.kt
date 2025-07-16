package com.spark.roadvibe.app.ui

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.LocationDisabled
import androidx.compose.material.icons.rounded.LocationSearching
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.spark.roadvibe.app.R
import com.spark.roadvibe.app.ui.chart.LineChart
import com.spark.roadvibe.app.ui.chart.model.DataSetConfiguration
import com.spark.roadvibe.app.ui.components.DotsPulsing
import com.spark.roadvibe.app.ui.viewmodels.MainViewModel
import com.spark.roadvibe.app.ui.viewmodels.RecordingState
import com.spark.roadvibe.lib.remote.data.RemoteState
import kotlinx.coroutines.flow.observeOn
import org.koin.androidx.compose.koinViewModel
import java.math.RoundingMode
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
internal fun MainScreen(viewModel: MainViewModel = koinViewModel()) {

    val lifecycleOwner = LocalLifecycleOwner.current
    lifecycleOwner.lifecycle.addObserver(viewModel)

    val context = LocalContext.current

    val openDialog = remember { mutableStateOf(false) }
    val compose = rememberLazyListState()
    val locationRequired by viewModel.locationRequiredLiveData.observeAsState()
    val recordingState by viewModel.recordingLiveData.observeAsState()
    val remoteState by viewModel.serverStatusLiveData.observeAsState()
    val distanceReached by viewModel.distanceReached.observeAsState()
    val loading by viewModel.loading.observeAsState()
    val regionNotSupported by viewModel.regionNotSupported.observeAsState()
    val location by viewModel.location.observeAsState()
    val stateChange by viewModel.stateChanging.observeAsState()


    viewModel.stateChanging.observe(lifecycleOwner) {
        Toast.makeText(context, if (it) "Changing record state" else "New state: $recordingState", Toast.LENGTH_SHORT).show()
    }

    if (locationRequired != null && locationRequired!!) {
        openDialog.value = true
    }

    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    ) { pr ->
        val allGranted = pr.all { v -> v.value }
        if (!allGranted) {
            openDialog.value = true
        } else {
            viewModel.updateRecordState(RecordingState.RECORD)
        }
    }

    if (openDialog.value) {
        val canUserChangeMind = !locationPermissionState.allPermissionsGranted &&
                locationPermissionState.shouldShowRationale
        AlertDialog(onDismissRequest = {
            openDialog.value = false
        }, confirmButton = {
            Button(onClick = {
                openDialog.value = false
                if (canUserChangeMind) {
                    locationPermissionState.launchMultiplePermissionRequest()
                }
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }, dismissButton = {
            if (canUserChangeMind) {
                Button(onClick = {
                    openDialog.value = false
                }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        }, title = {
            Text(text = stringResource(id = R.string.unable_to_continue_without_location_title))
        }, text = {
            if (canUserChangeMind) {
                Text(text = stringResource(id = R.string.unable_to_continue_without_location_content))
            } else {
                Text(text = stringResource(id = R.string.unable_to_continue_without_location_settings_only))
            }
        })
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                when (remoteState) {
                    RemoteState.Uploading -> ServerUploading()
                    RemoteState.UploadFailed -> ServerUploadFailed()
                    else -> Text(text = stringResource(id = R.string.business_name))
                }
            },
            actions = {
                if ((loading != null && loading!!) || stateChange == true) {
                    CircularProgressIndicator(
                        Modifier
                            .width(24.dp)
                            .height(24.dp)
                    )
                }
                if (regionNotSupported != null && regionNotSupported!!) {
                    Icon(
                        Icons.Rounded.LocationDisabled,
                        contentDescription = "LocationSearch",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        )
    }) { pv ->
        Column(
            Modifier
                .fillMaxHeight()
                .padding(pv)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,) {
                    Text(text = "Location::")
                    Text(text = "LG: ${location?.lon ?: 0.0}")
                    Text(text = "LT: ${location?.lat ?: 0.0}")
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column {
                        RecordStateButton((!(stateChange ?: false) || (regionNotSupported != null && regionNotSupported!!)), recordingState) {
//                        check permissions first
//                        request permissions grant if not done
                            when {
                                locationPermissionState.allPermissionsGranted -> {
                                    viewModel.updateRecordState(it)
                                }

                                locationPermissionState.shouldShowRationale -> { // here we will record state anyway got we location or not
                                    openDialog.value = true
                                }

                                else -> {
                                    locationPermissionState.launchMultiplePermissionRequest()
                                }
                            }
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
            }

            Row {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    compose,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    cardItem {
                        LineChart(
                            initialConfiguration = getConfigurationForAccelerometerChart(),
                            30000F,
                            sensorValue = viewModel.liveAccSeries,
                            desc = stringResource(id = R.string.accelerometer)
                        )
                    }
                    cardItem {
                        LineChart(
                            initialConfiguration = getConfigurationForGyroscopeChart(),
                            30000F,
                            sensorValue = viewModel.liveGyrSeries,
                            desc = stringResource(id = R.string.gyroscope)
                        )
                    }
                    cardItem {
                        LineChart(
                            initialConfiguration = getConfigurationForAllRotation(),
                            30000F,
                            sensorValue = viewModel.liveRotationSeries,
                            desc = stringResource(id = R.string.allrotation)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ServerStatus(serverStatus: LiveData<RemoteState>) {
    val status by serverStatus.observeAsState()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
        Text(
            text = "${stringResource(id = R.string.server)}:",
        )
        Box(Modifier.padding(start = 4.dp), contentAlignment = Alignment.Center) {
            when (status) {
                RemoteState.Available -> ServerAvailable()
                RemoteState.Uploading -> ServerUploading()
                else -> ServerAvailable()
            }
        }
    }
}

@Composable
internal fun RecordStateButton(
    enabled: Boolean,
    recordState: RecordingState?,
    onClick: (state: RecordingState) -> Unit
) {
    Row {
        AnimatedVisibility(
            visible = recordState == RecordingState.RECORD,
        ) {
            PauseStateButton(enabled = enabled) {
                onClick.invoke(RecordingState.PAUSE)
            }
        }
        AnimatedVisibility(
            visible = recordState == RecordingState.PAUSE,
        ) {
            PlayStateButton(enabled = enabled) {
                onClick.invoke(RecordingState.RECORD)
            }
        }
        AnimatedVisibility(
            visible = recordState != null && recordState != RecordingState.STOP && recordState != RecordingState.PREPARING && recordState != RecordingState.UNSUPPORTED,
        ) {
            Box(modifier = Modifier.run { padding(horizontal = 4.dp) }) {
                StopStateButton(enabled = enabled) {
                    onClick.invoke(RecordingState.STOP)
                }
            }
        }
        AnimatedVisibility(
            visible = ((recordState == RecordingState.STOP || recordState == RecordingState.PREPARING) || recordState == null),
        ) {
            PlayStateButton(enabled = enabled) {
                onClick.invoke(RecordingState.RECORD)
            }
        }
    }
}

@Composable
internal fun PlayStateButton(enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(
            Icons.Rounded.PlayArrow,
            contentDescription = "play",
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun StopStateButton(enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
    ) {
        Icon(
            Icons.Rounded.Stop,
            contentDescription = "stop",
        )
    }
}

@Composable
fun PauseStateButton(enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(
            Icons.Rounded.Pause,
            contentDescription = null
        )
    }
}

@Preview
@Composable
fun ButtonsPreview() {
    val onClick = { }
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.width(200.dp)) {
        if (true) {
            PauseStateButton(onClick = onClick)
        } else {
            PlayStateButton(onClick = onClick)
        }
        StopStateButton(onClick = onClick)
    }
}


@Preview
@Composable
internal fun ServerAvailable() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.CloudDone,
            contentDescription = "Check mark",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text = stringResource(id = R.string.server_available), Modifier.padding(4.dp, 0.dp))
    }
}

@Preview
@Composable
internal fun ServerUnavailable() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.CloudOff,
            contentDescription = "Check mark",
            tint = MaterialTheme.colorScheme.error
        )
        Text(text = stringResource(id = R.string.server_unavailable), Modifier.padding(4.dp, 0.dp))
    }
}

@Preview
@Composable
internal fun ServerUploading() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DotsPulsing()
        Text(text = stringResource(id = R.string.server_uploading), Modifier.padding(4.dp, 0.dp))
    }
}

@Preview
@Composable
internal fun ServerUploadFailed() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.Warning,
            contentDescription = "Check mark",
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = stringResource(id = R.string.server_upload_failed), Modifier.padding(4.dp, 0.dp)
        )
    }
}

internal fun getConfigurationForAccelerometerChart(): List<DataSetConfiguration> {
    return listOf(
        DataSetConfiguration(color = android.graphics.Color.MAGENTA, name = "X"),
        DataSetConfiguration(color = android.graphics.Color.RED, name = "Y"),
        DataSetConfiguration(color = android.graphics.Color.GREEN, name = "Z"),
        DataSetConfiguration(color = android.graphics.Color.BLUE, name = "SUM")
    )
}

internal fun getConfigurationForAllRotation(): List<DataSetConfiguration> {
    return listOf(
        DataSetConfiguration(color = android.graphics.Color.MAGENTA, name = "X"),
        DataSetConfiguration(color = android.graphics.Color.RED, name = "Y"),
        DataSetConfiguration(color = android.graphics.Color.GREEN, name = "Z"),
        DataSetConfiguration(color = android.graphics.Color.BLUE, name = "cos"),
        DataSetConfiguration(color = android.graphics.Color.BLACK, name = "zero")
    )
}

internal fun getConfigurationForGyroscopeChart(): List<DataSetConfiguration> {
    return listOf(
        DataSetConfiguration(color = android.graphics.Color.rgb(228, 142, 26), name = "X"),
        DataSetConfiguration(color = android.graphics.Color.rgb(0, 86, 149), name = "Y"),
        DataSetConfiguration(color = android.graphics.Color.rgb(108, 126, 41), name = "Z"),
    )
}

internal fun formatKilometer(distanceInMeter: Double): String {
    val distanceInKilometer = distanceInMeter / 1000
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.FLOOR

    return "${df.format(distanceInKilometer)}km"
}

internal fun formatMeter(distanceInMeter: Double): String {
    val df = DecimalFormat("###")
    df.roundingMode = RoundingMode.DOWN

    return "${df.format(distanceInMeter)} meters"
}

private fun LazyListScope.cardItem(content: @Composable () -> Unit) {
    item {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(),
            modifier = Modifier.height(250.dp)
        ) {
            Box(Modifier.padding(2.dp)) {
                content()
            }
        }
    }
}