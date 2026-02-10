package com.example.wan2gpremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wan2gpremote.gallery.GalleryScreen
import com.example.wan2gpremote.ui.main.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Wan2GpNavGraph()
        }
    }
}

@Composable
private fun Wan2GpNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(onOpenGallery = { navController.navigate("gallery") })
        }
        composable("gallery") {
            GalleryScreen(onBack = { navController.popBackStack() })
        }
    }
}
