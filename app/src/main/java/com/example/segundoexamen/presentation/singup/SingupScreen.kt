package com.example.segundoexamen.presentation.singup

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
import com.example.segundoexamen.presentation.home.contieneNumeros
import com.example.segundoexamen.presentation.login.esCorreoValido
import com.example.segundoexamen.presentation.model.User
import com.example.segundoexamen.ui.theme.Black
import com.example.segundoexamen.ui.theme.Gray
import com.example.segundoexamen.ui.theme.SelectedField
import com.example.segundoexamen.ui.theme.UnselectedField
import com.example.segundoexamen.ui.theme.White
import com.example.segundoexamen.ui.theme.Yellow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun SingupScreen(navHostController:NavHostController, auth: FirebaseAuth, db:FirebaseFirestore) {
    var name by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var descerror by remember { mutableStateOf("") }
    var carga by remember { mutableStateOf(true) }

    Column(modifier = Modifier
        .fillMaxSize()
        .fillMaxHeight()
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
        Text("Nombre", color = White, fontWeight = FontWeight.Bold, fontSize = 35.sp)
        TextField(value = name,
            onValueChange = {name = it},
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            ))
        Spacer(modifier = Modifier.height(48.dp))
        Text("Apellido Paterno", color = White, fontWeight = FontWeight.Bold, fontSize = 35.sp)
        TextField(value = lastname,
            onValueChange = {lastname = it},
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            ))
        Spacer(modifier = Modifier.height(48.dp))
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
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = UnselectedField,
                focusedContainerColor = SelectedField
            ))
        Spacer(Modifier.height(48.dp))
        Button(onClick = {
            if(name.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()){
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

            if(contieneNumeros(name.lowercase()) || contieneNumeros(lastname.lowercase())){
                error = true
                descerror = "El nombre y el apellido no pueden contener numeros"
                return@Button
            }

            comprobarCorreo(db, email){
                if(it){
                    error = true
                    descerror = "El correo ya existe"
                }
            }


            if(error){
                return@Button
            }
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                 //Completado
                    carga = false
                    val role: Int
                    if(password == "admin123")
                        role = 1
                    else
                        role = 0
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->

                        if(task.isSuccessful){
                            val token = task.result
                            val user = User(name.lowercase(), lastname.lowercase(), email.lowercase(), role, token)
                            db.collection("users").add(user)
                                .addOnSuccessListener {
                                    navHostController.navigate("home")
                                }
                                .addOnFailureListener {
                                    Log.i("Admin", "Error al registrar el usuario en la base de datos")
                                }
                        }else{
                            carga = true
                        }
                    }
                }else{
                    Log.i("Admin", "Error al registrar la autenticacion del usuario")
                }
            }
        },
            enabled = carga, colors = ButtonDefaults.buttonColors(containerColor = Yellow)){
            Text(text = "Registrarse", color = Black)
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

fun comprobarCorreo(db: FirebaseFirestore, email: String, onResult: (Boolean) -> Unit){
    db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
        onResult(!result.isEmpty)
    }
        .addOnFailureListener {
            onResult(false)
        }
}