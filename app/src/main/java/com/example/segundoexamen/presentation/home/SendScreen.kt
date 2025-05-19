package com.example.segundoexamen.presentation.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.segundoexamen.R
import com.example.segundoexamen.presentation.model.User
import com.example.segundoexamen.presentation.model.UserNotification
import com.example.segundoexamen.presentation.repository.FirebaseAuthHelper
import com.example.segundoexamen.presentation.repository.FirebaseAuthHelper.getAccessToken
import com.example.segundoexamen.ui.theme.Black
import com.example.segundoexamen.ui.theme.Gray
import com.example.segundoexamen.ui.theme.White
import com.example.segundoexamen.ui.theme.Yellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// ✅ Definición correcta de UserNotification
data class UserNotification(
    val id: String,
    val name: String,
    val description: String,
    val token: String,
    var isSelected: Boolean = false
)

// ✅ Composable corregido para mostrar la tabla con CheckBoxes
@Composable
fun SendScreen(navHostController: NavHostController, auth: FirebaseAuth, db: FirebaseFirestore) {
    var usuarios by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        obtenerUsuarios(db) { lista -> usuarios = lista }
    }

    TablaUsuarios(usuarios, navHostController)

}

@Composable
fun TablaUsuarios(usuarios: List<User>, navHostController: NavHostController) {
    val seleccionados = remember { mutableStateMapOf<String, Boolean>() }
    var error by remember { mutableStateOf(false) }
    var descerror by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .background(Brush.verticalGradient(listOf(Gray, Black)))
        .padding(16.dp),
        verticalArrangement = Arrangement.Top){
        var tituloNotificacion by remember { mutableStateOf("") }
        var cuerpoNotificacion by remember { mutableStateOf("") }

        Row(){
            Icon(painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "",
                tint = White,
                modifier = Modifier.padding(vertical = 24.dp).size(24.dp).clickable {
                    navHostController.navigate("home")
                })
            Spacer(modifier = Modifier.weight(1f))
        }

        Box(
            modifier = Modifier
                .height(300.dp) // 🔄 Limita la tabla a 300dp de altura
                .fillMaxWidth() // 🔄 Habilita el scroll dentro de la tabla
        ){
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth().height(300.dp)
                    .padding(top = 40.dp, start = 30.dp, end = 30.dp), // Agregar espacio arriba y a los lados
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item{
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(text = "Nombre", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = White)
                        Text(text = "Apellido", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = White)
                        Text(text = "Email", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = White)
                    }
                }

                items(usuarios) { usuario ->
                    HorizontalDivider(color = Color.White, thickness = 2.dp)
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = seleccionados[usuario.email] ?: false,
                            onCheckedChange = {isChecked ->
                                seleccionados[usuario.email] = isChecked
                            }
                        )
                        Text(text = usuario.name, modifier = Modifier.weight(1f), color = White)
                        Text(text = usuario.lastname, modifier = Modifier.weight(1f), color = White)
                        Text(text = usuario.email, modifier = Modifier.weight(1f), color = White)
                    }
                    HorizontalDivider(color = Color.White, thickness = 2.dp)
                }


            }
        }


        Text(text = "Título de la Notificación", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
        TextField(
            value = tituloNotificacion,
            onValueChange = { tituloNotificacion = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Escribe el título...") }
        )

        Spacer(modifier = Modifier.height(8.dp)) // 🔄 Espacio entre los TextField

        Text(text = "Cuerpo de la Notificación", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
        TextField(
            value = cuerpoNotificacion,
            onValueChange = { cuerpoNotificacion = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Escribe el mensaje...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        Button(
            onClick = {
                val usuariosSeleccionadosTokens = usuarios.filter { seleccionados[it.email] == true }
                    .map { it.token } // Extraemos los tokens

                if(tituloNotificacion.isEmpty() || cuerpoNotificacion.isEmpty()){
                    error = true
                    descerror = "Debe rellenar todos los campos"
                    return@Button
                }
                if (usuariosSeleccionadosTokens.isNotEmpty()) {
                    usuariosSeleccionadosTokens.forEach { token ->
                        sendPushNotification(context, token, tituloNotificacion, cuerpoNotificacion)
                        success = true
                    }
                }else{
                    error = true
                    descerror = "Debe seleccionar al menos un usuario"
                    return@Button
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally), colors = ButtonDefaults.buttonColors(containerColor = Yellow)
        ) {
            Text("Enviar Notificación a Seleccionados", color = Black)
        }

        Spacer(modifier = Modifier.height(16.dp)) // 🔄 Espacio entre los botones

        // 🔹 Botón para enviar la notificación a todos los usuarios
        Button(
            onClick = {
                val todosLosTokens = usuarios.map { it.token } // 🔄 Extrae todos los tokens sin filtrar

                if (tituloNotificacion.isEmpty() || cuerpoNotificacion.isEmpty()) {
                    error = true
                    descerror = "Debe rellenar todos los campos"
                    return@Button
                }
                    todosLosTokens.forEach { token ->
                        sendPushNotification(context, token, tituloNotificacion, cuerpoNotificacion)
                    }
                    success = true


            },
            modifier = Modifier.padding(top = 16.dp)
                .align(Alignment.CenterHorizontally), colors = ButtonDefaults.buttonColors(containerColor = Yellow)
        ) {
            Text("Enviar Notificación a Todos", color = Black)
        }

        if(error){
            AlertDialog(
                onDismissRequest = {error = false},
                title = { Text("Error") },
                text = { Text(descerror) },
                confirmButton = { }
            )

        }

        if(success){
            AlertDialog(
                onDismissRequest = {success = false},
                title = { Text("Exito") },
                text = { Text("La notificación fue enviada correctamente") },
                confirmButton = { }
            )

        }
    }







}

fun obtenerUsuarios(db: FirebaseFirestore, onResult: (List<User>) -> Unit) {
    db.collection("users").get().addOnSuccessListener { result ->
        val listaUsuarios = result.map { doc ->
            User(
                name = doc.getString("name") ?: "",
                lastname = doc.getString("lastname") ?: "",
                email = doc.getString("email") ?: "",
                role = doc.getLong("role")?.toInt() ?: 0,
                token = doc.getString("token") ?: ""
            )
        }
        onResult(listaUsuarios)
    }
}

fun sendPushNotification(context: Context, token: String, title: String, message: String) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken(context)
            if (accessToken.isEmpty()) {
                println("Error: No se pudo obtener el accessToken.")
                return@launch
            }

            val json = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", message)
                    })
                })
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/segundoexamen-450fd/messages:send")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            println("Código de respuesta: ${response.code}")

            if (response.code == 400) {
                println("Error 400: Verifica la estructura del JSON y el token de destino.")
            } else if (response.code == 404) {
                println("Error 404: El token de FCM no es válido o ha expirado.")
            }

        } catch (e: Exception) {
            println("Error al enviar la notificación: ${e.message}")
        }
    }
}



