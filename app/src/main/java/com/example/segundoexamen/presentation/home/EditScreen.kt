package com.example.segundoexamen.presentation.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.segundoexamen.R
import com.example.segundoexamen.presentation.model.User
import com.example.segundoexamen.presentation.singup.comprobarCorreo
import com.example.segundoexamen.ui.theme.Black
import com.example.segundoexamen.ui.theme.SelectedField
import com.example.segundoexamen.ui.theme.UnselectedField
import com.example.segundoexamen.ui.theme.White
import com.example.segundoexamen.ui.theme.Yellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditScreen(navHostController: NavHostController, auth: FirebaseAuth, db: FirebaseFirestore){
    var correoActual by remember { mutableStateOf("") }
    var nombreU by remember { mutableStateOf("") }
    var apellidoU by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var descerror by remember { mutableStateOf("") }
    var carga by remember { mutableStateOf(true) }
    var nombreEditable by remember { mutableStateOf("") }
    var apellidoEditable by remember { mutableStateOf("")}


    var confirmacion by remember {mutableStateOf(0)}

    correoActual = auth.currentUser?.email ?: ""
    obtenerUsuario(db, correoActual){ nombre, apellido, rol ->
        nombreU = nombre
        apellidoU = apellido
        if (nombreEditable.isEmpty()) { // Solo lo actualiza si el usuario no lo ha cambiado
            nombreEditable = nombreU
        }
        if (apellidoEditable.isEmpty()) { // Solo lo actualiza si el usuario no lo ha cambiado
            apellidoEditable = apellidoU
        }
    }



    Column(modifier = Modifier
        .fillMaxSize()
        .background(Black)
        .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally){

        Row(){
            Icon(painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "",
                tint = White,
                modifier = Modifier.padding(vertical = 24.dp).size(24.dp).clickable {
                    navHostController.navigate("home")
                })
            Spacer(modifier = Modifier.weight(1f))
        }
        Text("Nombre", color = White, fontWeight = FontWeight.Bold, fontSize = 35.sp)
        TextField(value = nombreEditable,
            onValueChange = {nombreEditable = it},
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            ))
        Spacer(modifier = Modifier.height(48.dp))
        Text("Apellido Paterno", color = White, fontWeight = FontWeight.Bold, fontSize = 35.sp)
        TextField(value = apellidoEditable,
            onValueChange = {apellidoEditable = it},
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            ))
        Spacer(Modifier.height(48.dp))
        Button(onClick = {
            if(contieneNumeros(nombreEditable) || contieneNumeros(apellidoEditable)){
                error = true
                descerror = "El nombre y el apellido no pueden contener numeros"
                return@Button
            }

            carga = false
            db.collection("users")
                .whereEqualTo("email", correoActual)
                .get()
                .addOnSuccessListener { result ->
                    if(!result.isEmpty){
                        val usuarioRef = result.documents[0].reference
                        usuarioRef.update("name", nombreEditable).addOnSuccessListener {
                            confirmacion++
                        }
                        usuarioRef.update("lastname", apellidoEditable).addOnSuccessListener {
                            confirmacion++
                        }


                    }
                }
        },
            enabled = carga, colors = ButtonDefaults.buttonColors(containerColor = Yellow)){
            Text(text = "Editar", color = Black)
        }
        if(error){
            AlertDialog(
                onDismissRequest = {error = false},
                title = { Text("Error") },
                text = { Text(descerror) },
                confirmButton = { }
            )
        }

        if(confirmacion == 2){
            AlertDialog(
                onDismissRequest = {confirmacion = 0},
                title = { Text("Exito") },
                text = { Text("Los datos se han actualizado correctamente") },
                confirmButton = { }
            )
            navHostController.navigate("home")
        }


    }
}

fun contieneNumeros(texto: String): Boolean {
    return texto.any { it.isDigit() }
}

