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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import upm.gretaapp.R
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
 */
@Composable
fun HomeScreen(
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

                // Button to start the app without a login
                FilledTonalButton(
                    onClick = {/*TODO*/},
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 48.dp),
                ) {
                    Text(text = stringResource(id = R.string.no_user))
                }
            }

            Image(
                painter = painterResource(id = R.drawable.developers),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = modifier.padding(vertical = 16.dp)
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
        HomeScreen(navigateToLogin = {}, navigateToSignup = {})
    }
}