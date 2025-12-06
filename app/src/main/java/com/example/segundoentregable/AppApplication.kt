package com.example.segundoentregable

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.repository.AttractionRepository
import com.example.segundoentregable.data.repository.FavoriteRepository
import com.example.segundoentregable.data.repository.UserRepository
import okhttp3.OkHttpClient

class AppApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val userRepository by lazy {
        UserRepository(database.userDao(), this)
    }

    val attractionRepository by lazy {
        AttractionRepository(database.atractivoDao(), database.reviewDao())
    }

    val favoriteRepository by lazy {
        FavoriteRepository(database.favoritoDao())
    }

    override fun onCreate() {
        super.onCreate()

        // Configurar Coil con caché de disco y OkHttp (Mantenemos tu config de imágenes)
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)
    }
}