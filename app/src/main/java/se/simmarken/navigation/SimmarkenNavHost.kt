package se.simmarken.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import se.simmarken.SimmarkenApplication
import se.simmarken.ui.badge.BadgeDetailPlaceholderScreen
import se.simmarken.ui.badge.BadgeDetailViewModel
import se.simmarken.ui.child.ChildCatalogScreen
import se.simmarken.ui.child.ChildCatalogViewModel
import se.simmarken.ui.home.HomeScreen
import se.simmarken.ui.home.HomeViewModel

@Composable
fun SimmarkenNavHost() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as SimmarkenApplication

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(application.container.kidRepository),
            )
            HomeScreen(viewModel = viewModel, navController = navController)
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
            BadgeDetailPlaceholderScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
