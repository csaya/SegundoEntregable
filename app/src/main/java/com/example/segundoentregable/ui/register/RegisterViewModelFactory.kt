package com.example.segundoentregable.ui.register

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.segundoentregable.AppApplication

@Suppress("UNCHECKED_CAST")
class RegisterViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val app = application as AppApplication
        // Inyectamos el UserRepository único
        return RegisterViewModel(app.userRepository) as T
    }
}