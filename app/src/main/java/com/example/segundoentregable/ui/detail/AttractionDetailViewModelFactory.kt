package com.example.segundoentregable.ui.detail

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.segundoentregable.AppApplication

@Suppress("UNCHECKED_CAST")
class AttractionDetailViewModelFactory(
    private val application: Application,
    private val attractionId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val app = application as AppApplication

        val savedStateHandle = SavedStateHandle().apply {
            set("attractionId", attractionId)
        }

        return AttractionDetailViewModel(
            attractionRepo = app.attractionRepository,
            favoriteRepo = app.favoriteRepository,
            userRepo = app.userRepository,
            userRouteRepo = app.userRouteRepository,
            reviewRepo = app.reviewRepository,
            savedStateHandle = savedStateHandle
        ) as T
    }
}