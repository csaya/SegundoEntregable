package com.example.segundoentregable.data.repository

import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.Review

object FakeAttractionRepository {

    private val favoritos = mutableSetOf<String>()

    private val allReviews = mapOf(
        "a1" to listOf(
            Review(
                id = "r1",
                userName = "Sophia Clark",
                date = "Hace 2 meses",
                rating = 5f,
                comment = "¡Unas vistas absolutamente impresionantes! El corazón de la ciudad.",
                likes = 12,
                dislikes = 2
            ),
            Review(
                id = "r2",
                userName = "Ethan Carter",
                date = "Hace 3 meses",
                rating = 4f,
                comment = "El sillar es espectacular. Sin embargo, estaba muy concurrido. Recomiendo ir de noche.",
                likes = 8,
                dislikes = 1
            )
        ),
        "a2" to listOf(
            Review(
                id = "r1",
                userName = "Sophia Clark",
                date = "Hace 2 meses",
                rating = 5f,
                comment = "¡Unas vistas absolutamente impresionantes! El Cañón del Colca es un destino que no te puedes perder. La caminata fue difícil, pero mereció la pena y el paisaje era incomparable. ¡Muy recomendable!",
                likes = 12,
                dislikes = 2
            ),
            Review(
                id = "r2",
                userName = "Ethan Carter",
                date = "Hace 3 meses",
                rating = 3f,
                comment = "El cañón es impresionante y las vistas son espectaculares. Sin embargo, la visita me pareció un poco apresurada y me hubiera gustado tener más tiempo para explorarlo. En general, fue una buena experiencia, pero se podría mejorar.",
                likes = 8,
                dislikes = 1
            )
        )
    )

    private val todosLosAtractivos = listOf(
        AtractivoTuristico(
            id = "a1",
            nombre = "Plaza de Armas",
            descripcionCorta = "Corazón de la ciudad",
            descripcionLarga = "La Plaza de Armas de Arequipa es famosa por su arquitectura de sillar y su imponente catedral. Un lugar de encuentro vibrante y lleno de historia.",
            ubicacion = "Centro Histórico",
            categoria = "Cultural",
            idImagen = "img_plaza_armas"
        ),
        AtractivoTuristico(
            id = "a2",
            nombre = "Cañón del Colca",
            descripcionCorta = "Maravilla natural",
            descripcionLarga = "Uno de los cañones más profundos del mundo, hogar del majestuoso cóndor andino. Ideal para trekking y vistas espectaculares.",
            ubicacion = "Provincia de Caylloma",
            categoria = "Aventura",
            idImagen = "img_colca"
        ),
        AtractivoTuristico(
            id = "a3",
            nombre = "Museo Santuarios Andinos",
            descripcionCorta = "Hogar de la Momia Juanita",
            descripcionLarga = "Este museo alberga a Juanita, la famosa 'Dama de Ampato', una momia inca excepcionalmente conservada descubierta en la cima del volcán Ampato.",
            ubicacion = "Calle La Merced 110",
            categoria = "Cultural",
            idImagen = "img_museo_santuarios",
            distanciaTexto = "A 500m"
        ),
        AtractivoTuristico(
            id = "a4",
            nombre = "Mercado San Camilo",
            descripcionCorta = "Mercado tradicional",
            descripcionLarga = "Diseñado por Gustave Eiffel, este mercado es el corazón comercial de Arequipa. Encuentra frutas exóticas, jugos frescos, quesos y artesanías.",
            ubicacion = "Calle San Camilo S/N",
            categoria = "Gastronomía",
            idImagen = "img_san_camilo",
            distanciaTexto = "A 1.2km"
        ),
        AtractivoTuristico(
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

    fun getRecomendaciones(): List<AtractivoTuristico> {
        return todosLosAtractivos.filter { it.id == "a1" || it.id == "a2" }
    }

    fun getTodosLosAtractivos(): List<AtractivoTuristico> {
        return todosLosAtractivos
    }

    fun getCercanos(): List<AtractivoTuristico> {
        return todosLosAtractivos.filter { it.id in listOf("a3", "a4", "a5") }
    }

    fun getAtractivoPorId(id: String): AtractivoTuristico? {
        return todosLosAtractivos.find { it.id == id }
    }

    fun getReviewsForAttraction(attractionId: String): List<Review> {
        return allReviews[attractionId] ?: emptyList()
    }

    fun toggleFavorito(id: String) {
        if (favoritos.contains(id)) favoritos.remove(id) else favoritos.add(id)
    }

    fun isFavorito(id: String): Boolean = favoritos.contains(id)

    fun getFavoritos(): List<AtractivoTuristico> {
        return todosLosAtractivos.filter { favoritos.contains(it.id) }
    }
}