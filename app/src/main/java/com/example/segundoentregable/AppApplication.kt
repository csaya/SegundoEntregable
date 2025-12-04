package com.example.segundoentregable

import android.app.Application
import com.example.segundoentregable.data.local.AppDatabase

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializar la base de datos
        AppDatabase.getInstance(this)
    }
}
