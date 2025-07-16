package com.spark.roadvibe.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
//import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.spark.roadvibe.app.ui.MainScreen
import com.spark.roadvibe.app.ui.theme.RoadvibeTheme

@Composable
internal fun RoadvibeApp() {
    val systemUiController = rememberSystemUiController()
    val useLight = isSystemInDarkTheme()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !useLight
        )
    }
    RoadvibeTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen()
        }
    }
}
