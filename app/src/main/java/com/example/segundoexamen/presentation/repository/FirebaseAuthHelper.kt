package com.example.segundoexamen.presentation.repository

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream

object FirebaseAuthHelper {
    fun getAccessToken(context: Context): String {
        val inputStream = context.assets.open("firebase-adminsdk.json") // 🔄 Acceder correctamente al archivo en assets
        val credentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue // ✅ Devuelve el token correctamente
    }
}