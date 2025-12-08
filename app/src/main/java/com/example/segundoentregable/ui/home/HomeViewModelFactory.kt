package com.example.segundoentregable.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.segundoentregable.AppApplication

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val app = application as AppApplication
            return HomeViewModel(
                repo = app.attractionRepository,
                locationService = app.locationService,
                isDataReadyFlow = app.isDataReady
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}