package com.spark.roadvibe.app.di

import com.google.android.gms.location.LocationServices
import com.spark.roadvibe.app.BuildConfig
import com.spark.roadvibe.app.data.repository.RoadVibeTelemetryRepositoryImpl
import com.spark.roadvibe.app.location.GMSLocationObservationImpl
import com.spark.roadvibe.app.ui.viewmodels.MainViewModel
import com.spark.roadvibe.lib.androidContext
import com.spark.roadvibe.lib.androidLogger
import com.spark.roadvibe.lib.applicationLocation
import com.spark.roadvibe.lib.applicationTelemetryRepository
import com.spark.roadvibe.lib.data.TelemetryRepository
import com.spark.roadvibe.lib.infrastrucure.Level
import com.spark.roadvibe.lib.location.ApplicationLocation
import com.spark.roadvibe.lib.startRvs
import com.spark.roadvibe.lib.useNextGenApi
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    factoryOf(::RoadVibeTelemetryRepositoryImpl) { bind<TelemetryRepository>() }
    factory {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }
    single<ApplicationLocation> {
        GMSLocationObservationImpl(get())
    }
    factory {
        startRvs {
            androidContext(androidContext())
//            applicationTelemetryRepository(get())
//            applicationLocation(get())
            androidLogger(if (BuildConfig.DEBUG) Level.VERBOSE else Level.INFO)
            useNextGenApi()
        }
    }

    viewModel {
        MainViewModel(
            get()
//            ,get()
        )
    }
}