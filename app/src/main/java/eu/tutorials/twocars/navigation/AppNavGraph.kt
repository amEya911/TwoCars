package eu.tutorials.twocars.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.ui.screen.Game
import eu.tutorials.twocars.ui.screen.GameMenu
import eu.tutorials.twocars.ui.screen.OnboardingScreen
import eu.tutorials.twocars.ui.theme.AppTheme
import eu.tutorials.twocars.ui.viewmodel.GameViewModel
import eu.tutorials.twocars.ui.viewmodel.MenuViewModel
import eu.tutorials.twocars.util.FirebaseUtils

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    val menuViewModel: MenuViewModel = hiltViewModel()
    val menuState = menuViewModel.menuState.collectAsState().value
    val showOnboarding = menuViewModel.showOnboarding.collectAsState().value

    // Determine start destination
    val startDestination = if (showOnboarding == null) {
        return // Loading — don't render NavHost yet
    } else if (showOnboarding) {
        AppScreen.OnboardingScreen.route
    } else {
        AppScreen.GameMenuScreen.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppScreen.OnboardingScreen.route) {
            OnboardingScreen(
                onFinished = {
                    menuViewModel.markOnboardingSeen()
                    navController.navigate(AppScreen.GameMenuScreen.route) {
                        popUpTo(AppScreen.OnboardingScreen.route) { inclusive = true }
                    }
                }
            )
        }
        composable(AppScreen.GameMenuScreen.route) {
            LaunchedEffect(Unit) {
                menuViewModel.refreshUnlocks()
            }
            GameMenu(
                modifier = modifier,
                navController = navController,
                menuState = menuState,
                onGameModeSelected = { mode -> menuViewModel.setGameMode(mode) }
            )
        }
        composable("${AppScreen.GameScreen.route}/{gameId}/{gameMode}") {
            val gameViewModel: GameViewModel = hiltViewModel()
            val gameState = gameViewModel.gameState.collectAsState().value
            val gameId = it.arguments?.getString("gameId")
            val gameModeStr = it.arguments?.getString("gameMode") ?: GameMode.ENDLESS.name
            val gameMode = try { GameMode.valueOf(gameModeStr) } catch (e: Exception) { GameMode.ENDLESS }
            val themeData = FirebaseUtils.getGameThemeFromRemote(gameId)

            // Only set team ID and game mode once, not on every recomposition
            LaunchedEffect(gameId, gameMode) {
                gameViewModel.setTeamId(gameId)
                gameViewModel.setGameMode(gameMode)
            }

            AppTheme(colorScheme = themeData) {
                Game(
                    navController = navController,
                    gameId = gameId,
                    gameState = gameState,
                    viewModel = gameViewModel
                )
            }
        }
    }
}

sealed class AppScreen(val route: String) {
    data object GameScreen : AppScreen("game_screen")
    data object GameMenuScreen : AppScreen("game_menu_screen")
    data object OnboardingScreen : AppScreen("onboarding_screen")
}