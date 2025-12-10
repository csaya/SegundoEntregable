package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.SavedRouteEntity
import com.example.segundoentregable.data.local.entity.SavedRouteItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: SavedRouteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteItems(items: List<SavedRouteItemEntity>)

    @Update
    suspend fun updateRoute(route: SavedRouteEntity)

    @Query("DELETE FROM saved_routes WHERE id = :routeId")
    suspend fun deleteRoute(routeId: String)

    @Query("DELETE FROM saved_route_items WHERE routeId = :routeId")
    suspend fun deleteRouteItems(routeId: String)

    @Query("SELECT * FROM saved_routes ORDER BY updatedAt DESC")
    fun getAllRoutesFlow(): Flow<List<SavedRouteEntity>>

    @Query("SELECT * FROM saved_routes ORDER BY updatedAt DESC")
    suspend fun getAllRoutes(): List<SavedRouteEntity>

    @Query("SELECT * FROM saved_routes WHERE id = :routeId")
    suspend fun getRouteById(routeId: String): SavedRouteEntity?

    @Query("SELECT * FROM saved_route_items WHERE routeId = :routeId ORDER BY orderIndex ASC")
    suspend fun getRouteItems(routeId: String): List<SavedRouteItemEntity>

    @Query("""
        SELECT a.* FROM atractivos a
        INNER JOIN saved_route_items sri ON a.id = sri.atractivoId
        WHERE sri.routeId = :routeId
        ORDER BY sri.orderIndex ASC
    """)
    suspend fun getRouteAtractivos(routeId: String): List<AtractivoEntity>

    @Query("""
        SELECT a.* FROM atractivos a
        INNER JOIN saved_route_items sri ON a.id = sri.atractivoId
        WHERE sri.routeId = :routeId
        ORDER BY sri.orderIndex ASC
    """)
    fun getRouteAtractivosFlow(routeId: String): Flow<List<AtractivoEntity>>

    @Query("SELECT COUNT(*) FROM saved_routes")
    suspend fun getRoutesCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM saved_routes WHERE nombre = :nombre)")
    suspend fun routeNameExists(nombre: String): Boolean

    @Transaction
    suspend fun saveRouteWithItems(route: SavedRouteEntity, items: List<SavedRouteItemEntity>) {
        insertRoute(route)
        deleteRouteItems(route.id)
        insertRouteItems(items)
    }
}
