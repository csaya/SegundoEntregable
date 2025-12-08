package com.example.segundoentregable.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utilidad para determinar si un lugar está abierto basándose en su horario.
 * 
 * Formatos de horario soportados:
 * - "07:00 a.m. - 06:00 p.m."
 * - "09:30 a.m. - 06:30 p.m."
 * - "24 horas"
 * - "Todo el día"
 */
object HorarioUtils {

    data class EstadoHorario(
        val isOpen: Boolean,
        val mensaje: String,
        val proximoCambio: String? = null
    )

    /**
     * Determina si un lugar está abierto ahora.
     * @param horario String con el horario (ej: "07:00 a.m. - 06:00 p.m.")
     * @return EstadoHorario con información del estado actual
     */
    fun getEstadoActual(horario: String): EstadoHorario {
        if (horario.isBlank()) {
            return EstadoHorario(
                isOpen = true,
                mensaje = "Horario no disponible"
            )
        }

        val horarioLower = horario.lowercase(Locale.getDefault())

        // Casos especiales
        if (horarioLower.contains("24 horas") || 
            horarioLower.contains("todo el día") ||
            horarioLower.contains("todo el dia")) {
            return EstadoHorario(
                isOpen = true,
                mensaje = "Abierto 24 horas"
            )
        }

        // Intentar parsear formato "HH:MM a.m. - HH:MM p.m."
        return try {
            parseHorarioRango(horario)
        } catch (e: Exception) {
            EstadoHorario(
                isOpen = true,
                mensaje = horario // Mostrar el horario original si no se puede parsear
            )
        }
    }

    private fun parseHorarioRango(horario: String): EstadoHorario {
        // Limpiar y normalizar el string
        val cleanHorario = horario
            .replace(".", "")
            .replace("am", "AM")
            .replace("pm", "PM")
            .replace("a m", "AM")
            .replace("p m", "PM")
            .trim()

        // Buscar patrón "HH:MM AM/PM - HH:MM AM/PM"
        val regex = """(\d{1,2}):(\d{2})\s*(AM|PM)\s*-\s*(\d{1,2}):(\d{2})\s*(AM|PM)""".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(cleanHorario)

        if (match != null) {
            val (h1, m1, ampm1, h2, m2, ampm2) = match.destructured

            val horaApertura = convertTo24Hour(h1.toInt(), m1.toInt(), ampm1.uppercase())
            val horaCierre = convertTo24Hour(h2.toInt(), m2.toInt(), ampm2.uppercase())

            val ahora = LocalTime.now()

            val isOpen = if (horaCierre.isBefore(horaApertura)) {
                // Horario nocturno (ej: 20:00 - 02:00)
                ahora.isAfter(horaApertura) || ahora.isBefore(horaCierre)
            } else {
                // Horario normal
                ahora.isAfter(horaApertura) && ahora.isBefore(horaCierre)
            }

            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

            return if (isOpen) {
                EstadoHorario(
                    isOpen = true,
                    mensaje = "Abierto",
                    proximoCambio = "Cierra a las ${horaCierre.format(formatter)}"
                )
            } else {
                EstadoHorario(
                    isOpen = false,
                    mensaje = "Cerrado",
                    proximoCambio = "Abre a las ${horaApertura.format(formatter)}"
                )
            }
        }

        // Si no se puede parsear, asumir abierto y mostrar horario original
        return EstadoHorario(
            isOpen = true,
            mensaje = horario
        )
    }

    private fun convertTo24Hour(hour: Int, minute: Int, ampm: String): LocalTime {
        var h = hour
        if (ampm == "PM" && hour != 12) {
            h += 12
        } else if (ampm == "AM" && hour == 12) {
            h = 0
        }
        return LocalTime.of(h, minute)
    }

    /**
     * Versión simplificada que solo retorna si está abierto o no.
     */
    fun isOpenNow(horario: String): Boolean {
        return getEstadoActual(horario).isOpen
    }
}
