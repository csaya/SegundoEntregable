package com.example.segundoentregable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // <-- Importar
import androidx.navigation.compose.rememberNavController
import com.example.segundoentregable.navigation.AppNavGraph
import com.example.segundoentregable.ui.session.SessionViewModel // <-- Importar
import com.example.segundoentregable.ui.theme.SegundoEntregableTheme

class MainActivity : ComponentActivity() {

    // 1. Creamos el ViewModel compartido a nivel de Actividad
    private val sessionViewModel: SessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SegundoEntregableTheme {
                val navController = rememberNavController()
                // 2. Lo pasamos al NavGraph
                AppNavGraph(
                    navController = navController,
                    sessionViewModel = sessionViewModel
                )
            }
        }
    }
}