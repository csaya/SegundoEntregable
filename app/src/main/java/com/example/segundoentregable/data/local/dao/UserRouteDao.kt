package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.UserRouteItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToRoute(item: UserRouteItemEntity)

    @Query("DELETE FROM user_route_items WHERE atractivoId = :atractivoId")
    suspend fun removeFromRoute(atractivoId: String)

    @Query("DELETE FROM user_route_items")
    suspend fun clearRoute()

    @Query("SELECT EXISTS(SELECT 1 FROM user_route_items WHERE atractivoId = :atractivoId)")
    suspend fun isInRoute(atractivoId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM user_route_items WHERE atractivoId = :atractivoId)")
    fun isInRouteFlow(atractivoId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM user_route_items")
    fun getRouteCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_route_items")
    suspend fun getRouteCount(): Int

    @Query("SELECT * FROM user_route_items ORDER BY orderIndex ASC, addedAt ASC")
    fun getAllRouteItemsFlow(): Flow<List<UserRouteItemEntity>>

    @Query("SELECT * FROM user_route_items ORDER BY orderIndex ASC, addedAt ASC")
    suspend fun getAllRouteItems(): List<UserRouteItemEntity>

    /**
     * Obtiene los atractivos completos que están en la ruta del usuario
     */
    @Query("""
        SELECT a.* FROM atractivos a 
        INNER JOIN user_route_items uri ON a.id = uri.atractivoId 
        ORDER BY uri.orderIndex ASC, uri.addedAt ASC
    """)
    fun getRouteAtractivosFlow(): Flow<List<AtractivoEntity>>

    @Query("""
        SELECT a.* FROM atractivos a 
        INNER JOIN user_route_items uri ON a.id = uri.atractivoId 
        ORDER BY uri.orderIndex ASC, uri.addedAt ASC
    """)
    suspend fun getRouteAtractivos(): List<AtractivoEntity>

    /**
     * Actualiza el orden de un item después de optimizar
     */
    @Query("UPDATE user_route_items SET orderIndex = :newOrder WHERE atractivoId = :atractivoId")
    suspend fun updateOrder(atractivoId: String, newOrder: Int)

    /**
     * Inserta múltiples items (para crear ruta desde favoritos)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMultipleToRoute(items: List<UserRouteItemEntity>)
}
