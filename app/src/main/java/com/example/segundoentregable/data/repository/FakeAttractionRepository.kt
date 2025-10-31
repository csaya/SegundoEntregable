package com.example.segundoentregable.data.repository

import com.example.segundoentregable.data.model.AtractivoTuristico

// Singleton que simula nuestra base de datos en memoria
object FakeAttractionRepository {

    // Lista temporal de IDs de favoritos
    private val favoritos = mutableSetOf<String>()

    // DATOS FALSOS (ahora coinciden con el mockup)
    private val todosLosAtractivos = listOf(
        // Para "Recomendaciones"
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
        // Para "Cercanos a ti"
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

    // --- Funciones que llamarán nuestros ViewModels ---

    fun getRecomendaciones(): List<AtractivoTuristico> {
        // Devolvemos los 2 primeros (Plaza y Colca)
        return todosLosAtractivos.filter { it.id == "a1" || it.id == "a2" }
    }

    fun getTodosLosAtractivos(): List<AtractivoTuristico> {
        return todosLosAtractivos
    }

    fun getCercanos(): List<AtractivoTuristico> {
        // Devolvemos los otros 3
        return todosLosAtractivos.filter { it.id in listOf("a3", "a4", "a5") }
    }

    fun getAtractivoPorId(id: String): AtractivoTuristico? {
        return todosLosAtractivos.find { it.id == id }
    }

    // --- Lógica de Favoritos (Simulación) ---

    fun toggleFavorito(id: String) {
        if (favoritos.contains(id)) favoritos.remove(id) else favoritos.add(id)
    }

    fun isFavorito(id: String): Boolean = favoritos.contains(id)

    fun getFavoritos(): List<AtractivoTuristico> {
        return todosLosAtractivos.filter { favoritos.contains(it.id) }
    }
}