package com.example.segundoentregable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // <-- Importar
import androidx.navigation.compose.rememberNavController
import com.example.segundoentregable.navigation.AppNavGraph
import com.example.segundoentregable.ui.session.SessionViewModel
import com.example.segundoentregable.ui.session.SessionViewModelFactory // <-- IMPORTANTE: Importar el Factory
import com.example.segundoentregable.ui.theme.SegundoEntregableTheme

class MainActivity : ComponentActivity() {

    // 1. CORRECCIÓN: Usamos el Factory para inyectar el repositorio
    private val sessionViewModel: SessionViewModel by viewModels {
        SessionViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SegundoEntregableTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    sessionViewModel = sessionViewModel
                )
            }
        }
    }
}