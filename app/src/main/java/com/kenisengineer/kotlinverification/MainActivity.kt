package com.kenisengineer.kotlinverification

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kenisengineer.kotlinverification.core.navigation.Routes
import com.kenisengineer.kotlinverification.core.ui.theme.KotlinVerificationTheme
import com.kenisengineer.kotlinverification.feature.liststability.ListStabilityRoute
import com.kenisengineer.kotlinverification.home.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinVerificationTheme {
                KotlinVerificationApp()
            }
        }
    }
}

@Composable
fun KotlinVerificationApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onOpenVerification = { route -> navController.navigate(route) })
        }
        composable(Routes.LIST_STABILITY) {
            ListStabilityRoute(onBack = { navController.popBackStack() })
        }
    }
}
