package com.example.segundoentregable.data.repository

import com.example.segundoentregable.data.local.dao.RutaDao
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.local.entity.RutaParadaEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar rutas turísticas curadas.
 */
class RutaRepository(
    private val rutaDao: RutaDao
) {
    /**
     * Obtener todas las rutas como Flow
     */
    fun getAllRutas(): Flow<List<RutaEntity>> = rutaDao.getAllRutas()
    
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
