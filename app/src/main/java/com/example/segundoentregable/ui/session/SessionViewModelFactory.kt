package com.example.segundoentregable.ui.session

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.segundoentregable.AppApplication

@Suppress("UNCHECKED_CAST")
class SessionViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val app = application as AppApplication
        // Inyectamos el mismo repositorio de usuarios que usan Login y Profile
        return SessionViewModel(app.userRepository) as T
    }
}