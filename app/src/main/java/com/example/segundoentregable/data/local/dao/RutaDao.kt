package com.example.segundoentregable.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.local.entity.RutaParadaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RutaDao {

    // ========== RUTAS ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuta(ruta: RutaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRutas(rutas: List<RutaEntity>)

    @Query("SELECT * FROM rutas ORDER BY orden ASC")
    fun getAllRutas(): Flow<List<RutaEntity>>
    
    @Query("SELECT * FROM rutas ORDER BY orden ASC")
    suspend fun getAllRutasList(): List<RutaEntity>

    @Query("SELECT * FROM rutas WHERE id = :rutaId")
    suspend fun getRutaById(rutaId: String): RutaEntity?

    @Query("SELECT * FROM rutas WHERE categoria = :categoria ORDER BY orden ASC")
    suspend fun getRutasByCategoria(categoria: String): List<RutaEntity>

    @Query("SELECT DISTINCT categoria FROM rutas")
    suspend fun getCategorias(): List<String>

    // ========== PARADAS ==========
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParada(parada: RutaParadaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParadas(paradas: List<RutaParadaEntity>)

    @Query("SELECT * FROM ruta_paradas WHERE rutaId = :rutaId ORDER BY orden ASC")
    suspend fun getParadasByRuta(rutaId: String): List<RutaParadaEntity>

    @Query("DELETE FROM ruta_paradas WHERE rutaId = :rutaId")
    suspend fun deleteParadasByRuta(rutaId: String)

    // ========== CONSULTAS COMBINADAS ==========
    
    /**
     * Obtener los atractivos de una ruta en orden
     */
    @Query("""
        SELECT a.* FROM atractivos a
        INNER JOIN ruta_paradas rp ON a.id = rp.atractivoId
        WHERE rp.rutaId = :rutaId
        ORDER BY rp.orden ASC
    """)
    suspend fun getAtractivosByRuta(rutaId: String): List<AtractivoEntity>
    
    /**
     * Contar paradas de una ruta
     */
    @Query("SELECT COUNT(*) FROM ruta_paradas WHERE rutaId = :rutaId")
    suspend fun countParadas(rutaId: String): Int

    // ========== RUTAS DE USUARIO ==========

    @Query("SELECT * FROM rutas WHERE tipo = 'usuario' AND userId = :userId ORDER BY updatedAt DESC")
    fun getUserRoutes(userId: String): Flow<List<RutaEntity>>

    @Query("SELECT * FROM rutas WHERE tipo = 'usuario' AND userId = :userId ORDER BY updatedAt DESC")
    suspend fun getUserRoutesList(userId: String): List<RutaEntity>

    @Query("SELECT * FROM rutas WHERE tipo = 'predefinida' ORDER BY orden ASC")
    fun getPredefinedRoutes(): Flow<List<RutaEntity>>

    @Query("DELETE FROM rutas WHERE id = :rutaId AND tipo = 'usuario'")
    suspend fun deleteUserRoute(rutaId: String)

    @Query("UPDATE rutas SET nombre = :nombre, descripcion = :descripcion, updatedAt = :updatedAt WHERE id = :rutaId")
    suspend fun updateRoute(rutaId: String, nombre: String, descripcion: String, updatedAt: Long)

    @Transaction
    suspend fun saveUserRouteWithParadas(ruta: RutaEntity, paradas: List<RutaParadaEntity>) {
        insertRuta(ruta)
        deleteParadasByRuta(ruta.id)
        insertParadas(paradas)
    }
    
    // ========== SINCRONIZACIÓN ==========
    
    @Query("SELECT * FROM rutas WHERE tipo = 'usuario' AND isSynced = 0")
    suspend fun getUnsyncedUserRoutes(): List<RutaEntity>
    
    @Query("UPDATE rutas SET isSynced = 1 WHERE id = :rutaId")
    suspend fun markAsSynced(rutaId: String)
    
    @Query("UPDATE rutas SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markMultipleAsSynced(ids: List<String>)
}
