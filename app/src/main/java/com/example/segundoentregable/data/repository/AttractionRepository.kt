package com.example.segundoentregable.data.repository

import com.example.segundoentregable.data.local.dao.AtractivoDao
import com.example.segundoentregable.data.local.dao.ReviewDao
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.ReviewEntity
import com.example.segundoentregable.data.model.AtractivoTuristico
import com.example.segundoentregable.data.model.Review

class AttractionRepository(
    private val atractivoDao: AtractivoDao,
    private val reviewDao: ReviewDao
) {

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
                nombre = "Monasterio de Santa Catalina",
                descripcionCorta = "Convento colonial del siglo XVI",
                descripcionLarga = "El Monasterio de Santa Catalina es una joya arquitectónica del siglo XVI. Este convento activo alberga a monjas de clausura y es famoso por sus callejones coloridos, patios tranquilos y la mezcla única de arquitectura colonial española con influencias andinas. Es uno de los monumentos más importantes de Arequipa.",
                ubicacion = "Calle Santa Catalina 301",
                latitud = -16.3964,
                longitud = -71.5375,
                categoria = "Cultural",
                precio = 40.0,
                horario = "09:00 - 17:00",
                rating = 4.8f,
                idImagen = "https://upload.wikimedia.org/wikipedia/commons/a/ae/Monasterio_de_Santa_Catalina%2C_Arequipa.jpg",
                distanciaTexto = "Centro"
            ),
            AtractivoEntity(
                id = "a2",
                nombre = "Mirador de Yanahuara",
                descripcionCorta = "Vistas panorámicas del volcán Misti",
                descripcionLarga = "El Mirador de Yanahuara es uno de los puntos más fotografiados de Arequipa. Ofrece vistas espectaculares del volcán Misti, Chachani y Pichu Pichu. Los arcos de sillar con inscripciones de poetas arequipeños hacen de este lugar un sitio romántico y culturalmente significativo.",
                ubicacion = "Plaza de Yanahuara",
                latitud = -16.3850,
                longitud = -71.5450,
                categoria = "Vistas",
                precio = 0.0,
                horario = "06:00 - 20:00",
                rating = 4.7f,
                idImagen = "https://upload.wikimedia.org/wikipedia/commons/a/ae/Monasterio_de_Santa_Catalina%2C_Arequipa.jpg",
                distanciaTexto = "2.5 km"
            ),
            AtractivoEntity(
                id = "a3",
                nombre = "Cañón del Colca",
                descripcionCorta = "Uno de los cañones más profundos del mundo",
                descripcionLarga = "El Cañón del Colca es una maravilla natural con una profundidad de más de 3,400 metros. Es famoso por el vuelo del cóndor andino, especialmente visible en la mañana. Ofrece vistas espectaculares, oportunidades de trekking y baños termales naturales. Un destino imprescindible para aventureros.",
                ubicacion = "Provincia de Caylloma",
                latitud = -15.5500,
                longitud = -71.9500,
                categoria = "Aventura",
                precio = 60.0,
                horario = "24 horas",
                rating = 4.9f,
                idImagen = "https://upload.wikimedia.org/wikipedia/commons/a/ae/Monasterio_de_Santa_Catalina%2C_Arequipa.jpg",
                distanciaTexto = "160 km"
            ),
            AtractivoEntity(
                id = "a4",
                nombre = "Plaza de Armas",
                descripcionCorta = "Centro histórico de Arequipa",
                descripcionLarga = "La Plaza de Armas es el corazón de Arequipa, rodeada de edificios coloniales de sillar blanco. La majestuosa Catedral Basílica domina la plaza. Es un lugar vibrante lleno de historia, cafés, tiendas y vida local. Perfecta para pasear y disfrutar de la arquitectura colonial.",
                ubicacion = "Centro Histórico",
                latitud = -16.3989,
                longitud = -71.5349,
                categoria = "Cultural",
                precio = 0.0,
                horario = "24 horas",
                rating = 4.6f,
                idImagen = "https://upload.wikimedia.org/wikipedia/commons/a/ae/Monasterio_de_Santa_Catalina%2C_Arequipa.jpg",
                distanciaTexto = "Centro"
            ),
            AtractivoEntity(
                id = "a5",
                nombre = "Volcán El Misti",
                descripcionCorta = "Volcán icónico de Arequipa",
                descripcionLarga = "El Misti es el volcán más emblemático de Arequipa con 5,822 metros de altura. Su cono perfecto es visible desde toda la ciudad. Es un destino popular para montañismo con vistas panorámicas de la región desde la cumbre. La ascensión requiere aclimatación y experiencia.",
                ubicacion = "Parque Nacional Misti",
                latitud = -16.2950,
                longitud = -71.4117,
                categoria = "Aventura",
                precio = 80.0,
                horario = "24 horas",
                rating = 4.8f,
                idImagen = "https://upload.wikimedia.org/wikipedia/commons/a/ae/Monasterio_de_Santa_Catalina%2C_Arequipa.jpg",
                distanciaTexto = "17 km"
            ),
            AtractivoEntity(
                id = "a6",
                nombre = "Mansión del Fundador",
                descripcionCorta = "Casa colonial histórica",
                descripcionLarga = "La Mansión del Fundador es una casa colonial del siglo XVI que perteneció a García Manuel de Carbajal, fundador de Arequipa. Conserva la arquitectura original con patios, fuentes y detalles coloniales. Actualmente funciona como museo que muestra la vida de la época colonial.",
                ubicacion = "Calle La Merced 207",
                latitud = -16.3975,
                longitud = -71.5380,
                categoria = "Cultural",
                precio = 25.0,
                horario = "09:00 - 17:00",
                rating = 4.5f,
                idImagen = "https://upload.wikimedia.org/wikipedia/commons/a/ae/Monasterio_de_Santa_Catalina%2C_Arequipa.jpg",
                distanciaTexto = "Centro"
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

    suspend fun addReview(attractionId: String, userEmail: String, userName: String, rating: Float, comment: String) {
        val newReview = ReviewEntity(
            id = java.util.UUID.randomUUID().toString(), // Generamos ID único
            attractionId = attractionId,
            userName = userName,
            date = "Hace un momento", // En una app real usaríamos DateFormatter
            rating = rating,
            comment = comment,
            likes = 0,
            dislikes = 0
        )
        reviewDao.insertReviews(listOf(newReview))
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
