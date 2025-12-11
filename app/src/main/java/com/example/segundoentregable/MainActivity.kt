package com.example.segundoentregable

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.segundoentregable.data.sync.FavoriteSyncWorker
import com.example.segundoentregable.data.sync.ReviewSyncWorker
import com.example.segundoentregable.data.sync.RutaSyncWorker
import com.example.segundoentregable.navigation.AppNavGraph
import com.example.segundoentregable.ui.session.SessionViewModel
import com.example.segundoentregable.ui.session.SessionViewModelFactory
import com.example.segundoentregable.ui.theme.ArequipaExplorerTheme
import com.example.segundoentregable.utils.DeepLinkHandler
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.segundoentregable.ui.theme.ThemePreference
import com.example.segundoentregable.ui.theme.loadThemePreference
import com.example.segundoentregable.ui.theme.saveThemePreference

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val sessionViewModel: SessionViewModel by viewModels {
        SessionViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            val context = LocalContext.current
            var themePref by remember { mutableStateOf(loadThemePreference(context)) }

            ArequipaExplorerTheme(themePreference = themePref) {
                val app = application as AppApplication
                val navController = rememberNavController()
                val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

                // ✅ ELIMINADO: auto-login desde Firebase
                // La app siempre arranca como "no logueado"
                // Solo se marca como logueado tras pasar por LoginScreen

                LaunchedEffect(Unit) {
                    val firebaseAuth = app.userRepository.getFirebaseAuthService()

                    // ✅ Limpiar estado persistente de Firebase y SharedPrefs
                    // para que no interfiera con la lógica de la app
                    if (firebaseAuth.isAuthenticated()) {
                        // Firebase tiene un usuario, pero nosotros NO lo consideramos logueado
                        // hasta que pase por login explícito
                        Log.d(TAG, "⚠️ Firebase tiene sesión persistente, pero se ignora hasta login explícito")
                    }

                    // Limpiar SharedPrefs para evitar inconsistencias
                    app.userRepository.clearCurrentUser()
                }

                // Sincronizar SOLO si está realmente logueado
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        FavoriteSyncWorker.syncNow(app)
                        RutaSyncWorker.syncNow(app)
                        ReviewSyncWorker.syncNow(app)
                    }
                }

                AppNavGraph(
                    navController = navController,
                    sessionViewModel = sessionViewModel,
                    onThemeChange = { newPref ->
                        themePref = newPref
                        saveThemePreference(context, newPref)
                    }
                )
            }
        }
    }

    /**
     * Manejar nuevos intents cuando la app ya está en foreground
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * Procesar intents de notificaciones y extraer attraction_id
     */
    private fun handleIntent(intent: Intent?) {
        val attractionId = intent?.getStringExtra("attraction_id")
        if (attractionId != null) {
            Log.d(TAG, "🔔 Deep link detectado: attraction_id=$attractionId")
            DeepLinkHandler.setAttractionId(attractionId)
        }
    }
}
