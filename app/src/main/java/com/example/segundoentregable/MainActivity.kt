package com.example.segundoentregable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import com.example.segundoentregable.navigation.AppNavGraph
import com.example.segundoentregable.ui.theme.SegundoEntregableTheme
import com.example.segundoentregable.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {

    // obtain AndroidViewModel (requires UserViewModel to extend AndroidViewModel)
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SegundoEntregableTheme {
                val navController = rememberNavController()
                // Pass the same userViewModel to the nav graph so all screens share it
                AppNavGraph(navController = navController, userVM = userViewModel)
            }
        }
    }
}
