package eu.tutorials.twocars.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import eu.tutorials.twocars.ui.screen.Game
import eu.tutorials.twocars.ui.screen.GameMenu
import eu.tutorials.twocars.ui.theme.AppTheme
import eu.tutorials.twocars.util.FirebaseUtils

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.GameMenuScreen.route
    ) {
        composable(AppScreen.GameMenuScreen.route) {
            GameMenu(modifier = modifier, navController = navController)
        }
        composable("${AppScreen.GameScreen.route}/{gameId}") {
            val gameId = it.arguments?.getString("gameId")
            val themeData = FirebaseUtils.getGameThemeFromRemote(gameId)

            AppTheme(colorScheme = themeData) {
                Game(
                    navController = navController,
                    gameId = gameId
                )
            }
        }

    }
}

sealed class AppScreen(val route: String) {
    data object GameScreen: AppScreen("game_screen")
    data object GameMenuScreen: AppScreen("game_menu_screen")
}