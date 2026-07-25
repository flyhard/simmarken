package se.simmarken.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import se.simmarken.SimmarkenApplication
import se.simmarken.ui.badge.BadgeDetailScreen
import se.simmarken.ui.badge.BadgeDetailViewModel
import se.simmarken.ui.child.ChildCatalogScreen
import se.simmarken.ui.child.ChildCatalogViewModel
import se.simmarken.ui.home.HomeScreen
import se.simmarken.ui.home.HomeViewModel
import se.simmarken.ui.settings.SettingsScreen
import se.simmarken.ui.settings.SettingsViewModel

@Composable
fun SimmarkenNavHost() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as SimmarkenApplication
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            application = application,
            kidRepository = application.container.kidRepository,
            exportRepository = application.container.exportRepository,
            localePreferencesRepository = application.container.localePreferencesRepository,
        ),
    )

    LaunchedEffect(application) {
        application.container.pendingImportUri.collect { uri ->
            if (uri != null) {
                navController.navigate(Settings) {
                    launchSingleTop = true
                }
                settingsViewModel.onImportUriReceived(uri)
                application.container.pendingImportUri.value = null
            }
        }
    }

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    kidRepository = application.container.kidRepository,
                    catalogRepository = application.container.catalogRepository,
                    progressRepository = application.container.progressRepository,
                ),
            )
            HomeScreen(
                viewModel = viewModel,
                navController = navController,
                onSettingsClick = { navController.navigate(Settings) },
            )
        }
        composable<Settings> {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable<ChildCatalog> { backStackEntry ->
            val route = backStackEntry.toRoute<ChildCatalog>()
            val catalogViewModel: ChildCatalogViewModel = viewModel(
                factory = ChildCatalogViewModelFactory(
                    kidRepository = application.container.kidRepository,
                    catalogRepository = application.container.catalogRepository,
                    progressRepository = application.container.progressRepository,
                    kidId = route.kidId,
                ),
            )
            ChildCatalogScreen(
                viewModel = catalogViewModel,
                onBack = { navController.popBackStack() },
                onBadgeClick = { badgeId ->
                    navController.navigate(BadgeDetail(kidId = route.kidId, badgeId = badgeId))
                },
            )
        }
        composable<BadgeDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<BadgeDetail>()
            val detailViewModel: BadgeDetailViewModel = viewModel(
                factory = BadgeDetailViewModelFactory(
                    kidId = route.kidId,
                    badgeId = route.badgeId,
                    catalogRepository = application.container.catalogRepository,
                    progressRepository = application.container.progressRepository,
                ),
            )
            BadgeDetailScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
