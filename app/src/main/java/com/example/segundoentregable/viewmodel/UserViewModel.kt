package com.example.segundoentregable.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.segundoentregable.data.model.User
import com.example.segundoentregable.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application.applicationContext)

    // Keep last operation message observable if needed (not LiveData here for simplicity)
    var lastMessage: String = ""

    fun register(name: String, email: String, password: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                if (name.isBlank() || email.isBlank() || password.length < 6) {
                    false
                } else {
                    repo.register(User(name.trim(), email.trim(), password))
                }
            }
            callback(ok)
            lastMessage = if (ok) "Registro exitoso" else "Correo ya existe o datos inválidos"
        }
    }

    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                repo.login(email.trim(), password)
            }
            if (ok) {
                repo.setCurrentUser(email.trim())
            }
            callback(ok)
            lastMessage = if (ok) "" else "Credenciales incorrectas"
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setCurrentUser(null)
        }
    }

    fun getCurrentUser(): User? {
        val email = repo.getCurrentUserEmail() ?: return null
        return repo.getUser(email)
    }
}
