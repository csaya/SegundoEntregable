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

/**
 * AppBottomBar con comportamiento estándar tipo Instagram/Spotify:
 * - Si cambias de pestaña: guarda estado y restaura al volver
 * - Si tocas la pestaña actual: vuelve a la raíz de esa sección (popBackStack)
 */
@Composable
fun AppBottomBar(navController: NavController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomBarScreens.forEach { screen ->
            // Determinar si esta pestaña está seleccionada
            // Considera rutas anidadas como "detail/{id}" que pertenecen a "home"
            val isSelected = when {
                currentRoute == screen.route -> true
                screen == BottomBarScreen.Home && currentRoute?.startsWith("detail") == true -> true
                screen == BottomBarScreen.Home && currentRoute?.startsWith("list") == true -> true
                screen == BottomBarScreen.Home && currentRoute?.startsWith("rutas") == true -> true
                screen == BottomBarScreen.Home && currentRoute?.startsWith("planner") == true -> true
                else -> false
            }
            
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    // SIEMPRE navegamos con el patrón estándar
                    navController.navigate(screen.route) {
                        // Pop hasta el inicio del grafo
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Evita copias múltiples si tocas rápido
                        launchSingleTop = true
                        // Restaura estado
                        restoreState = true
                    }
                    
                    // LÓGICA DE RESELECCIÓN EXPLÍCITA:
                    // Si la ruta actual YA es la de la pantalla tocada (o una sub-ruta),
                    // forzamos volver a su raíz
                    if (isSelected && currentRoute != screen.route) {
                        navController.popBackStack(screen.route, inclusive = false)
                    }
                }
            )
        }
    }
}