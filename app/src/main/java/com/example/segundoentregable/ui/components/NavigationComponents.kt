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
    object Mapa : BottomBarScreen("mapa?focusId={focusId}", "Mapa", Icons.Filled.Map)  // ✅ Con parámetro
    object Favoritos : BottomBarScreen("favoritos", "Favoritos", Icons.Filled.Favorite)
    object Perfil : BottomBarScreen("perfil", "Perfil", Icons.Filled.Person)
}

private val bottomBarScreens = listOf(
    BottomBarScreen.Home,
    BottomBarScreen.Mapa,
    BottomBarScreen.Favoritos,
    BottomBarScreen.Perfil,
)

/**
 * AppBottomBar con navegación que respeta el origen de pantallas compartidas
 */
@Composable
fun AppBottomBar(navController: NavController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val previousBackStackEntry = navController.previousBackStackEntry
    val previousRoute = previousBackStackEntry?.destination?.route

    NavigationBar {
        bottomBarScreens.forEach { screen ->
            val isSelected = when (screen) {
                BottomBarScreen.Home -> {
                    currentRoute == "home" ||
                            currentRoute?.startsWith("rutas") == true ||
                            currentRoute?.startsWith("planner") == true ||
                            (currentRoute?.startsWith("detail/") == true && previousRoute == "home") ||
                            (currentRoute?.startsWith("list") == true && previousRoute == "home")
                }
                BottomBarScreen.Mapa -> {
                    // ✅ Ahora la ruta siempre es "mapa?focusId={focusId}"
                    currentRoute == "mapa?focusId={focusId}" ||
                            (currentRoute?.startsWith("detail/") == true &&
                                    previousRoute == "mapa?focusId={focusId}")
                }
                BottomBarScreen.Favoritos -> {
                    currentRoute == "favoritos" ||
                            (currentRoute?.startsWith("detail/") == true && previousRoute == "favoritos")
                }
                BottomBarScreen.Perfil -> {
                    currentRoute == "perfil"
                }
            }

            // ✅ Detectar raíz: para Mapa, verificar que focusId esté vacío
            val isAtRoot = when (screen) {
                BottomBarScreen.Home -> currentRoute == "home"
                BottomBarScreen.Mapa -> {
                    val focusId = navBackStackEntry?.arguments?.getString("focusId")
                    currentRoute == "mapa?focusId={focusId}" && focusId.isNullOrBlank()
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
                        // ✅ Navegar a la ruta (con o sin parámetro)
                        val targetRoute = when (screen) {
                            BottomBarScreen.Mapa -> "mapa?focusId="  // ✅ focusId vacío
                            else -> screen.route.split("?").first()  // Solo la base sin parámetros
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