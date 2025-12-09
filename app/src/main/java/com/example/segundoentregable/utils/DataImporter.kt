package com.example.segundoentregable.utils

import android.content.Context
import android.util.Log
import com.example.segundoentregable.data.local.AppDatabase
import com.example.segundoentregable.data.local.entity.ActividadEntity
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.GaleriaFotoEntity
import com.example.segundoentregable.data.local.entity.RutaEntity
import com.example.segundoentregable.data.local.entity.RutaParadaEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "DataImporter"

object DataImporter {

    // Mapa de traducción: IDs legibles en rutas.json -> IDs reales en atractivos.json
    private val idTranslationMap = mapOf(
        "plaza_armas" to "mincetur_972",
        "monasterio_santa_catalina" to "mincetur_974",
        "yanahuara" to "mincetur_597",
        "molino_sabandia" to "mincetur_596", // La Mansión del Fundador está cerca
        "catedral" to "mincetur_973",
        "iglesia_compania" to "mincetur_975",
        "san_francisco" to "mincetur_976",
        "santo_domingo" to "mincetur_977",
        "la_recoleta" to "mincetur_982",
        "san_lazaro" to "mincetur_989",
        "museo_santuarios" to "mincetur_2735",
        "casa_tristan" to "mincetur_2744",
        "volcán_misti" to "mincetur_599",
        "canteras_sillar" to "mincetur_3941"
    )

    suspend fun importDataFromAssets(context: Context, database: AppDatabase) {
        withContext(Dispatchers.IO) {
            try {
                val gson = Gson()

                // 1. Importar Atractivos
                val atractivosJson = context.assets.open("atractivos.json")
                    .bufferedReader()
                    .use { it.readText() }

                val atractivosType = object : TypeToken<List<AtractivoJsonModel>>() {}.type
                val atractivosJsonList: List<AtractivoJsonModel> = gson.fromJson(atractivosJson, atractivosType)

                val atractivosEntities = atractivosJsonList.map { it.toEntity() }
                database.atractivoDao().insertAtractivos(atractivosEntities)
                Log.d(TAG, "Importados ${atractivosEntities.size} atractivos")

                // 2. Importar Galerías
                val galeriasJson = context.assets.open("galerias.json")
                    .bufferedReader()
                    .use { it.readText() }

                val galeriasType = object : TypeToken<List<GaleriaFotoEntity>>() {}.type
                val galeriasEntities: List<GaleriaFotoEntity> = gson.fromJson(galeriasJson, galeriasType)
                database.galeriaFotoDao().insertAll(galeriasEntities)
                Log.d(TAG, "Importadas ${galeriasEntities.size} fotos de galería")

                // 3. Importar Actividades
                val actividadesJson = context.assets.open("actividades.json")
                    .bufferedReader()
                    .use { it.readText() }

                val actividadesType = object : TypeToken<List<ActividadEntity>>() {}.type
                val actividadesEntities: List<ActividadEntity> = gson.fromJson(actividadesJson, actividadesType)
                database.actividadDao().insertAll(actividadesEntities)
                Log.d(TAG, "Importadas ${actividadesEntities.size} actividades")

                // 4. Importar Rutas Curadas
                try {
                    val rutasJson = context.assets.open("rutas.json")
                        .bufferedReader()
                        .use { it.readText() }

                    val rutasData = gson.fromJson(rutasJson, RutasJsonModel::class.java)
                    
                    database.rutaDao().insertRutas(rutasData.rutas)
                    Log.d(TAG, "Importadas ${rutasData.rutas.size} rutas")
                    
                    // Traducir IDs de atractivos en las paradas
                    val paradasTraducidas = rutasData.paradas.map { parada ->
                        val translatedId = idTranslationMap[parada.atractivoId] ?: parada.atractivoId
                        parada.copy(atractivoId = translatedId)
                    }
                    
                    database.rutaDao().insertParadas(paradasTraducidas)
                    Log.d(TAG, "Importadas ${paradasTraducidas.size} paradas de ruta (IDs traducidos)")
                } catch (e: Exception) {
                    Log.w(TAG, "No se encontró rutas.json o error al importar rutas: ${e.message}")
                }

                Log.d(TAG, "Importación de datos completada exitosamente")

            } catch (e: Exception) {
                Log.e(TAG, "Error al importar datos desde assets", e)
                throw e
            }
        }
    }
}

/**
 * Modelo JSON para rutas.json
 */
private data class RutasJsonModel(
    val rutas: List<RutaEntity>,
    val paradas: List<RutaParadaEntity>
)

/**
 * Modelo JSON intermedio para parsear atractivos.json
 * Los nombres de campos coinciden exactamente con el JSON
 */
private data class AtractivoJsonModel(
    val id: String,
    val codigoMincetur: String,
    val nombre: String,
    val descripcionCorta: String,
    val descripcionLarga: String,
    val ubicacion: String,
    val latitud: Double,
    val longitud: Double,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val altitud: Int,
    val categoria: String,
    val tipo: String,
    val subtipo: String,
    val jerarquia: Int,
    val precio: Double,
    val precioDetalle: String,
    val horario: String,
    val horarioDetallado: String,
    val epocaVisita: String,
    val tiempoVisitaSugerido: String,
    val estadoActual: String,
    val observaciones: String,
    val tieneAccesibilidad: Boolean,
    val imagenPrincipal: String,
    val rating: Float,
    val distanciaTexto: String
) {
    fun toEntity(): AtractivoEntity {
        return AtractivoEntity(
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
