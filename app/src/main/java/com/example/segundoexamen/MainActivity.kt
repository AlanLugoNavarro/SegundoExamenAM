package com.example.segundoexamen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.segundoexamen.ui.theme.SegundoExamenTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var navHostController: NavHostController
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        enableEdgeToEdge()

        setContent {
            navHostController = rememberNavController()

            LaunchedEffect(auth.currentUser) {
                if (auth.currentUser != null) {
                    navHostController.navigate("home")
                }
            }
            SegundoExamenTheme {
                NavigationWrapper(navHostController, auth, db)
            }
        }
    }
}
