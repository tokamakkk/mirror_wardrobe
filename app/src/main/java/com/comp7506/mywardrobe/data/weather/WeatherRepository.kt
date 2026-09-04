package com.comp7506.mywardrobe.data.weather

import kotlin.math.abs

class WeatherRepository(
    private val client: OpenMeteoClient = OpenMeteoClient(),
    private val cacheTtlMs: Long = 15 * 60 * 1000L,
) {
    private var cache: CacheEntry? = null

    suspend fun getCurrentWeather(lat: Double, lon: Double, forceRefresh: Boolean = false): WeatherResult {
        val now = System.currentTimeMillis()
        val cached = cache

        if (!forceRefresh && cached != null && now - cached.fetchedAtMs <= cacheTtlMs) {
            val closeEnough = abs(cached.lat - lat) <= 0.02 && abs(cached.lon - lon) <= 0.02
            if (closeEnough) return cached.result
        }

        val result = client.fetchCurrentWeather(lat = lat, lon = lon)
        cache = CacheEntry(lat = lat, lon = lon, fetchedAtMs = now, result = result)
        return result
    }

    private data class CacheEntry(
        val lat: Double,
        val lon: Double,
        val fetchedAtMs: Long,
        val result: WeatherResult,
    )
}

