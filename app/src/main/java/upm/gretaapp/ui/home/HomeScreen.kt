package upm.gretaapp.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import upm.gretaapp.R
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

/**
 * Object that represents the route of the Home screen
 */
object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
    override val icon: ImageVector = Icons.Filled.Abc
}

/**
 * Composable function that represents the Home screen
 *
 * @param navigateToLogin Function to go to the Login screen
 * @param navigateToSignup Function to go to the Signup screen
 * @param skipsLogin Flag that checks whether this screen can be skipped or not
 * @param onSkip Function to skip to the map screen if the user is already logged in
 */
@Composable
fun HomeScreen(
    navigateToLogin: () -> Unit,
    navigateToSignup: () -> Unit,
    modifier: Modifier = Modifier,
    skipsLogin: Boolean = true,
    onSkip: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    // Checks if the screen can be skipped or the user needs to logout
    LaunchedEffect(true) {
        delay(100.toLong())
        if(skipsLogin && viewModel.isUserLoggedIn())
            onSkip()
        else
            viewModel.logout()
    }

    HomeBody(navigateToLogin = navigateToLogin, navigateToSignup = navigateToSignup,
        modifier = modifier)
}

/**
 * Body of the screen containing the buttons and functions to log in the app
 *
 * @param navigateToLogin Function to navigate to the login screen if the button is pressed
 * @param navigateToSignup Function to navigate to the signup screen if the button is pressed
 */
@Composable
fun HomeBody(
    navigateToLogin: () -> Unit,
    navigateToSignup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.welcome),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = modifier.padding(16.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = modifier.padding(vertical = 20.dp)
                )

                // Button to login
                Button(
                    onClick = { navigateToLogin() },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 48.dp)
                ) {
                    Text(text = stringResource(id = R.string.login))
                }

                // Button to signup
                Button(
                    onClick = { navigateToSignup() },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 48.dp)
                ) {
                    Text(text = stringResource(id = R.string.sign_up))
                }

            }

            Image(
                painter = painterResource(id = R.drawable.developers),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.8f)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 650)
@Preview(showBackground = true, heightDp = 650, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun HomeScreenPreview() {
    GRETAAppTheme {
        HomeBody(navigateToLogin = {}, navigateToSignup = {})
    }
}