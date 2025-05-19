package com.example.segundoexamen.presentation.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.segundoexamen.R
import com.example.segundoexamen.presentation.model.Notification
import com.example.segundoexamen.presentation.model.User
import com.example.segundoexamen.presentation.singup.comprobarCorreo
import com.example.segundoexamen.ui.theme.Black
import com.example.segundoexamen.ui.theme.Gray
import com.example.segundoexamen.ui.theme.SelectedField
import com.example.segundoexamen.ui.theme.UnselectedField
import com.example.segundoexamen.ui.theme.White
import com.example.segundoexamen.ui.theme.Yellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MailboxScreen(navHostController: NavHostController, auth: FirebaseAuth, db: FirebaseFirestore){
    val notifications by getUserNotifications().collectAsState(initial = emptyList())
    var correoActual by remember { mutableStateOf("") }
    correoActual = auth.currentUser?.email ?: ""

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).fillMaxHeight().background(Brush.verticalGradient(listOf(Gray, Black)))) {
        Icon(painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "",
            tint = White,
            modifier = Modifier.padding(vertical = 24.dp).size(24.dp).clickable {
                navHostController.navigate("home")
            })
        Text(text = "Notificaciones", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
        Icon(painter = painterResource(id = R.drawable.ic_delete),
            contentDescription = "",
            tint = White,
            modifier = Modifier.padding(vertical = 24.dp).size(24.dp).clickable {
                eliminarDocumentosPorCorreo(correoActual)
            })
        Spacer(modifier = Modifier.height(8.dp))

        if (notifications.isEmpty()) {
            Text(
                text = "No tienes notificaciones aún.",
                fontSize = 16.sp,
                color = White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn {
                items(notifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
        Button(
            onClick = {
                navHostController.navigate("home")
            },
            modifier = Modifier.padding(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Yellow)
        ) {
            Text("Regresar", color = Black)
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = notification.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Black)
            Text(text = notification.description, fontSize = 14.sp, color = Black)
            Text(text = "Fecha: ${notification.date}", fontSize = 12.sp, color = Black)
        }
    }
}

fun getUserNotifications(): Flow<List<Notification>> {
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return flowOf(emptyList())

    return callbackFlow {
        val db = Firebase.firestore
        val query = db.collection("notifications").whereEqualTo("userEmail", userEmail)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val notifications = snapshot?.documents?.map { document ->
                val dateStr = document.getString("date") ?: "0000-00-00 00:00:00" // 🔄 Fecha por defecto si falta

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = dateFormat.parse(dateStr) ?: Date(0) // 🔄 Convierte la fecha a un objeto `Date`

                Notification(
                    title = document.getString("title") ?: "Sin título",
                    description = document.getString("description") ?: "Sin descripción",
                    date = dateStr,
                    realDate = date // 🔄 Agrega `realDate` para ordenar correctamente
                )
            }?.sortedByDescending { it.realDate } ?: emptyList() // 🔄 Ordena de más reciente a más antiguo

            trySend(notifications)
        }

        awaitClose { listener.remove() }
    }
}

fun eliminarDocumentosPorCorreo(correo: String) {
    val db = FirebaseFirestore.getInstance()
    db.collection("notifications") // 🔄 Cambia "users" por el nombre de tu colección
        .whereEqualTo("userEmail", correo)
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                db.collection("notifications").document(document.id)
                    .delete()
            }
        }
}