package com.example.segundoentregable.ui.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.segundoentregable.AppApplication

@Suppress("UNCHECKED_CAST")
class MapViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            val app = application as AppApplication
            return MapViewModel(
                repo = app.attractionRepository,
                isDataReadyFlow = app.isDataReady
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}