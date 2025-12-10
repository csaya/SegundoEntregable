package com.example.segundoentregable.ui.components

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

@Composable
fun AppBottomBar(navController: NavController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mapOrigin = if (currentRoute?.startsWith("mapa?") == true) {
        navBackStackEntry?.arguments?.getString("origin")
    } else null

    val detailOrigin = if (currentRoute?.startsWith("detail/") == true) {
        navBackStackEntry?.arguments?.getString("origin")
    } else null

    NavigationBar {
        bottomBarScreens.forEach { screen ->
            val isSelected = when (screen) {
                BottomBarScreen.Home -> {
                    currentRoute == "home" ||
                            currentRoute?.startsWith("rutas") == true ||
                            currentRoute?.startsWith("planner") == true ||
                            (currentRoute?.startsWith("detail/") == true && detailOrigin == "home") ||
                            (currentRoute?.startsWith("list") == true) ||
                            (currentRoute?.startsWith("mapa") == true && mapOrigin == "home")
                }
                BottomBarScreen.Mapa -> {
                    currentRoute?.startsWith("mapa") == true &&
                            (mapOrigin.isNullOrBlank() || mapOrigin == "mapa") ||
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

            val isAtRoot = when (screen) {
                BottomBarScreen.Home -> currentRoute == "home"
                BottomBarScreen.Mapa -> currentRoute?.startsWith("mapa") == true && mapOrigin.isNullOrBlank()
                BottomBarScreen.Favoritos -> currentRoute == "favoritos"
                BottomBarScreen.Perfil -> currentRoute == "perfil"
            }

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    // Si ya estamos en esta sección
                    if (isSelected) {
                        // Si no estamos en la raíz, volver a la raíz de esta sección
                        if (!isAtRoot) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = false
                                }
                                launchSingleTop = true
                            }
                        }
                        // Si ya estamos en la raíz, no hacer nada
                    } else {
                        // Navegar a la nueva sección
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
