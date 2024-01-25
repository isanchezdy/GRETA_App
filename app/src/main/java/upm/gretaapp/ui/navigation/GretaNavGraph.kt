package upm.gretaapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import upm.gretaapp.ui.home.HomeDestination
import upm.gretaapp.ui.home.HomeScreen
import upm.gretaapp.ui.user.LoginDestination
import upm.gretaapp.ui.user.SignupDestination
import upm.gretaapp.ui.user.UserLoginScreen
import upm.gretaapp.ui.user.UserSignupScreen
import upm.gretaapp.ui.vehicle.VehicleListDestination
import upm.gretaapp.ui.vehicle.VehicleListScreen

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun GretaNavHost(
    navController: NavHostController,
    openMenu: () -> Unit,
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
            UserLoginScreen(
                navigateUp = { navController.navigateUp() },
                onLogin = { navController.navigate(VehicleListDestination.route) }
            )
        }
        composable(route = SignupDestination.route) {
            UserSignupScreen(navigateUp = { navController.navigateUp() })
        }
        composable(route = VehicleListDestination.route) {
            VehicleListScreen(
                onVehicleAdd = { /*TODO*/ },
                onVehicleDelete = { },
                onVehicleEdit = { },
                onVehicleFav = { },
                openMenu = openMenu
            )
        }
    }
}