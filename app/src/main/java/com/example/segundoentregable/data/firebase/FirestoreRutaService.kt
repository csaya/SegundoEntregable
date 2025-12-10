package com.example.segundoentregable.data.firebase

import android.util.Log
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.local.entity.RutaParadaEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreRutaService"

/**
 * Servicio para sincronizar rutas de usuario con Firestore.
 * Solo sincroniza rutas de tipo "usuario", no las predefinidas.
 */
class FirestoreRutaService {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val rutasCollection = firestore.collection("user_routes")
    
    /**
     * Subir una ruta a Firestore
     */
    suspend fun uploadRuta(ruta: RutaEntity, paradas: List<RutaParadaEntity>): Result<Unit> {
        return try {
            val rutaData = hashMapOf(
                "id" to ruta.id,
                "nombre" to ruta.nombre,
                "descripcion" to ruta.descripcion,
                "categoria" to ruta.categoria,
                "duracionEstimada" to ruta.duracionEstimada,
                "distanciaTotal" to ruta.distanciaTotal,
                "dificultad" to ruta.dificultad,
                "imagenPrincipal" to ruta.imagenPrincipal,
                "puntoInicio" to ruta.puntoInicio,
                "puntoFin" to ruta.puntoFin,
                "recomendaciones" to ruta.recomendaciones,
                "tipo" to ruta.tipo,
                "userId" to ruta.userId,
                "createdAt" to ruta.createdAt,
                "updatedAt" to ruta.updatedAt,
                "tiempoEstimadoMinutos" to ruta.tiempoEstimadoMinutos,
                "paradas" to paradas.map { parada ->
                    hashMapOf(
                        "id" to parada.id,
                        "rutaId" to parada.rutaId,
                        "atractivoId" to parada.atractivoId,
                        "orden" to parada.orden,
                        "tiempoSugerido" to parada.tiempoSugerido,
                        "notas" to parada.notas
                    )
                }
            )
            
            rutasCollection.document(ruta.id).set(rutaData).await()
            Log.d(TAG, "Ruta sincronizada: ${ruta.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al subir ruta: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar una ruta de Firestore
     */
    suspend fun deleteRuta(rutaId: String): Result<Unit> {
        return try {
            rutasCollection.document(rutaId).delete().await()
            Log.d(TAG, "Ruta eliminada de Firestore: $rutaId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar ruta: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener rutas de un usuario desde Firestore
     */
    suspend fun getRutasForUser(userEmail: String): Result<List<Pair<RutaEntity, List<RutaParadaEntity>>>> {
        return try {
            val snapshot = rutasCollection
                .whereEqualTo("userId", userEmail)
                .get()
                .await()
            
            val rutas = snapshot.documents.mapNotNull { doc ->
                try {
                    val ruta = RutaEntity(
                        id = doc.getString("id") ?: return@mapNotNull null,
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        categoria = doc.getString("categoria") ?: "",
                        duracionEstimada = doc.getString("duracionEstimada") ?: "",
                        distanciaTotal = (doc.getDouble("distanciaTotal") ?: 0.0).toFloat(),
                        dificultad = doc.getString("dificultad") ?: "",
                        imagenPrincipal = doc.getString("imagenPrincipal") ?: "",
                        puntoInicio = doc.getString("puntoInicio") ?: "",
                        puntoFin = doc.getString("puntoFin") ?: "",
                        recomendaciones = doc.getString("recomendaciones") ?: "",
                        tipo = doc.getString("tipo") ?: RutaEntity.TIPO_USUARIO,
                        userId = doc.getString("userId"),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        updatedAt = doc.getLong("updatedAt") ?: 0L,
                        tiempoEstimadoMinutos = (doc.getLong("tiempoEstimadoMinutos") ?: 0L).toInt(),
                        isSynced = true
                    )
                    
                    @Suppress("UNCHECKED_CAST")
                    val paradasData = doc.get("paradas") as? List<Map<String, Any>> ?: emptyList()
                    val paradas = paradasData.mapNotNull { paradaMap ->
                        try {
                            RutaParadaEntity(
                                id = paradaMap["id"] as? String ?: return@mapNotNull null,
                                rutaId = paradaMap["rutaId"] as? String ?: "",
                                atractivoId = paradaMap["atractivoId"] as? String ?: "",
                                orden = (paradaMap["orden"] as? Long)?.toInt() ?: 0,
                                tiempoSugerido = paradaMap["tiempoSugerido"] as? String ?: "",
                                notas = paradaMap["notas"] as? String ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    Pair(ruta, paradas)
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(rutas)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener rutas: ${e.message}")
            Result.failure(e)
        }
    }
}
