package ru.gorev.example.roadvibe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    internal lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PackageManager.PERMISSION_GRANTED)
            continueCreation()
        } else {
            continueCreation()
        }
    }

    private fun continueCreation() {
        viewModel = MainActivityViewModel(this)


        findViewById<Button>(R.id.startButton).setOnClickListener {
            viewModel.startRecord()
        }
        findViewById<Button>(R.id.pauseButton).setOnClickListener {
            viewModel.pauseRecord()
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            viewModel.stopRecord()
        }

        val currentState = viewModel.recordState.value ?: RecordingState.STOP
        findViewById<TextView>(R.id.textView).text = "Current State: ${currentState.name}"
        changeButtonsVisibility(currentState)
        viewModel.recordState.observe(this) {
            findViewById<TextView>(R.id.textView).text = "Current State: ${it.name}"
            changeButtonsVisibility(it)
        }
    }

    private fun changeButtonsVisibility(state: RecordingState) {
        when (state) {
            RecordingState.RECORD -> {
                findViewById<Button>(R.id.startButton).visibility = View.GONE
                findViewById<Button>(R.id.pauseButton).visibility = View.VISIBLE
                findViewById<Button>(R.id.stopButton).visibility = View.VISIBLE

            }
            RecordingState.PAUSE -> {
                findViewById<Button>(R.id.startButton).visibility = View.VISIBLE
                findViewById<Button>(R.id.pauseButton).visibility = View.GONE
                findViewById<Button>(R.id.stopButton).visibility = View.VISIBLE
            }
            else -> {
                findViewById<Button>(R.id.startButton).visibility = View.VISIBLE
                findViewById<Button>(R.id.stopButton).visibility = View.GONE
                findViewById<Button>(R.id.pauseButton).visibility = View.GONE
            }
        }
    }
}