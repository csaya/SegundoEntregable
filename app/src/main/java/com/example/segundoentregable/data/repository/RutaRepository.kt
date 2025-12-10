package com.example.segundoentregable.data.repository

import android.content.Context
import android.util.Log
import com.example.segundoentregable.data.firebase.FirestoreRutaService
import com.example.segundoentregable.data.local.dao.RutaDao
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.local.entity.RutaParadaEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.sync.RutaSyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val TAG = "RutaRepository"

/**
 * Repositorio para gestionar rutas turísticas.
 * Soporta tanto rutas predefinidas como rutas de usuario.
 */
class RutaRepository(
    private val rutaDao: RutaDao,
    private val context: Context? = null
) {
    private val firestoreService = FirestoreRutaService()
    /**
     * Obtener todas las rutas como Flow
     */
    fun getAllRutas(): Flow<List<RutaEntity>> = rutaDao.getAllRutas()

    /**
     * Obtener solo rutas predefinidas
     */
    fun getPredefinedRoutes(): Flow<List<RutaEntity>> = rutaDao.getPredefinedRoutes()
    
    /**
     * Obtener todas las rutas (suspending)
     */
    suspend fun getAllRutasList(): List<RutaEntity> = rutaDao.getAllRutasList()
    
    /**
     * Obtener una ruta por ID
     */
    suspend fun getRutaById(rutaId: String): RutaEntity? = rutaDao.getRutaById(rutaId)
    
    /**
     * Obtener rutas por categoría
     */
    suspend fun getRutasByCategoria(categoria: String): List<RutaEntity> = 
        rutaDao.getRutasByCategoria(categoria)
    
    /**
     * Obtener categorías disponibles
     */
    suspend fun getCategorias(): List<String> = rutaDao.getCategorias()
    
    /**
     * Obtener los atractivos de una ruta en orden
     */
    suspend fun getAtractivosByRuta(rutaId: String): List<AtractivoTuristico> {
        return rutaDao.getAtractivosByRuta(rutaId).map { it.toModel() }
    }
    
    /**
     * Obtener paradas de una ruta
     */
    suspend fun getParadasByRuta(rutaId: String): List<RutaParadaEntity> =
        rutaDao.getParadasByRuta(rutaId)
    
    /**
     * Contar paradas de una ruta
     */
    suspend fun countParadas(rutaId: String): Int = rutaDao.countParadas(rutaId)
    
    /**
     * Insertar rutas (para seeding)
     */
    suspend fun insertRutas(rutas: List<RutaEntity>) = rutaDao.insertRutas(rutas)
    
    /**
     * Insertar paradas (para seeding)
     */
    suspend fun insertParadas(paradas: List<RutaParadaEntity>) = rutaDao.insertParadas(paradas)

    // ========== RUTAS DE USUARIO ==========

    /**
     * Obtener rutas de un usuario como Flow
     */
    fun getUserRoutes(userId: String): Flow<List<RutaEntity>> = rutaDao.getUserRoutes(userId)

    /**
     * Obtener rutas de un usuario (suspending)
     */
    suspend fun getUserRoutesList(userId: String): List<RutaEntity> = rutaDao.getUserRoutesList(userId)

    /**
     * Guardar una ruta de usuario
     */
    suspend fun saveUserRoute(
        userId: String,
        nombre: String,
        descripcion: String,
        atractivos: List<AtractivoTuristico>,
        distanciaTotal: Float = 0f,
        tiempoEstimadoMinutos: Int = 0
    ): String {
        val routeId = java.util.UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val ruta = RutaEntity(
            id = routeId,
            nombre = nombre,
            descripcion = descripcion,
            distanciaTotal = distanciaTotal,
            tipo = RutaEntity.TIPO_USUARIO,
            userId = userId,
            createdAt = now,
            updatedAt = now,
            tiempoEstimadoMinutos = tiempoEstimadoMinutos,
            isSynced = false
        )

        val paradas = atractivos.mapIndexed { index, atractivo ->
            RutaParadaEntity(
                id = "${routeId}_$index",
                rutaId = routeId,
                atractivoId = atractivo.id,
                orden = index,
                tiempoSugerido = ""
            )
        }

        rutaDao.saveUserRouteWithParadas(ruta, paradas)
        
        // Disparar sincronización
        context?.let { RutaSyncWorker.syncNow(it) }
        
        return routeId
    }

    /**
     * Eliminar una ruta de usuario
     */
    suspend fun deleteUserRoute(rutaId: String) {
        rutaDao.deleteParadasByRuta(rutaId)
        rutaDao.deleteUserRoute(rutaId)
        
        // Eliminar de Firebase
        withContext(Dispatchers.IO) {
            try {
                firestoreService.deleteRuta(rutaId)
                Log.d(TAG, "Ruta eliminada de Firebase: $rutaId")
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando ruta de Firebase: ${e.message}")
            }
        }
    }

    /**
     * Actualizar nombre/descripción de una ruta
     */
    suspend fun updateUserRoute(rutaId: String, nombre: String, descripcion: String) {
        rutaDao.updateRoute(rutaId, nombre, descripcion, System.currentTimeMillis())
    }
    
    /**
     * Extensión para convertir Entity a Model
     */
    private fun AtractivoEntity.toModel(): AtractivoTuristico {
        return AtractivoTuristico(
            id = id,
            codigoMincetur = codigoMincetur,
            nombre = nombre,
            descripcionCorta = descripcionCorta,
            descripcionLarga = descripcionLarga,
            ubicacion = ubicacion,
            latitud = latitud,
            longitud = longitud,
            departamento = departamento,
            provincia = provincia,
            distrito = distrito,
            altitud = altitud,
            categoria = categoria,
            tipo = tipo,
            subtipo = subtipo,
            jerarquia = jerarquia,
            precio = precio,
            precioDetalle = precioDetalle,
            horario = horario,
            horarioDetallado = horarioDetallado,
            epocaVisita = epocaVisita,
            tiempoVisitaSugerido = tiempoVisitaSugerido,
            estadoActual = estadoActual,
            observaciones = observaciones,
            tieneAccesibilidad = tieneAccesibilidad,
            imagenPrincipal = imagenPrincipal,
            rating = rating,
            distanciaTexto = distanciaTexto
        )
    }
}
