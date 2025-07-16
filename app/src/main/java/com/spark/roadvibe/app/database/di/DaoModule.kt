package com.spark.roadvibe.app.database.di

import com.spark.roadvibe.app.database.RVADatabase
import org.koin.dsl.module

val daoModule = module {
    single {
        get<RVADatabase>().trackPointDao()
    }
}