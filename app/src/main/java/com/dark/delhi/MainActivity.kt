package com.dark.delhi

import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import android.os.Bundle
import androidx.activity.ComponentActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // NavController create karein
            val navController = rememberNavController()

            // AppNavGraph ko call karein
            AppNavGraph(navController = navController)
        }
    }
}