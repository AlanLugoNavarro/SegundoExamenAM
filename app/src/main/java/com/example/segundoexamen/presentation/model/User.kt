package com.example.segundoexamen.presentation.model

data class User(
    val name: String,
    val lastname: String,
    val email: String,
    val role: Int,
    val token: String
)