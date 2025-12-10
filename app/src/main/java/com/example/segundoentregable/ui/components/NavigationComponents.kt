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
    object Home : BottomBarScreen("home", "Home", Icons.Filled.Home)
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
    val mapOrigin = navBackStackEntry?.arguments?.getString("origin")
    val detailOrigin = navBackStackEntry?.arguments?.getString("origin")
    val routeIds = navBackStackEntry?.arguments?.getString("routeIds")
    
    // Log para debug
    Log.d(TAG, "currentRoute=$currentRoute, mapOrigin=$mapOrigin, routeIds=$routeIds")

    NavigationBar {
        bottomBarScreens.forEach { screen ->
            // Determinar si el mapa viene de rutas/planner (parte de Home)
            val isMapFromRoutes = currentRoute?.startsWith("mapa") == true && !routeIds.isNullOrBlank()
            
            val isSelected = when (screen) {
                BottomBarScreen.Home -> {
                    currentRoute == "home" ||
                            currentRoute?.startsWith("rutas") == true ||
                            currentRoute?.startsWith("planner") == true ||
                            currentRoute == "mis_rutas" ||
                            (currentRoute?.startsWith("detail/") == true && detailOrigin == "home") ||
                            (currentRoute?.startsWith("list") == true) ||
                            (currentRoute?.startsWith("mapa") == true && (mapOrigin == "home" || isMapFromRoutes))
                }
                BottomBarScreen.Mapa -> {
                    currentRoute?.startsWith("mapa") == true &&
                            mapOrigin.isNullOrBlank() && routeIds.isNullOrBlank() ||
                            (currentRoute?.startsWith("detail/") == true && detailOrigin == "mapa")
                }
                BottomBarScreen.Favoritos -> {
                    currentRoute == "favoritos" ||
                            (currentRoute?.startsWith("detail/") == true && detailOrigin == "favoritos") ||
                            (currentRoute?.startsWith("mapa") == true && mapOrigin == "favoritos")
                }
                BottomBarScreen.Perfil -> {
                    currentRoute == "perfil" || currentRoute == "login"
                }
            }
            
            Log.d(TAG, "screen=${screen.route}, isSelected=$isSelected, isMapFromRoutes=$isMapFromRoutes")

            val isAtRoot = when (screen) {
                BottomBarScreen.Home -> currentRoute == "home"
                BottomBarScreen.Mapa -> currentRoute == "mapa" && mapOrigin.isNullOrBlank() && routeIds.isNullOrBlank()
                BottomBarScreen.Favoritos -> currentRoute == "favoritos"
                BottomBarScreen.Perfil -> currentRoute == "perfil"
            }

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    Log.d(TAG, "onClick: ${screen.route}, isSelected=$isSelected, isAtRoot=$isAtRoot")
                    
                    // SIEMPRE navegar limpiamente sin guardar estado del mapa
                    // Esto evita congelamientos por estados inválidos
                    navController.navigate(screen.route) {
                        // Limpiar todo el backstack hasta el inicio
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false  // NO guardar estado - causa congelamientos
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = false  // NO restaurar estado - causa congelamientos
                    }
                }
            )
        }
    }
}
