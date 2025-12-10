package com.example.segundoentregable.data.model

import java.util.concurrent.TimeUnit

data class Review(
    val id: String,
    val userName: String,
    val userEmail: String = "",
    val date: String,
    val rating: Float,
    val comment: String,
    val likes: Int,
    val dislikes: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Retorna el tiempo relativo desde que se creó la reseña
     */
    fun getRelativeTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - createdAt
        
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        return when {
            seconds < 60 -> "Hace un momento"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hace $hours h"
            days < 7 -> "Hace $days días"
            days < 30 -> "Hace ${days / 7} semanas"
            days < 365 -> "Hace ${days / 30} meses"
            else -> "Hace ${days / 365} años"
        }
    }
}