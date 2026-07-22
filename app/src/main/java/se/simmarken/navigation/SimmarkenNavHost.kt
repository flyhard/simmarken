package se.simmarken.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import se.simmarken.SimmarkenApplication
import se.simmarken.ui.home.HomeScreen
import se.simmarken.ui.home.HomeViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SimmarkenNavHost() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as SimmarkenApplication

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(application.container.kidRepository),
            )
            HomeScreen(viewModel = viewModel)
        }
    }
}
