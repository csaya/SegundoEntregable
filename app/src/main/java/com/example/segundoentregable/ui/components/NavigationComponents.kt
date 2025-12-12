package com.example.segundoentregable.ui.components

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomBarScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomBarScreen("home", "Inicio", Icons.Filled.Home)
    object Mapa : BottomBarScreen("mapa", "Mapa", Icons.Filled.Map)
    object Favoritos : BottomBarScreen("favoritos", "Favoritos", Icons.Filled.Favorite)
    object Perfil : BottomBarScreen("perfil", "Perfil", Icons.Filled.Person)
}

private val bottomBarScreens = listOf(
    BottomBarScreen.Home,
    BottomBarScreen.Mapa,
    BottomBarScreen.Favoritos,
    BottomBarScreen.Perfil,
)

private const val TAG = "AppBottomBar"

@Composable
fun AppBottomBar(navController: NavController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Obtener origin desde los argumentos
    val origin = navBackStackEntry?.arguments?.getString("origin")
    val routeIds = navBackStackEntry?.arguments?.getString("routeIds")

    // Log para debug
    Log.d(TAG, "currentRoute=$currentRoute, origin=$origin, routeIds=$routeIds")

    NavigationBar {
        bottomBarScreens.forEach { screen ->
            // Determinar si el mapa viene de rutas/planner (parte de Home)
            val isMapFromRoutes = currentRoute?.startsWith("mapa") == true && !routeIds.isNullOrBlank()

            val isSelected = when (screen) {
                BottomBarScreen.Home -> {
                    currentRoute == "home" ||
                            currentRoute?.startsWith("rutas") == true ||
                            currentRoute?.startsWith("planner") == true ||
                            (currentRoute?.startsWith("mis_rutas") == true && origin != "perfil") ||
                            (currentRoute?.startsWith("detail/") == true && origin == "home") ||
                            (currentRoute?.startsWith("list") == true) ||
                            (currentRoute?.startsWith("mapa") == true && (origin == "home" || isMapFromRoutes))
                }
                BottomBarScreen.Mapa -> {
                    currentRoute?.startsWith("mapa") == true &&
                            origin.isNullOrBlank() && routeIds.isNullOrBlank() ||
                            (currentRoute?.startsWith("detail/") == true && origin == "mapa")
                }
                BottomBarScreen.Favoritos -> {
                    (currentRoute?.startsWith("favoritos") == true && origin != "perfil") ||
                            (currentRoute?.startsWith("detail/") == true && origin == "favoritos") ||
                            (currentRoute?.startsWith("mapa") == true && origin == "favoritos")
                }
                BottomBarScreen.Perfil -> {
                    currentRoute == "perfil" ||
                            currentRoute == "login" ||
                            (currentRoute?.startsWith("favoritos") == true && origin == "perfil") ||
                            (currentRoute?.startsWith("mis_rutas") == true && origin == "perfil")
                }
            }

            Log.d(TAG, "screen=${screen.route}, isSelected=$isSelected, isMapFromRoutes=$isMapFromRoutes")

            val isAtRoot = when (screen) {
                BottomBarScreen.Home -> currentRoute == "home"
                BottomBarScreen.Mapa -> currentRoute == "mapa" && origin.isNullOrBlank() && routeIds.isNullOrBlank()
                BottomBarScreen.Favoritos -> currentRoute == "favoritos" && origin != "perfil"
                BottomBarScreen.Perfil -> currentRoute == "perfil"
            }

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    Log.d(TAG, "onClick: ${screen.route}, isSelected=$isSelected, isAtRoot=$isAtRoot")

                    if (isSelected && isAtRoot) {
                        // Ya estás en la raíz de la pantalla, no hagas nada
                        return@NavigationBarItem
                    }

                    if (isSelected && !isAtRoot) {
                        // Estás en la pantalla pero no en la raíz, vuelve a la raíz
                        navController.navigate(screen.route) {
                            popUpTo(screen.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        return@NavigationBarItem
                    }

                    // Si no estás en la pantalla, navega normalmente
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
