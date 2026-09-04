package com.comp7506.mywardrobe.ui.viewmodel

import android.location.Location
import com.comp7506.mywardrobe.data.location.LocationProvider
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import com.comp7506.mywardrobe.data.weather.CurrentWeather
import com.comp7506.mywardrobe.data.weather.WeatherRepository
import com.comp7506.mywardrobe.data.weather.WeatherResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshWeather_locationTimeout_switchesToErrorState() = runTest {
        val repository = mockWardrobeRepository()
        val locationProvider = mockk<LocationProvider>()
        val weatherRepository = mockk<WeatherRepository>()

        coEvery { locationProvider.getLatLonByCityNameOrNull(any()) } returns Pair(22.3, 114.17)
        coEvery {
            weatherRepository.getCurrentWeather(any(), any(), any())
        } returns WeatherResult.Success(
            lat = 22.3,
            lon = 114.17,
            weather = CurrentWeather(
                temperatureC = 26.0,
                apparentTemperatureC = 26.0,
                windSpeedMps = 2.0,
                relativeHumidityPct = 60.0,
                precipitationMm = 0.0,
                cloudCoverPct = 20.0,
                weatherCode = 0,
                observedAtIso = null,
            ),
        )

        // 模拟定位卡住 10 秒，应该被 HomeViewModel 的 5 秒超时兜底。
        coEvery { locationProvider.getLastKnownLocationOrNull() } coAnswers {
            delay(10_000)
            null
        }

        val viewModel = HomeViewModel(repository, locationProvider, weatherRepository)
        advanceUntilIdle()
        viewModel.refreshWeather(forceRefresh = false)

        // 推进测试虚拟时间，触发 withTimeoutOrNull(5_000)。
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Error)
    }

    @Test
    fun initialWeatherLoad_networkFailure_setsErrorState() = runTest {
        val repository = mockWardrobeRepository()
        val locationProvider = mockk<LocationProvider>()
        val weatherRepository = mockk<WeatherRepository>()

        coEvery { locationProvider.getLatLonByCityNameOrNull(any()) } returns Pair(22.3, 114.17)
        coEvery { weatherRepository.getCurrentWeather(any(), any(), any()) } throws RuntimeException("Network down")

        val viewModel = HomeViewModel(repository, locationProvider, weatherRepository)
        advanceUntilIdle()

        val state = viewModel.weatherState.value
        assertTrue(state is WeatherUiState.Error)
    }

    private fun mockWardrobeRepository(): WardrobeRepository {
        val repository = mockk<WardrobeRepository>()
        every { repository.observeTotalClothingCount() } returns flowOf(0)
        every { repository.observeCategoryCounts() } returns flowOf(emptyList())
        every { repository.observeRecentOutfits(limit = any()) } returns flowOf(emptyList())
        every { repository.observeClothingItems(category = null, query = "") } returns flowOf(emptyList())
        return repository
    }
}
