package com.example.segundoexamen

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.segundoexamen.presentation.home.HomeScreen
import com.example.segundoexamen.presentation.home.EditScreen
import com.example.segundoexamen.presentation.home.MailboxScreen
import com.example.segundoexamen.presentation.home.SendScreen
import com.example.segundoexamen.presentation.initial.InitialScreen
import com.example.segundoexamen.presentation.login.LoginScreen
import com.example.segundoexamen.presentation.singup.SingupScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {
    NavHost(navController = navHostController, startDestination = "initial") {
        composable("initial") {
            InitialScreen(navHostController) // Pasamos solo el controlador
        }
        composable("login") {
            LoginScreen(navHostController, auth, db)
        }
        composable("singup") {
            SingupScreen(navHostController, auth, db)
        }
        composable("home") {
            HomeScreen(navHostController, auth, db)
        }
        composable("edit") {
            EditScreen(navHostController, auth, db)
        }
        composable("mailbox"){
            MailboxScreen(navHostController, auth, db)
        }
        composable("send"){
            SendScreen(navHostController, auth, db)
        }
    }
}
