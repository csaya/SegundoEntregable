package com.example.segundoentregable.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.segundoentregable.ui.components.BottomBarScreen
import com.example.segundoentregable.ui.detail.AttractionDetailScreen
import com.example.segundoentregable.ui.favorites.FavoritesScreen
import com.example.segundoentregable.ui.home.HomeScreen
import com.example.segundoentregable.ui.list.AttractionListScreen
import com.example.segundoentregable.ui.login.LoginScreen
import com.example.segundoentregable.ui.map.MapScreen
import com.example.segundoentregable.ui.profile.ProfileScreen
import com.example.segundoentregable.ui.register.RegisterScreen
import com.example.segundoentregable.ui.session.SessionViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    // Obtenemos el estado de sesión para decidir dónde iniciar
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()
    val startRoute = if (isLoggedIn) BottomBarScreen.Home.route else "login"

    NavHost(
        navController = navController,
        startDestination = startRoute // Usamos la lógica de sesión
    ) {

        // --- RUTA LOGIN (Faltaba esta ruta independiente) ---
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    sessionViewModel.login() // Ahora sí existe este método
                    // LoginScreen ya maneja la navegación a "home" internamente
                }
            )
        }

        composable(BottomBarScreen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(BottomBarScreen.Mapa.route) {
            MapScreen(navController = navController)
        }

        composable("list") {
            AttractionListScreen(navController = navController)
        }

        composable(
            route = "detail/{attractionId}",
            arguments = listOf(navArgument("attractionId") {
                type = NavType.StringType
            })
        ) {
            AttractionDetailScreen(navController = navController)
        }

        composable(BottomBarScreen.Favoritos.route) {
            FavoritesScreen(navController = navController)
        }

        composable(BottomBarScreen.Perfil.route) {
            // Lógica interna de la pestaña perfil
            if (isLoggedIn) {
                ProfileScreen(
                    navController = navController,
                    onLogout = {
                        sessionViewModel.logout()
                        // Al cerrar sesión, mandamos al usuario al login y limpiamos el historial
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            } else {
                // Si entra a perfil pero no está logueado
                LoginScreen(
                    navController = navController,
                    onLoginSuccess = {
                        sessionViewModel.login()
                    }
                )
            }
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }
    }
}