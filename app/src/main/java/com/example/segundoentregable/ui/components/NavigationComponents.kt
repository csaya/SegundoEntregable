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
import androidx.navigation.compose.currentBackStackEntryAsState

// 1. Definimos las rutas de nuestras pestañas
sealed class BottomBarScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomBarScreen("home", "Home", Icons.Filled.Home)
    object Mapa : BottomBarScreen("mapa", "Mapa", Icons.Filled.Map)
    object Favoritos : BottomBarScreen("favoritos", "Favoritos", Icons.Filled.Favorite)
    object Perfil : BottomBarScreen("perfil", "Perfil", Icons.Filled.Person)
}

// 2. Creamos la lista de pantallas para la barra
private val bottomBarScreens = listOf(
    BottomBarScreen.Home,
    BottomBarScreen.Mapa,
    BottomBarScreen.Favoritos,
    BottomBarScreen.Perfil,
)

// 3. Este es nuestro Composable reutilizable
@Composable
fun AppBottomBar(navController: NavController) {

    // 4. Observamos la pila de navegación para saber la ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomBarScreens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                // 5. Comparamos la ruta de la pestaña con la ruta actual
                selected = currentRoute == screen.route,
                onClick = {
                    // 6. Navegamos con 'launchSingleTop' y 'restoreState'
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}