package com.spark.roadvibe.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.spark.roadvibe.app.location.LocationForegroundService
import java.security.Permission

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: запускается MainActivity")
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { RoadvibeApp() }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (PermissionManager.hasLocationPermissions(this)) {
            Log.d("MainActivity", "Разрешения уже есть — запускаем сервис")
            startLocationTracking() //вызов сбора данных в фоновом режиме
        } else {
            Log.d("MainActivity", "Разрешения НЕТ — запрашиваем")
            PermissionManager.requestPermissions(this, 1001)
        }
    }

    private fun startLocationTracking(){
        if (PermissionManager.hasLocationPermissions(this)){
            Log.d("MainActivity", "Запускаем LocationForegroundService")
            val intent = Intent(this, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }else {
            PermissionManager.requestPermissions(this, 1001)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.d("MainActivity", "Разрешение ПОЛУЧЕНО — запускаем сервис")
            startLocationTracking()
        }else {
            Toast.makeText(this, "Разрешенине на геолокацию нк выданно", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop: Activity is stopping, starting foreground service")
        startLocationTracking() // запускаем сервис
    }

}