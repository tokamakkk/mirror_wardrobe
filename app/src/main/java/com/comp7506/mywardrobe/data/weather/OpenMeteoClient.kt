package com.comp7506.mywardrobe.data.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class OpenMeteoClient {
    suspend fun fetchCurrentWeather(lat: Double, lon: Double): WeatherResult {
        return withContext(Dispatchers.IO) {
            runCatching {
                val url = buildUrl(lat, lon)
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 8_000
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                }

                val code = conn.responseCode
                val body =
                    (if (code in 200..299) conn.inputStream else conn.errorStream)
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: ""

                if (code !in 200..299) {
                    return@runCatching WeatherResult.Error("Weather request failed ($code)")
                }

                val json = JSONObject(body)
                val current = json.optJSONObject("current")
                val weather = CurrentWeather(
                    temperatureC = current?.optDoubleOrNull("temperature_2m"),
                    apparentTemperatureC = current?.optDoubleOrNull("apparent_temperature"),
                    windSpeedMps = current?.optDoubleOrNull("wind_speed_10m"),
                    relativeHumidityPct = current?.optDoubleOrNull("relative_humidity_2m"),
                    precipitationMm = current?.optDoubleOrNull("precipitation"),
                    cloudCoverPct = current?.optDoubleOrNull("cloud_cover"),
                    weatherCode = current?.optIntOrNull("weather_code"),
                    observedAtIso = current?.optString("time"),
                )

                WeatherResult.Success(lat = lat, lon = lon, weather = weather)
            }.getOrElse { e ->
                WeatherResult.Error(e.message ?: "Weather request failed")
            }
        }
    }

    private fun buildUrl(lat: Double, lon: Double): String {
        val params = linkedMapOf(
            "latitude" to lat.toString(),
            "longitude" to lon.toString(),
            "timezone" to "auto",
            "current" to listOf(
                "temperature_2m",
                "apparent_temperature",
                "weather_code",
                "wind_speed_10m",
                "relative_humidity_2m",
                "precipitation",
                "cloud_cover",
            ).joinToString(","),
        )

        val query = params.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }
        return "https://api.open-meteo.com/v1/forecast?$query"
    }
}

private fun JSONObject.optDoubleOrNull(key: String): Double? {
    if (!has(key)) return null
    val v = optDouble(key)
    return if (v.isNaN()) null else v
}

private fun JSONObject.optIntOrNull(key: String): Int? {
    if (!has(key)) return null
    return runCatching { getInt(key) }.getOrNull()
}

