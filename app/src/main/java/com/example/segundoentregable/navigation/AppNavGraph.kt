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
import com.example.segundoentregable.ui.planner.PlannerScreen
import com.example.segundoentregable.ui.profile.ProfileScreen
import com.example.segundoentregable.ui.register.RegisterScreen
import com.example.segundoentregable.ui.routes.RutaDetalleScreen
import com.example.segundoentregable.ui.routes.RutasScreen
import com.example.segundoentregable.ui.session.SessionViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route
    ) {

        // --- RUTAS PÚBLICAS ---
        composable(BottomBarScreen.Home.route) {
            HomeScreen(navController = navController)
        }

        // ✅ UNA SOLA ruta para Mapa con parámetro opcional
        composable(
            route = "mapa?focusId={focusId}",
            arguments = listOf(
                navArgument("focusId") {
                    type = NavType.StringType
                    defaultValue = ""  // ✅ Valor por defecto vacío
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val focusId = backStackEntry.arguments?.getString("focusId")?.takeIf { it.isNotBlank() }
            MapScreen(
                navController = navController,
                focusAttractionId = focusId  // null si está vacío
            )
        }

        composable(
            route = "list?query={query}",
            arguments = listOf(navArgument("query") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            AttractionListScreen(
                navController = navController,
                initialQuery = query
            )
        }

        composable(
            route = "detail/{attractionId}",
            arguments = listOf(navArgument("attractionId") { type = NavType.StringType })
        ) {
            AttractionDetailScreen(
                navController = navController,
                isUserLoggedIn = isLoggedIn
            )
        }

        // --- RUTAS HÍBRIDAS / PROTEGIDAS ---

        composable(BottomBarScreen.Favoritos.route) {
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
                        navController.navigate("login") { popUpTo(0) }
                    }
                )
            } else {
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

        // --- RUTAS TURÍSTICAS ---
        composable("rutas") {
            RutasScreen(navController = navController)
        }

        composable(
            route = "ruta_detalle/{rutaId}",
            arguments = listOf(navArgument("rutaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rutaId = backStackEntry.arguments?.getString("rutaId") ?: ""
            RutaDetalleScreen(
                navController = navController,
                rutaId = rutaId
            )
        }

        // --- PLANIFICADOR PERSONAL ---
        composable("planner") {
            PlannerScreen(navController = navController)
        }
    }
}