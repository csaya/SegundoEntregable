package com.example.segundoentregable.data.model

import com.example.segundoentregable.data.local.entity.ActividadEntity
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.GaleriaFotoEntity

data class AtractivoTuristico(
    val id: String,
    val codigoMincetur: String = "",
    val nombre: String,
    val descripcionCorta: String,
    val descripcionLarga: String,
    val ubicacion: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val departamento: String = "",
    val provincia: String = "",
    val distrito: String = "",
    val altitud: Int = 0,
    val categoria: String,
    val tipo: String = "",
    val subtipo: String = "",
    val jerarquia: Int = 0,
    val precio: Double = 0.0,
    val precioDetalle: String = "",
    val horario: String = "",
    val horarioDetallado: String = "",
    val epocaVisita: String = "",
    val tiempoVisitaSugerido: String = "",
    val estadoActual: String = "",
    val observaciones: String = "",
    val tieneAccesibilidad: Boolean = false,
    val imagenPrincipal: String,
    val rating: Float = 5.0f,
    val distanciaTexto: String = "",
    // Relaciones opcionales (cargadas solo en DetailScreen)
    val galeria: List<GaleriaFoto> = emptyList(),
    val actividades: List<Actividad> = emptyList(),
    val isFavorito: Boolean = false
)

data class GaleriaFoto(
    val id: String,
    val atractivoId: String,
    val urlFoto: String,
    val orden: Int
)

data class Actividad(
    val id: String,
    val atractivoId: String,
    val nombre: String,
    val categoria: String,
    val icono: String
)

// Extension functions para convertir Entity a Domain Model
fun AtractivoEntity.toDomainModel(
    galeria: List<GaleriaFoto> = emptyList(),
    actividades: List<Actividad> = emptyList(),
    isFavorito: Boolean = false
): AtractivoTuristico {
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
        distanciaTexto = distanciaTexto,
        galeria = galeria,
        actividades = actividades,
        isFavorito = isFavorito
    )
}

fun GaleriaFotoEntity.toDomainModel(): GaleriaFoto {
    return GaleriaFoto(
        id = id,
        atractivoId = atractivoId,
        urlFoto = urlFoto,
        orden = orden
    )
}

fun ActividadEntity.toDomainModel(): Actividad {
    return Actividad(
        id = id,
        atractivoId = atractivoId,
        nombre = nombre,
        categoria = categoria,
        icono = icono
    )
}