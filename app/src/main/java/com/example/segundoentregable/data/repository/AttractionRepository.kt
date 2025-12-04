package com.example.segundoentregable.data.repository

import android.content.Context
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.ReviewEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.Review

class AttractionRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val atractivoDao = db.atractivoDao()
    private val reviewDao = db.reviewDao()

    suspend fun initializeData() {
        val existingAtractivos = atractivoDao.getAllAtractivos()
        if (existingAtractivos.isEmpty()) {
            insertDefaultAtractivos()
            insertDefaultReviews()
        }
    }

    private suspend fun insertDefaultAtractivos() {
        val atractivos = listOf(
            AtractivoEntity(
                id = "a1",
                nombre = "Plaza de Armas",
                descripcionCorta = "Corazón de la ciudad",
                descripcionLarga = "La Plaza de Armas de Arequipa es famosa por su arquitectura de sillar y su imponente catedral. Un lugar de encuentro vibrante y lleno de historia.",
                ubicacion = "Centro Histórico",
                categoria = "Cultural",
                idImagen = "img_plaza_armas"
            ),
            AtractivoEntity(
                id = "a2",
                nombre = "Cañón del Colca",
                descripcionCorta = "Maravilla natural",
                descripcionLarga = "Uno de los cañones más profundos del mundo, hogar del majestuoso cóndor andino. Ideal para trekking y vistas espectaculares.",
                ubicacion = "Provincia de Caylloma",
                categoria = "Aventura",
                idImagen = "img_colca"
            ),
            AtractivoEntity(
                id = "a3",
                nombre = "Museo Santuarios Andinos",
                descripcionCorta = "Hogar de la Momia Juanita",
                descripcionLarga = "Este museo alberga a Juanita, la famosa 'Dama de Ampato', una momia inca excepcionalmente conservada descubierta en la cima del volcán Ampato.",
                ubicacion = "Calle La Merced 110",
                categoria = "Cultural",
                idImagen = "img_museo_santuarios",
                distanciaTexto = "A 500m"
            ),
            AtractivoEntity(
                id = "a4",
                nombre = "Mercado San Camilo",
                descripcionCorta = "Mercado tradicional",
                descripcionLarga = "Diseñado por Gustave Eiffel, este mercado es el corazón comercial de Arequipa. Encuentra frutas exóticas, jugos frescos, quesos y artesanías.",
                ubicacion = "Calle San Camilo S/N",
                categoria = "Gastronomía",
                idImagen = "img_san_camilo",
                distanciaTexto = "A 1.2km"
            ),
            AtractivoEntity(
                id = "a5",
                nombre = "Mirador de Yanahuara",
                descripcionCorta = "Vistas icónicas del Misti",
                descripcionLarga = "Famoso por sus arcos de sillar con inscripciones de poetas arequipeños, ofrece una vista panorámica inigualable de la ciudad y sus tres volcanes.",
                ubicacion = "Plaza de Yanahuara",
                categoria = "Vistas",
                idImagen = "img_yanahuara",
                distanciaTexto = "A 2.5km"
            )
        )
        atractivoDao.insertAtractivos(atractivos)
    }

    private suspend fun insertDefaultReviews() {
        val reviews = listOf(
            ReviewEntity(
                id = "r1",
                attractionId = "a1",
                userName = "Sophia Clark",
                date = "Hace 2 meses",
                rating = 5f,
                comment = "¡Unas vistas absolutamente impresionantes! El corazón de la ciudad.",
                likes = 12,
                dislikes = 2
            ),
            ReviewEntity(
                id = "r2",
                attractionId = "a1",
                userName = "Ethan Carter",
                date = "Hace 3 meses",
                rating = 4f,
                comment = "El sillar es espectacular. Sin embargo, estaba muy concurrido. Recomiendo ir de noche.",
                likes = 8,
                dislikes = 1
            ),
            ReviewEntity(
                id = "r3",
                attractionId = "a2",
                userName = "Sophia Clark",
                date = "Hace 2 meses",
                rating = 5f,
                comment = "¡Unas vistas absolutamente impresionantes! El Cañón del Colca es un destino que no te puedes perder. La caminata fue difícil, pero mereció la pena y el paisaje era incomparable. ¡Muy recomendable!",
                likes = 12,
                dislikes = 2
            ),
            ReviewEntity(
                id = "r4",
                attractionId = "a2",
                userName = "Ethan Carter",
                date = "Hace 3 meses",
                rating = 3f,
                comment = "El cañón es impresionante y las vistas son espectaculares. Sin embargo, la visita me pareció un poco apresurada y me hubiera gustado tener más tiempo para explorarlo. En general, fue una buena experiencia, pero se podría mejorar.",
                likes = 8,
                dislikes = 1
            )
        )
        reviewDao.insertReviews(reviews)
    }

    suspend fun getRecomendaciones(): List<AtractivoTuristico> {
        return atractivoDao.getAllAtractivos()
            .filter { it.id == "a1" || it.id == "a2" }
            .map { it.toModel() }
    }

    suspend fun getTodosLosAtractivos(): List<AtractivoTuristico> {
        return atractivoDao.getAllAtractivos().map { it.toModel() }
    }

    suspend fun getCercanos(): List<AtractivoTuristico> {
        return atractivoDao.getAllAtractivos()
            .filter { it.id in listOf("a3", "a4", "a5") }
            .map { it.toModel() }
    }

    suspend fun getAtractivoPorId(id: String): AtractivoTuristico? {
        return atractivoDao.getAtractivoById(id)?.toModel()
    }

    suspend fun getReviewsForAttraction(attractionId: String): List<Review> {
        return reviewDao.getReviewsByAttraction(attractionId).map { it.toModel() }
    }

    suspend fun searchAtractivos(query: String): List<AtractivoTuristico> {
        return atractivoDao.searchAtractivos(query).map { it.toModel() }
    }

    suspend fun getAtractivosByCategoria(categoria: String): List<AtractivoTuristico> {
        return atractivoDao.getAtractivosByCategoria(categoria).map { it.toModel() }
    }

    private fun AtractivoEntity.toModel(): AtractivoTuristico {
        return AtractivoTuristico(
            id = id,
            nombre = nombre,
            descripcionCorta = descripcionCorta,
            descripcionLarga = descripcionLarga,
            ubicacion = ubicacion,
            latitud = latitud,
            longitud = longitud,
            categoria = categoria,
            precio = precio,
            horario = horario,
            rating = rating,
            idImagen = idImagen,
            distanciaTexto = distanciaTexto
        )
    }

    private fun ReviewEntity.toModel(): Review {
        return Review(
            id = id,
            userName = userName,
            date = date,
            rating = rating,
            comment = comment,
            likes = likes,
            dislikes = dislikes
        )
    }
}
