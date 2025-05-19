package com.example.segundoexamen.presentation.initial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.segundoexamen.ui.theme.Black
import com.example.segundoexamen.ui.theme.Gray
import com.example.segundoexamen.ui.theme.Green
import com.example.segundoexamen.ui.theme.Yellow

@Composable
fun InitialScreen(navHostController:NavHostController){
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(Gray, Black))),
            horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(50.dp))
        Text("Segundo Examen Parcial", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Button(onClick = { navHostController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Yellow)) {
            Text(text = "Iniciar Sesion", color = Black)
        }
        Button(onClick = { navHostController.navigate("singup") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp), colors = ButtonDefaults.buttonColors(containerColor = Yellow)) {
            Text(text = "Registrarse", color = Black)
        }
    }
}