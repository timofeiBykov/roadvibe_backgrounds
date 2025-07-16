package com.spark.roadvibe.app.database.di

import androidx.room.Room
import com.spark.roadvibe.app.database.RVADatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            RVADatabase::class.java,
            "road-vibe-app"
        ).build()
    }
}