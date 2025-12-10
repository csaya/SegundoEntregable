package com.example.segundoentregable.ui.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.segundoentregable.AppApplication
import com.example.segundoentregable.data.firebase.FirebaseAuthService
import com.example.segundoentregable.utils.NetworkConnectivityObserver

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val app = application as AppApplication
        return LoginViewModel(
            repo = app.userRepository,
            firebaseAuth = FirebaseAuthService(),
            connectivityObserver = NetworkConnectivityObserver(application)
        ) as T
    }
}