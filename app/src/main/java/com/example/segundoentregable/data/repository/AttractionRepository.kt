package com.example.segundoentregable.data.repository

import android.location.Location
import com.example.segundoentregable.data.local.dao.ActividadDao
import com.example.segundoentregable.data.local.dao.AtractivoDao
import com.example.segundoentregable.data.local.dao.FavoritoDao
import com.example.segundoentregable.data.local.dao.GaleriaFotoDao
import com.example.segundoentregable.data.local.dao.ReviewDao
import com.example.segundoentregable.data.local.entity.ReviewEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.Review
import com.example.segundoentregable.data.model.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AttractionRepository(
    private val atractivoDao: AtractivoDao,
    private val reviewDao: ReviewDao,
    private val galeriaFotoDao: GaleriaFotoDao,
    private val actividadDao: ActividadDao,
    private val favoritoDao: FavoritoDao
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
     * Versión suspend para obtener atractivo completo (sin Flow)
     */
    suspend fun getAtractivoCompletoSync(atractivoId: String, userEmail: String): AtractivoTuristico? {
        val atractivo = atractivoDao.getAtractivoById(atractivoId) ?: return null
        val galeria = galeriaFotoDao.getGaleriaByAtractivoIdSync(atractivoId)
        val actividades = actividadDao.getActividadesByAtractivoIdSync(atractivoId)
        val isFavorito = favoritoDao.isFavorito(userEmail, atractivoId) > 0

        return atractivo.toDomainModel(
            galeria = galeria.map { it.toDomainModel() },
            actividades = actividades.map { it.toDomainModel() },
            isFavorito = isFavorito
        )
    }

    suspend fun getReviewsForAttraction(attractionId: String): List<Review> {
        return reviewDao.getReviewsByAttraction(attractionId).map { it.toModel() }
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

    suspend fun addReview(attractionId: String, userEmail: String, userName: String, rating: Float, comment: String) {
        val newReview = ReviewEntity(
            id = java.util.UUID.randomUUID().toString(),
            attractionId = attractionId,
            userName = userName,
            date = "Hace un momento",
            rating = rating,
            comment = comment,
            likes = 0,
            dislikes = 0
        )
        reviewDao.insertReviews(listOf(newReview))
    }
    
    // ========== CALIFICACIÓN DE RESEÑAS ==========
    
    suspend fun likeReview(reviewId: String) {
        reviewDao.incrementLikes(reviewId)
    }
    
    suspend fun dislikeReview(reviewId: String) {
        reviewDao.incrementDislikes(reviewId)
    }
    
    // ========== EDICIÓN/ELIMINACIÓN DE RESEÑAS ==========
    
    suspend fun updateReview(reviewId: String, rating: Float, comment: String) {
        reviewDao.updateReview(reviewId, rating, comment)
    }
    
    suspend fun deleteReview(reviewId: String) {
        reviewDao.deleteReview(reviewId)
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
