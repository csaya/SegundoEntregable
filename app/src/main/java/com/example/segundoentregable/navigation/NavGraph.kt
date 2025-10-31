package com.example.segundoentregable.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.segundoentregable.ui.home.HomeScreen
import com.example.segundoentregable.ui.map.MapScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.segundoentregable.ui.detail.AttractionDetailScreen
// Importamos los componentes de navegación que creamos
import com.example.segundoentregable.ui.components.BottomBarScreen
import com.example.segundoentregable.ui.list.AttractionListScreen

// --- Importa aquí tus otras pantallas (Favoritos, Perfil) cuando las crees ---
// import com.example.segundoentregable.ui.favorites.FavoritesScreen
// import com.example.segundoentregable.ui.profile.ProfileScreen


@Composable
fun AppNavGraph(navController: NavHostController) {

    // Cambiamos 'startDestination' a la ruta de Home
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route // "home"
    ) {

        // --- Ruta de la Pantalla de Inicio ---
        composable(BottomBarScreen.Home.route) {
            HomeScreen(navController = navController)
        }

        // --- Ruta de la Pantalla de Mapa (¡NUEVA!) ---
        composable(BottomBarScreen.Mapa.route) {
            MapScreen(navController = navController)
        }

        composable("list") {
            AttractionListScreen(navController = navController)
        }


        composable(
            route = "detail/{attractionId}", // Ruta con un argumento
            arguments = listOf(navArgument("attractionId") {
                type = NavType.StringType // Definimos el tipo de argumento
            })
        ) {
            // El ViewModel tomará el 'attractionId' automáticamente
            AttractionDetailScreen(navController = navController)
        }
        // --- Marcadores de posición para las otras pantallas ---

        composable(BottomBarScreen.Favoritos.route) {
            // Cuando crees FavoritesScreen, ponla aquí
            // FavoritesScreen(navController = navController)
        }

        composable(BottomBarScreen.Perfil.route) {
            // Cuando crees ProfileScreen, ponla aquí
            // ProfileScreen(navController = navController)
        }

        // --- (Opcional) Puedes dejar las rutas de login aquí si las necesitas ---
        // composable("login") { ... }
        // composable("register") { ... }
    }
}