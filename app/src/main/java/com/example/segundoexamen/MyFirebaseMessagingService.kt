package com.example.segundoexamen

import android.util.Log
import com.example.segundoexamen.presentation.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val user = FirebaseAuth.getInstance().currentUser // Obtener el usuario autenticado
        val email = user?.email ?: "Correo no disponible"

        val title = remoteMessage.notification?.title ?: "Sin título"
        val description = remoteMessage.notification?.body ?: "Sin descripción"

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("America/Mexico_City") // Ajusta tu zona horaria
        val formattedDate = dateFormat.format(Date(System.currentTimeMillis()))

        val notification = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to formattedDate,
            "userEmail" to email // Guardar el correo del usuario
        )

        val db = Firebase.firestore
        db.collection("notifications")
            .add(notification)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Notificación guardada con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al guardar notificación", e)
            }
    }
    override fun onNewToken(token: String) {
        Log.d("Firebase", "Nuevo token de FCM: $token")
        guardarTokenEnFirestore(token) // 🔄 Guarda el token actualizado en Firestore
    }

    private fun guardarTokenEnFirestore(nuevoToken: String) {
        val usuarioActual = FirebaseAuth.getInstance().currentUser
        if (usuarioActual != null) {
            val db = Firebase.firestore
            db.collection("users").document(usuarioActual.uid)
                .update("token", nuevoToken)
                .addOnSuccessListener { Log.d("Firestore", "Token actualizado correctamente!") }
                .addOnFailureListener { e -> Log.w("Firestore", "Error al actualizar el token", e) }
        }
    }

}