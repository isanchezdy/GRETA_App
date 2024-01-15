package upm.gretaapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import upm.gretaapp.ui.home.HomeDestination
import upm.gretaapp.ui.home.HomeScreen
import upm.gretaapp.ui.user.LoginDestination
import upm.gretaapp.ui.user.SignupDestination
import upm.gretaapp.ui.user.UserLoginScreen

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun GretaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToLogin = { navController.navigate(LoginDestination.route) },
                navigateToSignup = { navController.navigate(SignupDestination.route) }
            )
        }
        composable(route = LoginDestination.route) {
            UserLoginScreen(navigateUp = { navController.navigateUp() })
        }
    }
}