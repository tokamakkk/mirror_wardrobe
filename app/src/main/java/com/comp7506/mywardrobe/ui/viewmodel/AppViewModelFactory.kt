package com.comp7506.mywardrobe.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.comp7506.mywardrobe.data.location.LocationProvider
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import com.comp7506.mywardrobe.data.weather.WeatherRepository

class AppViewModelFactory(
    private val application: Application,
    private val repository: WardrobeRepository,
    private val locationProvider: LocationProvider,
    private val weatherRepository: WeatherRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(
                    repository = repository,
                    locationProvider = locationProvider,
                    weatherRepository = weatherRepository,
                ) as T
            modelClass.isAssignableFrom(WardrobeViewModel::class.java) -> WardrobeViewModel(repository) as T
            modelClass.isAssignableFrom(AddItemViewModel::class.java) -> AddItemViewModel(application, repository) as T
            modelClass.isAssignableFrom(OutfitsViewModel::class.java) -> OutfitsViewModel(repository) as T
            modelClass.isAssignableFrom(CreateOutfitViewModel::class.java) -> CreateOutfitViewModel(repository) as T
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> CalendarViewModel(repository) as T
            modelClass.isAssignableFrom(StatsViewModel::class.java) -> StatsViewModel(repository) as T
            modelClass.isAssignableFrom(WarmthDetailsViewModel::class.java) -> WarmthDetailsViewModel(repository) as T
            modelClass.isAssignableFrom(PortraitCaptureViewModel::class.java) -> PortraitCaptureViewModel() as T
            modelClass.isAssignableFrom(AIViewModel::class.java) -> AIViewModel(application) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
