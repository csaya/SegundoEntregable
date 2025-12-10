package com.example.segundoentregable.data.repository

import android.location.Location
import com.example.segundoentregable.data.local.dao.ActividadDao
import com.example.segundoentregable.data.local.dao.AtractivoDao
import com.example.segundoentregable.data.local.dao.FavoritoDao
import com.example.segundoentregable.data.local.dao.GaleriaFotoDao
import com.example.segundoentregable.data.local.dao.ReviewDao
import com.example.segundoentregable.data.local.dao.ReviewVoteDao
import com.example.segundoentregable.data.local.entity.ReviewEntity
import com.example.segundoentregable.data.local.entity.ReviewVoteEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.Review
import com.example.segundoentregable.data.model.toDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttractionRepository(
    private val atractivoDao: AtractivoDao,
    private val galeriaFotoDao: GaleriaFotoDao,
    private val actividadDao: ActividadDao,
    private val favoritoDao: FavoritoDao,
    private val reviewRepository: ReviewRepository
) {

    // Ya no se necesita initializeData() porque DataImporter carga los datos desde JSON

    suspend fun getRecomendaciones(): List<AtractivoTuristico> {
        return atractivoDao.getAllAtractivos()
            .take(4)
            .map { it.toDomainModel() }
    }

    suspend fun getTodosLosAtractivos(): List<AtractivoTuristico> {
        return atractivoDao.getAllAtractivos().map { it.toDomainModel() }
    }

    suspend fun getCercanos(): List<AtractivoTuristico> {
        return atractivoDao.getAllAtractivos()
            .take(5)
            .map { it.toDomainModel() }
    }

    suspend fun getCercanosReal(userLat: Double, userLon: Double): List<AtractivoTuristico> {
        val todos = atractivoDao.getAllAtractivos()

        return todos.map { entity ->
            val model = entity.toDomainModel()

            val results = FloatArray(1)
            Location.distanceBetween(userLat, userLon, entity.latitud, entity.longitud, results)
            val distanciaMetros = results[0]

            val distanciaTexto = if (distanciaMetros < 1000) {
                "${distanciaMetros.toInt()} m"
            } else {
                String.format("%.1f km", distanciaMetros / 1000)
            }

            model.copy(distanciaTexto = distanciaTexto) to distanciaMetros
        }
            .sortedBy { it.second }
            .take(5)
            .map { it.first }
    }

    suspend fun getAtractivoPorId(id: String): AtractivoTuristico? {
        return atractivoDao.getAtractivoById(id)?.toDomainModel()
    }

    /**
     * Obtiene un atractivo completo con galería, actividades y estado de favorito.
     * Usa Flow combine para combinar los 3 streams de datos.
     */
    fun getAtractivoCompleto(atractivoId: String, userEmail: String): Flow<AtractivoTuristico?> {
        val galeriaFlow = galeriaFotoDao.getGaleriaByAtractivoId(atractivoId)
        val actividadesFlow = actividadDao.getActividadesByAtractivoId(atractivoId)

        return combine(galeriaFlow, actividadesFlow) { galeriaEntities, actividadEntities ->
            val atractivo = atractivoDao.getAtractivoById(atractivoId)
            val isFavorito = favoritoDao.isFavorito(userEmail, atractivoId) > 0

            atractivo?.toDomainModel(
                galeria = galeriaEntities.map { it.toDomainModel() },
                actividades = actividadEntities.map { it.toDomainModel() },
                isFavorito = isFavorito
            )
        }
    }

    /**
     * Calcula el rating promedio de un atractivo basado en sus reseñas
     */
    suspend fun calculateAverageRating(attractionId: String): Float {
        return reviewRepository.calculateAverageRating(attractionId)
    }

    /**
     * Versión suspend para obtener atractivo completo (sin Flow)
     */
    suspend fun getAtractivoCompletoSync(atractivoId: String, userEmail: String): AtractivoTuristico? {
        val atractivo = atractivoDao.getAtractivoById(atractivoId) ?: return null
        val galeria = galeriaFotoDao.getGaleriaByAtractivoIdSync(atractivoId)
        val actividades = actividadDao.getActividadesByAtractivoIdSync(atractivoId)
        val isFavorito = favoritoDao.isFavorito(userEmail, atractivoId) > 0

        val dynamicRating = calculateAverageRating(atractivoId)

        return atractivo.toDomainModel(
            galeria = galeria.map { it.toDomainModel() },
            actividades = actividades.map { it.toDomainModel() },
            isFavorito = isFavorito
        ).copy(rating = dynamicRating)
    }

    suspend fun getReviewsForAttraction(attractionId: String): List<Review> {
        return reviewRepository.getReviewsForAttraction(attractionId)
    }

    suspend fun searchAtractivos(query: String): List<AtractivoTuristico> {
        return atractivoDao.searchAtractivos(query).map { it.toDomainModel() }
    }

    suspend fun getAtractivosByCategoria(categoria: String): List<AtractivoTuristico> {
        return atractivoDao.getAtractivosByCategoria(categoria).map { it.toDomainModel() }
    }

    suspend fun getAllCategorias(): List<String> {
        return atractivoDao.getAllAtractivos()
            .map { it.categoria }
            .distinct()
            .sorted()
    }

    private fun ReviewEntity.toModel(): Review {
        return Review(
            id = id,
            userName = userName,
            userEmail = userEmail,
            date = date,
            rating = rating,
            comment = comment,
            likes = likes,
            dislikes = dislikes,
            createdAt = createdAt
        )
    }
}
