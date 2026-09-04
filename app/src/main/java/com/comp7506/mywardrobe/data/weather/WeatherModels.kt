package com.comp7506.mywardrobe.data.weather

data class CurrentWeather(
    val temperatureC: Double?,
    val apparentTemperatureC: Double?,
    val windSpeedMps: Double?,
    val relativeHumidityPct: Double?,
    val precipitationMm: Double?,
    val cloudCoverPct: Double?,
    val weatherCode: Int?,
    val observedAtIso: String?,
)

sealed interface WeatherResult {
    data class Success(val lat: Double, val lon: Double, val weather: CurrentWeather) : WeatherResult
    data class Error(val message: String) : WeatherResult
}

