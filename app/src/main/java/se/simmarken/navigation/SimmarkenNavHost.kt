package se.simmarken.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.simmarken.ui.home.HomePlaceholderScreen

@Composable
fun SimmarkenNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomePlaceholderScreen()
        }
    }
}
