package com.example.segundoexamen.presentation.model

import java.util.Date

data class Notification(
    val title: String,
    val description: String,
    val date: String,
    val realDate: Date
)