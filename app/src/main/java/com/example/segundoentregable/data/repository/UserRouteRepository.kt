package com.example.segundoentregable.data.repository

import com.example.segundoentregable.data.local.dao.UserRouteDao
import com.example.segundoentregable.data.local.entity.UserRouteItemEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRouteRepository(
    private val userRouteDao: UserRouteDao
) {
    /**
     * Añade un atractivo a la ruta del usuario
     */
    suspend fun addToRoute(atractivoId: String) {
        val item = UserRouteItemEntity(
            atractivoId = atractivoId,
            addedAt = System.currentTimeMillis()
        )
        userRouteDao.addToRoute(item)
    }

    /**
     * Elimina un atractivo de la ruta
     */
    suspend fun removeFromRoute(atractivoId: String) {
        userRouteDao.removeFromRoute(atractivoId)
    }

    /**
     * Alterna el estado de un atractivo en la ruta (añadir/quitar)
     * @return true si se añadió, false si se quitó
     */
    suspend fun toggleInRoute(atractivoId: String): Boolean {
        val isInRoute = userRouteDao.isInRoute(atractivoId)
        if (isInRoute) {
            userRouteDao.removeFromRoute(atractivoId)
            return false
        } else {
            addToRoute(atractivoId)
            return true
        }
    }

    /**
     * Verifica si un atractivo está en la ruta
     */
    suspend fun isInRoute(atractivoId: String): Boolean {
        return userRouteDao.isInRoute(atractivoId)
    }

    /**
     * Flow que indica si un atractivo está en la ruta
     */
    fun isInRouteFlow(atractivoId: String): Flow<Boolean> {
        return userRouteDao.isInRouteFlow(atractivoId)
    }

    /**
     * Flow con la cantidad de items en la ruta
     */
    fun getRouteCountFlow(): Flow<Int> {
        return userRouteDao.getRouteCountFlow()
    }

    /**
     * Obtiene la cantidad de items en la ruta
     */
    suspend fun getRouteCount(): Int {
        return userRouteDao.getRouteCount()
    }

    /**
     * Limpia toda la ruta
     */
    suspend fun clearRoute() {
        userRouteDao.clearRoute()
    }

    /**
     * Obtiene los atractivos de la ruta como Flow
     */
    fun getRouteAtractivosFlow(): Flow<List<AtractivoTuristico>> {
        return userRouteDao.getRouteAtractivosFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Obtiene los atractivos de la ruta (suspend)
     */
    suspend fun getRouteAtractivos(): List<AtractivoTuristico> {
        return userRouteDao.getRouteAtractivos().map { it.toDomainModel() }
    }

    /**
     * Actualiza el orden de los items después de optimizar
     */
    suspend fun updateRouteOrder(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, atractivoId ->
            userRouteDao.updateOrder(atractivoId, index)
        }
    }

    /**
     * Crea una ruta desde una lista de IDs de atractivos (desde favoritos)
     */
    suspend fun createRouteFromAtractivos(atractivoIds: List<String>) {
        // Limpiar ruta actual
        userRouteDao.clearRoute()
        
        // Añadir los nuevos items
        val items = atractivoIds.mapIndexed { index, id ->
            UserRouteItemEntity(
                atractivoId = id,
                addedAt = System.currentTimeMillis(),
                orderIndex = index
            )
        }
        userRouteDao.addMultipleToRoute(items)
    }
}
