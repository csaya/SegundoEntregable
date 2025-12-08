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
    // 1. Observamos el estado global de la sesión
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route // 2. Siempre inicia en Home
    ) {

        // --- RUTAS PÚBLICAS ---
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
            arguments = listOf(navArgument("attractionId") { type = NavType.StringType })
        ) {
            // 3. Pasamos el estado de sesión al Detalle
            AttractionDetailScreen(
                navController = navController,
                isUserLoggedIn = isLoggedIn
            )
        }

        // --- RUTAS HÍBRIDAS / PROTEGIDAS ---

        composable(BottomBarScreen.Favoritos.route) {
            // 4. Pasamos el estado a Favoritos para mostrar UI de invitado o lista real
            FavoritesScreen(
                navController = navController,
                isUserLoggedIn = isLoggedIn
            )
        }

        composable(BottomBarScreen.Perfil.route) {
            if (isLoggedIn) {
                ProfileScreen(
                    navController = navController,
                    onLogout = {
                        sessionViewModel.logout()
                        // Al salir, vamos al Login pero limpiando el stack para seguridad
                        navController.navigate("login") { popUpTo(0) }
                    }
                )
            } else {
                // Si es invitado y toca Perfil, mostramos Login
                LoginScreen(
                    navController = navController,
                    onLoginSuccess = { sessionViewModel.login() }
                )
            }
        }

        // --- AUTH ---
        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = { sessionViewModel.login() }
            )
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }
    }
}