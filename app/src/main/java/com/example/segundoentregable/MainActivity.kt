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
import com.example.segundoentregable.ui.theme.SegundoEntregableTheme
import com.example.segundoentregable.utils.DeepLinkHandler

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val sessionViewModel: SessionViewModel by viewModels {
        SessionViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            SegundoEntregableTheme {
                val app = application as AppApplication
                val navController = rememberNavController()
                val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        FavoriteSyncWorker.syncNow(app)
                        RutaSyncWorker.syncNow(app)
                        ReviewSyncWorker.syncNow(app)
                    }
                }

                AppNavGraph(
                    navController = navController,
                    sessionViewModel = sessionViewModel
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
