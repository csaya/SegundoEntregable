package com.example.segundoentregable.ui.favorites

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.segundoentregable.AppApplication

@Suppress("UNCHECKED_CAST")
class FavoritesViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val app = application as AppApplication

        return FavoritesViewModel(
            attractionRepo = app.attractionRepository,
            favoriteRepo = app.favoriteRepository,
            userRepo = app.userRepository
        ) as T
    }
}