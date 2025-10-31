package com.example.segundoentregable.data.model

data class Review(
    val id: String,
    val userName: String,
    val date: String,
    val rating: Float,
    val comment: String,
    val likes: Int,
    val dislikes: Int
)