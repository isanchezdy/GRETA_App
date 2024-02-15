package upm.gretaapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import upm.gretaapp.ui.home.HomeDestination
import upm.gretaapp.ui.home.HomeScreen
import upm.gretaapp.ui.map.MapDestination
import upm.gretaapp.ui.map.MapScreen
import upm.gretaapp.ui.user.LoginDestination
import upm.gretaapp.ui.user.SignupDestination
import upm.gretaapp.ui.user.UserLoginScreen
import upm.gretaapp.ui.user.UserSignupScreen
import upm.gretaapp.ui.vehicle.VehicleListDestination
import upm.gretaapp.ui.vehicle.VehicleListScreen

/**
 * Provides Navigation graph for the application.
 *
 * @param navController [NavHostController] to navigate to other screens when needed
 * @param openMenu Function to open the menu when the button is pressed
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
                onLogin = { navController.navigate(MapDestination.route){ popUpTo(0) } }
            )
        }
        composable(route = SignupDestination.route) {
            UserSignupScreen(navigateUp = { navController.navigateUp() })
        }
        composable(route = MapDestination.route) {
            MapScreen(openMenu = openMenu)
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