package com.comp7506.mywardrobe.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.Geocoder
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationProvider(context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private val appContext = context.applicationContext

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocationOrNull(): Location? {
        val last = runCatching { client.lastLocation.await() }.getOrNull()
        if (last != null) return last

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        return runCatching { client.getCurrentLocation(request, null).await() }.getOrNull()
    }

    fun getCityNameOrNull(lat: Double, lon: Double): String? {
        return runCatching {
            val geocoder = Geocoder(appContext, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocation(lat, lon, 1)
            val addr = results?.firstOrNull()
            addr?.locality
                ?: addr?.subAdminArea
                ?: addr?.adminArea
                ?: addr?.countryName
        }.getOrNull()
    }

    fun getLatLonByCityNameOrNull(cityName: String): Pair<Double, Double>? {
        return runCatching {
            val geocoder = Geocoder(appContext, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocationName(cityName, 1)
            val addr = results?.firstOrNull() ?: return@runCatching null
            Pair(addr.latitude, addr.longitude)
        }.getOrNull()
    }
}

