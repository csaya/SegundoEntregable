package com.example.segundoentregable.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.segundoentregable.ui.home.HomeScreen
import com.example.segundoentregable.ui.login.LoginScreen
import com.example.segundoentregable.ui.profile.ProfileScreen
import com.example.segundoentregable.ui.register.RegisterScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController = navController) }
        composable("register") { RegisterScreen(navController = navController) }
        composable("home") { HomeScreen(navController = navController) }
        composable("profile") { ProfileScreen(navController = navController) }
    }
}