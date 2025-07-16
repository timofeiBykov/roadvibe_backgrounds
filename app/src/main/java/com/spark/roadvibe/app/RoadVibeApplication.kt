package com.spark.roadvibe.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.spark.roadvibe.app.database.di.daoModule
import com.spark.roadvibe.app.database.di.databaseModule
import com.spark.roadvibe.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class RoadVibeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            AppCenter.start(
                this,
                "90e2fcf7-df4e-410e-933d-ee691677e579",
                Analytics::class.java,
                Crashes::class.java
            )

        }

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.INFO)
            androidContext(this@RoadVibeApplication)
            modules(getAppModules())
        }
    }

    private fun getAppModules() = module {
        includes(
            databaseModule,
            daoModule,
            appModule,
        )
    }
}