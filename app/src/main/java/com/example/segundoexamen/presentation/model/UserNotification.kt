package com.example.segundoexamen.presentation.model

data class UserNotification(
    val id: String,
    val name: String,
    val description: String,
    val token: String,
    var isSelected: Boolean = false
)