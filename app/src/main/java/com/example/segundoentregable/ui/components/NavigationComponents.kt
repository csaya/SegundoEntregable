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
    object Mapa : BottomBarScreen("mapa?focusId={focusId}&origin={origin}", "Mapa", Icons.Filled.Map)
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

    // ✅ Obtener el parámetro origin si estamos en mapa o detail
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
                            // ✅ Si estoy en Detail con origin=home
                            (currentRoute?.startsWith("detail/") == true && detailOrigin == "home") ||
                            (currentRoute?.startsWith("list") == true) ||
                            // ✅ Si estoy en Mapa con origin=home
                            (currentRoute?.startsWith("mapa?") == true && mapOrigin == "home")
                }
                BottomBarScreen.Mapa -> {
                    // Mapa seleccionado solo si NO tiene origin o si el origin es mapa
                    (currentRoute?.startsWith("mapa?") == true) &&
                            (mapOrigin.isNullOrBlank() || mapOrigin == "mapa") ||
                            // Si estoy en Detail con origin=mapa
                            (currentRoute?.startsWith("detail/") == true && detailOrigin == "mapa")
                }
                BottomBarScreen.Favoritos -> {
                    currentRoute == "favoritos" ||
                            // ✅ Si estoy en Detail con origin=favoritos
                            (currentRoute?.startsWith("detail/") == true && detailOrigin == "favoritos") ||
                            // ✅ Si estoy en Mapa con origin=favoritos
                            (currentRoute?.startsWith("mapa?") == true && mapOrigin == "favoritos")
                }
                BottomBarScreen.Perfil -> {
                    currentRoute == "perfil"
                }
            }

            // ✅ Detectar si estamos en la raíz
            val isAtRoot = when (screen) {
                BottomBarScreen.Home -> currentRoute == "home"
                BottomBarScreen.Mapa -> {
                    val focusId = navBackStackEntry?.arguments?.getString("focusId")
                    currentRoute?.startsWith("mapa?") == true && focusId.isNullOrBlank()
                }
                BottomBarScreen.Favoritos -> currentRoute == "favoritos"
                BottomBarScreen.Perfil -> currentRoute == "perfil"
            }

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        if (!isAtRoot) {
                            navController.popBackStack()
                        }
                    } else {
                        // ✅ Navegar a nueva pestaña
                        val targetRoute = when (screen) {
                            BottomBarScreen.Mapa -> "mapa?focusId=&origin="
                            else -> screen.route.split("?").firstOrNull() ?: screen.route
                        }

                        navController.navigate(targetRoute) {
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
