package com.example.segundoentregable.data.repository

import com.example.segundoentregable.data.local.dao.SavedRouteDao
import com.example.segundoentregable.data.local.entity.SavedRouteEntity
import com.example.segundoentregable.data.local.entity.SavedRouteItemEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repository para gestionar rutas personalizadas guardadas.
 */
class SavedRouteRepository(
    private val savedRouteDao: SavedRouteDao
) {
    /**
     * Obtiene todas las rutas guardadas como Flow.
     */
    fun getAllRoutesFlow(): Flow<List<SavedRouteEntity>> {
        return savedRouteDao.getAllRoutesFlow()
    }

    /**
     * Obtiene todas las rutas guardadas.
     */
    suspend fun getAllRoutes(): List<SavedRouteEntity> {
        return savedRouteDao.getAllRoutes()
    }

    /**
     * Obtiene una ruta por su ID.
     */
    suspend fun getRouteById(routeId: String): SavedRouteEntity? {
        return savedRouteDao.getRouteById(routeId)
    }

    /**
     * Obtiene los atractivos de una ruta guardada.
     */
    suspend fun getRouteAtractivos(routeId: String): List<AtractivoTuristico> {
        return savedRouteDao.getRouteAtractivos(routeId).map { it.toDomainModel() }
    }

    /**
     * Obtiene los atractivos de una ruta guardada como Flow.
     */
    fun getRouteAtractivosFlow(routeId: String): Flow<List<AtractivoTuristico>> {
        return savedRouteDao.getRouteAtractivosFlow(routeId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Guarda una nueva ruta con sus atractivos.
     */
    suspend fun saveRoute(
        nombre: String,
        descripcion: String = "",
        atractivos: List<AtractivoTuristico>,
        totalDistance: Float = 0f,
        estimatedTime: Int = 0
    ): String {
        val routeId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val route = SavedRouteEntity(
            id = routeId,
            nombre = nombre,
            descripcion = descripcion,
            createdAt = now,
            updatedAt = now,
            totalDistance = totalDistance,
            estimatedTime = estimatedTime,
            itemCount = atractivos.size
        )

        val items = atractivos.mapIndexed { index, atractivo ->
            SavedRouteItemEntity(
                routeId = routeId,
                atractivoId = atractivo.id,
                orderIndex = index
            )
        }

        savedRouteDao.saveRouteWithItems(route, items)
        return routeId
    }

    /**
     * Actualiza una ruta existente.
     */
    suspend fun updateRoute(
        routeId: String,
        nombre: String,
        descripcion: String = "",
        atractivos: List<AtractivoTuristico>,
        totalDistance: Float = 0f,
        estimatedTime: Int = 0
    ) {
        val existingRoute = savedRouteDao.getRouteById(routeId) ?: return

        val updatedRoute = existingRoute.copy(
            nombre = nombre,
            descripcion = descripcion,
            updatedAt = System.currentTimeMillis(),
            totalDistance = totalDistance,
            estimatedTime = estimatedTime,
            itemCount = atractivos.size
        )

        val items = atractivos.mapIndexed { index, atractivo ->
            SavedRouteItemEntity(
                routeId = routeId,
                atractivoId = atractivo.id,
                orderIndex = index
            )
        }

        savedRouteDao.saveRouteWithItems(updatedRoute, items)
    }

    /**
     * Elimina una ruta guardada.
     */
    suspend fun deleteRoute(routeId: String) {
        savedRouteDao.deleteRoute(routeId)
    }

    /**
     * Verifica si un nombre de ruta ya existe.
     */
    suspend fun routeNameExists(nombre: String): Boolean {
        return savedRouteDao.routeNameExists(nombre)
    }

    /**
     * Obtiene la cantidad de rutas guardadas.
     */
    suspend fun getRoutesCount(): Int {
        return savedRouteDao.getRoutesCount()
    }
}
