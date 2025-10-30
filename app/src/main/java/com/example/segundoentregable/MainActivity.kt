package com.example.segundoentregable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.segundoentregable.navigation.AppNavGraph
import com.example.segundoentregable.ui.theme.SegundoEntregableTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SegundoEntregableTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}