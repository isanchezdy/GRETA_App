package upm.gretaapp.ui.user

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.ui.AppViewModelProvider
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
 * @param onNavigate Function to go to the next screen when the login is complete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLoginScreen(
    navigateUp: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserLoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            GretaTopAppBar(
                canUseMenu = false,
                openMenu = { },
                navigateUp = navigateUp
            )
        }
    ) {innerPadding ->
        UserLoginBody(
            uiState = uiState,
            onLogin = {  email, password ->
                viewModel.login(email, password)
            },
            onNavigate = onNavigate,
            modifier = modifier.padding(innerPadding)
        )
    }
}

@Composable
fun UserLoginBody(
    uiState: LoginUiState,
    onLogin: (String, String) -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.login),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(all = 48.dp)
        ) {
            var email by remember{ mutableStateOf("") }
            TextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                isError = uiState is LoginUiState.Error,
                enabled = uiState != LoginUiState.Loading && uiState != LoginUiState.Complete,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                label = { Text(stringResource(id = R.string.email)) },
                modifier = Modifier.padding(8.dp)
            )
            var password by remember{ mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }
            TextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                isError = uiState is LoginUiState.Error,
                enabled = uiState != LoginUiState.Loading && uiState != LoginUiState.Complete,
                label = { Text(stringResource(id = R.string.password)) },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if(email.isNotBlank() && password.isNotBlank()) {
                        ImeAction.Done
                    }
                    else {
                        ImeAction.None
                    }
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onLogin(email, password)
                    }
                ),
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

            if(uiState is LoginUiState.Error) {
                Text(
                    text = stringResource(id = R.string.error_login),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }

            FilledTonalButton(
                onClick = {/*TODO*/},
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp),
            ) {
                Text(text = stringResource(id = R.string.forgot_password))
            }

            if(uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                    .padding(8.dp)
                )
            }
            else {
                Button(
                    onClick = {
                        onLogin(email, password)
                    },
                    enabled = email.isNotBlank() && password.isNotBlank() &&
                            uiState != LoginUiState.Complete,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.login))
                }
            }

            LaunchedEffect(uiState) {
                if(uiState is LoginUiState.Complete) {
                    onNavigate()
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
        UserLoginBody(uiState = LoginUiState.Error, onLogin = {_,_->}, onNavigate = {})
    }
}