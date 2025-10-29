package com.example.segundoentregable.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.segundoentregable.ui.home.HomeScreen
import com.example.segundoentregable.ui.login.LoginScreen
import com.example.segundoentregable.ui.profile.ProfileScreen
import com.example.segundoentregable.ui.register.RegisterScreen
import com.example.segundoentregable.viewmodel.UserViewModel

@Composable
fun AppNavGraph(navController: NavHostController, userVM: UserViewModel) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController = navController, userVM = userVM) }
        composable("register") { RegisterScreen(navController = navController, userVM = userVM) }
        composable("home") { HomeScreen(navController = navController, userVM = userVM) }
        composable("profile") { ProfileScreen(navController = navController, userVM = userVM) }
    }
}
