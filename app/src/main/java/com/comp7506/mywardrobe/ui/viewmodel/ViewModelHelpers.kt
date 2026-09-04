package com.comp7506.mywardrobe.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.comp7506.mywardrobe.MyWardrobeApplication

@Composable
fun rememberAppViewModelFactory(): AppViewModelFactory {
    val app = LocalContext.current.applicationContext as MyWardrobeApplication
    return remember(app) { AppViewModelFactory(app, app.repository, app.locationProvider, app.weatherRepository) }
}

@Composable
fun rememberAuthViewModelFactory(): AuthViewModelFactory {
    val app = LocalContext.current.applicationContext as MyWardrobeApplication
    return remember(app) { AuthViewModelFactory(app.authManager) }
}

@Composable
fun rememberEditOutfitViewModelFactory(outfitId: Long): ViewModelProvider.Factory {
    val app = LocalContext.current.applicationContext as MyWardrobeApplication
    return remember(outfitId, app) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EditOutfitViewModel::class.java)) {
                    return EditOutfitViewModel(app.repository, outfitId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
            }
        }
    }
}
