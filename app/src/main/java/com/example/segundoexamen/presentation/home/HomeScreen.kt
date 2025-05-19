package com.example.segundoexamen.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.segundoexamen.ui.theme.Black
import com.example.segundoexamen.ui.theme.Gray
import com.example.segundoexamen.ui.theme.White
import com.example.segundoexamen.ui.theme.Yellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun HomeScreen(navHostController: NavHostController, auth: FirebaseAuth, db:FirebaseFirestore){
    var correoActual by remember { mutableStateOf("") }
    var nombreU by remember { mutableStateOf("") }
    var apellidoU by remember { mutableStateOf("") }
    var rolU by remember { mutableStateOf(0) }
    var admin by remember { mutableStateOf(false) }
    correoActual = auth.currentUser?.email ?: ""
    obtenerUsuario(db, correoActual){ nombre, apellido, rol ->
        nombreU = nombre
        apellidoU = apellido
        rolU = rol
    }

    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val nuevoToken = task.result
            val usuarioActual = FirebaseAuth.getInstance().currentUser
            if (usuarioActual != null) {
                db.collection("users").whereEqualTo("email", correoActual).get().addOnSuccessListener { result ->
                    val usuarioRef = result.documents[0].reference
                    usuarioRef.update("token", nuevoToken)
                }
            }
        }
    }


    if(rolU == 1)
        admin = true

    Column(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Gray, Black))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 16.dp)
        ) {
            HorizontalDivider(color = Color.White, thickness = 2.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically // Centra verticalmente los elementos
            ) {
                VerticalDivider(color = Color.White, thickness = 2.dp)
                Text(
                    text = "Nombre",
                    color = White,
                    modifier = Modifier.weight(1f).fillMaxWidth().wrapContentHeight(),
                    textAlign = TextAlign.Center // Centra horizontalmente el texto,

                )
                VerticalDivider(color = Color.White, thickness = 2.dp)
                Text(
                    text = "Apellido",
                    color = White,
                    modifier = Modifier.weight(1f).fillMaxWidth().wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
                VerticalDivider(color = Color.White, thickness = 2.dp)
                Text(
                    text = "Correo",
                    color = White,
                    modifier = Modifier.weight(1f).fillMaxWidth().wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
                VerticalDivider(color = Color.White, thickness = 2.dp)
            }
            HorizontalDivider(color = Color.White, thickness = 2.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VerticalDivider(color = Color.White, thickness = 2.dp)
                Text(
                    text = nombreU,
                    color = White,
                    modifier = Modifier.weight(1f).fillMaxWidth().wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
                VerticalDivider(color = Color.White, thickness = 2.dp)
                Text(
                    text = apellidoU,
                    color = White,
                    modifier = Modifier.weight(1f).fillMaxWidth().wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
                VerticalDivider(color = Color.White, thickness = 2.dp)
                Text(
                    text = correoActual,
                    color = White,
                    modifier = Modifier.weight(1f).fillMaxWidth().wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
                VerticalDivider(color = Color.White, thickness = 2.dp)
            }
            HorizontalDivider(color = Color.White, thickness = 2.dp)
        }
        Button(
            onClick = {
                navHostController.navigate("edit")
            }, colors = ButtonDefaults.buttonColors(containerColor = Yellow),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Modificar Datos", color = Black)
        }
        Button(
            onClick = {
                navHostController.navigate("mailbox")
            }, colors = ButtonDefaults.buttonColors(containerColor = Yellow),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Buzón", color = Black)
        }
        Button(
            onClick = {
                FirebaseMessaging.getInstance().deleteToken()
                auth.signOut()
                navHostController.navigate("initial")
            }, colors = ButtonDefaults.buttonColors(containerColor = Yellow),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Cerrar Sesión", color = Black)
        }

        Button(
            onClick = {
                navHostController.navigate("send")
            }, colors = ButtonDefaults.buttonColors(containerColor = Yellow),
            modifier = Modifier.padding(16.dp).alpha(if(admin) 1f else 0f), enabled = admin
        ) {
            Text("Enviar Notificaciones", color = Black)
        }
    }

    }

fun obtenerUsuario(db: FirebaseFirestore, correo: String, onResult: (String, String, Int) -> Unit){
    db.collection("users")
        .whereEqualTo("email", correo)
        .get()
        .addOnSuccessListener { result ->
            if(!result.isEmpty){
                val usuario = result.documents[0]
                val nombre = usuario.getString("name") ?: "Desconocido"
                val apellido = usuario.getString("lastname") ?: "Desconocido"
                val rol = usuario.getLong("role")?.toInt() ?: 0
                onResult(nombre, apellido, rol)
            }else{
                onResult("No encontrado", "No encontrado", -1)
            }
        }
        .addOnFailureListener { exception ->
        println("Error al obtener datos: $exception")
        onResult("Error", "Error", -1) // Valores por defecto en caso de error
    }

}

