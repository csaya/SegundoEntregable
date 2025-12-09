package com.example.segundoentregable.ui.detail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.segundoentregable.AppApplication

@Suppress("UNCHECKED_CAST")
class AttractionDetailViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()

        // CORRECCIÓN: Usamos la instancia única de AppApplication
        val app = application as AppApplication

        return AttractionDetailViewModel(
            attractionRepo = app.attractionRepository, // Inyectamos el repo existente
            favoriteRepo = app.favoriteRepository,     // Inyectamos el repo existente
            userRepo = app.userRepository,             // Inyectamos el repo existente
            userRouteRepo = app.userRouteRepository,
            savedStateHandle = savedStateHandle
        ) as T
    }
}