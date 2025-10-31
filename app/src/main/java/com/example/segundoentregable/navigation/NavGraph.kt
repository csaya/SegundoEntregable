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

import com.example.segundoentregable.ui.components.BottomBarScreen
import com.example.segundoentregable.ui.list.AttractionListScreen
import com.example.segundoentregable.ui.favorites.FavoritesScreen
import com.example.segundoentregable.ui.profile.ProfileScreen


@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route
    ) {

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
            // El ViewModel tomará el 'attractionId' automáticamente
            AttractionDetailScreen(navController = navController)
        }

        composable(BottomBarScreen.Favoritos.route) {
            FavoritesScreen(navController = navController)
        }

        composable(BottomBarScreen.Perfil.route) {
            ProfileScreen(navController = navController)
        }
    }
}