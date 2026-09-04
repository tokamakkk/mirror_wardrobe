package com.comp7506.mywardrobe

import android.app.Application
import com.comp7506.mywardrobe.auth.FirebaseAuthManager
import com.comp7506.mywardrobe.data.db.AppDatabase
import com.comp7506.mywardrobe.data.location.LocationProvider
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import com.comp7506.mywardrobe.data.weather.WeatherRepository

class MyWardrobeApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: WardrobeRepository by lazy { WardrobeRepository(database) }
    val authManager: FirebaseAuthManager by lazy { FirebaseAuthManager() }
    val locationProvider: LocationProvider by lazy { LocationProvider(this) }
    val weatherRepository: WeatherRepository by lazy { WeatherRepository() }
}

