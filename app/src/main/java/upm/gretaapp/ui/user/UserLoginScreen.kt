package upm.gretaapp.ui.user

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

object LoginDestination : NavigationDestination {
    override val route = "login"
    override val titleRes = R.string.login
}

/**
 * Composable that represents the login screen
 *
 * @param navigateUp Function to go to the previous screen when the back arrow button is clicked
 * @param onLogin Function to go to the next screen when the login is complete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLoginScreen(
    navigateUp: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            GretaTopAppBar(
                canUseMenu = false,
                openMenu = { },
                navigateUp = navigateUp
            )
        }
    ) {innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.login),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier.padding(16.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier.padding(all = 48.dp)
            ) {
                TextField(
                    value = "",
                    onValueChange = {/*TODO*/},
                    singleLine = true,
                    label = { Text(stringResource(id = R.string.username)) },
                    modifier = Modifier.padding(8.dp)
                )
                TextField(
                    value = "",
                    onValueChange = {/*TODO*/},
                    singleLine = true,
                    label = { Text(stringResource(id = R.string.password)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                }
                                else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (passwordVisible) {
                                    stringResource(id = R.string.hide_password)
                                }
                                else {
                                    stringResource(id = R.string.show_password)
                                }
                            )
                        }

                    },
                    modifier = Modifier.padding(8.dp)
                )
                FilledTonalButton(
                    onClick = {},
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                ) {
                    Text(text = stringResource(id = R.string.forgot_password))
                }
                Button(
                    onClick = onLogin,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.login))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 650)
@Preview(showBackground = true, heightDp = 650, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun UserLoginScreenPreview() {
    GRETAAppTheme {
        UserLoginScreen(navigateUp = {}, onLogin = {})
    }
}