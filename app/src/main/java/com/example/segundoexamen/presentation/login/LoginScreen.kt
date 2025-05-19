package com.example.segundoexamen.presentation.login

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.segundoexamen.R
import com.example.segundoexamen.presentation.singup.comprobarCorreo
import com.example.segundoexamen.ui.theme.Black
import com.example.segundoexamen.ui.theme.Gray
import com.example.segundoexamen.ui.theme.SelectedField
import com.example.segundoexamen.ui.theme.UnselectedField
import com.example.segundoexamen.ui.theme.White
import com.example.segundoexamen.ui.theme.Yellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navHostController:NavHostController, auth: FirebaseAuth, db: FirebaseFirestore) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var descerror by remember { mutableStateOf("") }
    var carga by remember { mutableStateOf(true) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(Gray, Black)))
        .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally){

        Row(){
            Icon(painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "",
                tint = White,
                modifier = Modifier.padding(vertical = 24.dp).size(24.dp).clickable {
                    navHostController.navigate("initial")
                })
            Spacer(modifier = Modifier.weight(1f))
        }

        Text("Correo Electronico", color = White, fontWeight = FontWeight.Bold, fontSize = 35.sp)
        TextField(value = email,
            onValueChange = {email = it},
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            ))
        Spacer(modifier = Modifier.height(48.dp))
        Text("Contraseña", color = White, fontWeight = FontWeight.Bold, fontSize = 35.sp)
        TextField(value = password,
            onValueChange = {password = it},
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            ))
        Spacer(Modifier.height(48.dp))
        Button(onClick = {

            if(email.isEmpty() || password.isEmpty()){
                error = true
                descerror = "Debe rellenar todos los campos"
                return@Button
            }

            if(!esCorreoValido(email.lowercase())){
                error = true
                descerror = "El correo no es valido"
                return@Button
            }

            if(password.length < 8){
                error = true
                descerror = "La contraseña debe tener al menos 8 caracteres"
                return@Button
            }



            auth.signInWithEmailAndPassword(email.lowercase(), password).addOnCompleteListener {task ->
                if(task.isSuccessful){
                    carga = false
                    navHostController.navigate("home")
                }else{
                    comprobarCorreo(db, email.lowercase()){
                        if(!it){
                            error = true
                            descerror = "Correo no encontrado"
                            return@comprobarCorreo
                        }
                    }
                    comprobarCorreo(db, email.lowercase()){
                        if(it){
                            error = true
                            descerror = "Contraseña erronea"
                            return@comprobarCorreo
                        }
                    }

                }
            }

        }, enabled = carga, colors = ButtonDefaults.buttonColors(containerColor = Yellow)){
            Text(text = "Iniciar Sesion", color = Black)
        }
        if(error){
            AlertDialog(
                onDismissRequest = {error = false},
                title = { Text("Error") },
                text = { Text(descerror) },
                confirmButton = { }
            )
        }
    }
}

fun esCorreoValido(email: String): Boolean {
    val regex = "^[\\w.-]+@[\\w-]+\\.[a-z]{2,4}$".toRegex(RegexOption.IGNORE_CASE)
    return regex.matches(email)
}