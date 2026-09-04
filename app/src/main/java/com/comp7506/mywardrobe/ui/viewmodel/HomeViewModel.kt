package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.db.OutfitWithItems
import com.comp7506.mywardrobe.data.location.LocationProvider
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import com.comp7506.mywardrobe.data.weather.WeatherRepository
import com.comp7506.mywardrobe.data.weather.WeatherResult
import com.comp7506.mywardrobe.domain.recommendation.OutfitRecommendation
import com.comp7506.mywardrobe.domain.recommendation.OutfitRecommender
import com.comp7506.mywardrobe.domain.recommendation.WarmthCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: WardrobeRepository,
    private val locationProvider: LocationProvider,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    val totalCount: StateFlow<Int> =
        repository.observeTotalClothingCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val categoryCounts: StateFlow<Map<String, Int>> =
        repository.observeCategoryCounts()
            .map { list -> list.associate { it.category to it.count } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val recentOutfits: StateFlow<List<OutfitWithItems>> =
        repository.observeRecentOutfits(limit = 10)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allItems =
        repository.observeClothingItems(category = null, query = "")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _cityName = MutableStateFlow(DEFAULT_WEATHER_CITY)
    val cityName: StateFlow<String> = _cityName.asStateFlow()

    private val _isEditingCity = MutableStateFlow(false)
    val isEditingCity: StateFlow<Boolean> = _isEditingCity.asStateFlow()

    private val _recommendation = MutableStateFlow<OutfitRecommendation?>(null)
    val recommendation: StateFlow<OutfitRecommendation?> = _recommendation.asStateFlow()

    init {
        viewModelScope.launch {
            searchWeatherByCity(DEFAULT_WEATHER_CITY)
        }
    }

    fun setEditingCity(editing: Boolean) {
        _isEditingCity.value = editing
    }

    /**
     * Called when the city field loses focus after inline edit.
     * Persists name in state; fetches weather only when the name changed or last fetch was not successful.
     */
    fun commitCityEdit(draft: String) {
        if (!_isEditingCity.value) return
        _isEditingCity.value = false
        val q = draft.trim()
        if (q.isEmpty()) return
        val sameName = q.equals(_cityName.value, ignoreCase = true)
        val hadSuccess = _weatherState.value is WeatherUiState.Success
        if (sameName && hadSuccess) return
        searchWeatherByCity(q)
    }

    fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) {
            _weatherState.value = WeatherUiState.PermissionDenied
            return
        }
        refreshWeather(forceRefresh = false)
    }

    fun refreshWeather(forceRefresh: Boolean) {
        _weatherState.value = WeatherUiState.Loading
        viewModelScope.launch {
            try {
                val loc = withTimeoutOrNull(5_000) {
                    locationProvider.getLastKnownLocationOrNull()
                }
                if (loc == null) {
                    _weatherState.value = WeatherUiState.Error("Unable to get location. Make sure Location is enabled.")
                    return@launch
                }

                val named = withTimeoutOrNull(3_000) {
                    withContext(Dispatchers.IO) {
                        locationProvider.getCityNameOrNull(loc.latitude, loc.longitude)
                    }
                }
                if (!named.isNullOrBlank()) {
                    _cityName.value = named
                }

                when (val result = weatherRepository.getCurrentWeather(loc.latitude, loc.longitude, forceRefresh = forceRefresh)) {
                    is WeatherResult.Success -> {
                        updateWeatherAndRecommendation(result)
                    }
                    is WeatherResult.Error -> {
                        _weatherState.value = WeatherUiState.Error(result.message)
                        _recommendation.value = null
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(e.message ?: "Failed to refresh weather")
                _recommendation.value = null
            }
        }
    }

    /** One-shot weather refresh for the city currently shown on the card (no GPS). */
    fun retryCurrentCityWeather() {
        searchWeatherByCity(_cityName.value)
    }

    fun searchWeatherByCity(cityName: String) {
        val query = cityName.trim()
        if (query.isEmpty()) return

        _weatherState.value = WeatherUiState.Loading
        _isEditingCity.value = false
        _cityName.value = query

        viewModelScope.launch {
            try {
                val latLon = withTimeoutOrNull(3_000) {
                    withContext(Dispatchers.IO) {
                        locationProvider.getLatLonByCityNameOrNull(query)
                    }
                }
                if (latLon == null) {
                    _weatherState.value = WeatherUiState.Error("Unable to find city: $query")
                    _recommendation.value = null
                    return@launch
                }

                val (lat, lon) = latLon

                when (val result = weatherRepository.getCurrentWeather(lat, lon, forceRefresh = true)) {
                    is WeatherResult.Success -> updateWeatherAndRecommendation(result)
                    is WeatherResult.Error -> {
                        _weatherState.value = WeatherUiState.Error(result.message)
                        _recommendation.value = null
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(e.message ?: "Failed to search weather")
                _recommendation.value = null
            }
        }
    }

    private fun updateWeatherAndRecommendation(result: WeatherResult.Success) {
        _weatherState.value = WeatherUiState.Success(result)
        val baseRequired = WarmthCalculator.requiredWarmth(result.weather)
        val adjustment = WarmthCalculator.adjustment(result.weather)
        val required = (baseRequired + adjustment).coerceAtLeast(0)

        val fromWardrobe = OutfitRecommender.recommendFromWardrobe(
            requiredWarmth = required,
            items = allItems.value.map {
                OutfitRecommender.WardrobeItem(
                    name = it.name,
                    category = it.category,
                    warmthValue = it.warmthValue,
                )
            },
        )
        _recommendation.value = OutfitRecommendation(
            requiredWarmth = required,
            adjustedForFeel = adjustment,
            suggestions = if (fromWardrobe.isNotEmpty()) fromWardrobe else OutfitRecommender.recommend(required),
        )
    }

    companion object {
        const val DEFAULT_WEATHER_CITY = "Hong Kong"
    }
}

sealed interface WeatherUiState {
    data object NeedsPermission : WeatherUiState
    data object PermissionDenied : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(val data: WeatherResult.Success) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
