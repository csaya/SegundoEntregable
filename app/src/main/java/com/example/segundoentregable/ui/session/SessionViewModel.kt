package com.example.segundoentregable.ui.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.segundoentregable.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Usamos el UserRepository que ya existe
    private val repo = UserRepository(application.applicationContext)

    // 2. El StateFlow que toda la app observará
    private val _isLoggedIn = MutableStateFlow(repo.isUserLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 3. Método para que LoginViewModel nos notifique
    fun login() {
        // (LoginViewModel ya guarda al usuario en el repo)
        // Aquí solo actualizamos el estado global
        _isLoggedIn.value = true
    }

    // 4. Método para que ProfileViewModel nos notifique
    fun logout() {
        // Usamos el viewModelScope AQUÍ
        viewModelScope.launch(Dispatchers.IO) {
            repo.logout() // Llama a la función simple del repo
        }
        _isLoggedIn.value = false // Actualiza el estado global
    }
}