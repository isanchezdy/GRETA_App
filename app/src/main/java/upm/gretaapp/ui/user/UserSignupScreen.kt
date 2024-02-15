package upm.gretaapp.ui.user

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
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

object SignupDestination : NavigationDestination {
    override val route = "signup"
    override val titleRes = R.string.sign_up
}

/**
 * Composable that represent the signup screen
 *
 * @param navigateUp Function to go to the previous screen when the back arrow button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSignupScreen(
    navigateUp: () -> Unit,
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
                text = stringResource(R.string.sign_up),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier.padding(16.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
                modifier = modifier.padding(all = 48.dp)
            ) {
                LazyColumn(
                    modifier = modifier.fillMaxHeight(0.7f)
                ) {
                    item {
                        TextField(
                            value = "",
                            onValueChange = {/*TODO*/ },
                            singleLine = true,
                            placeholder = { Text(stringResource(id = R.string.first_name)) },
                            modifier = modifier.padding(vertical = 16.dp)
                        )
                    }

                    item {
                        TextField(
                            value = "",
                            onValueChange = {/*TODO*/ },
                            singleLine = true,
                            placeholder = { Text(stringResource(id = R.string.last_name)) },
                            modifier = modifier.padding(vertical = 16.dp)
                        )
                    }

                    item {
                        TextField(
                            value = "",
                            onValueChange = {/*TODO*/ },
                            singleLine = true,
                            placeholder = { Text(stringResource(id = R.string.username)) },
                            modifier = modifier.padding(vertical = 16.dp)
                        )
                    }

                    item {
                        TextField(
                            value = "",
                            onValueChange = {/*TODO*/ },
                            singleLine = true,
                            placeholder = { Text(stringResource(id = R.string.password)) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = if (passwordVisible) {
                                            stringResource(id = R.string.hide_password)
                                        } else {
                                            stringResource(id = R.string.show_password)
                                        }
                                    )
                                }
                            },
                            modifier = modifier.padding(vertical = 16.dp)
                        )
                    }

                    item {
                        var expanded by remember { mutableStateOf(false) }
                        val options = listOf(
                            stringResource(id = R.string.male),
                            stringResource(id = R.string.female),
                            stringResource(id = R.string.other)
                        )
                        var gender by remember { mutableStateOf("") }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = modifier.padding(vertical = 16.dp)
                        ) {
                            TextField(
                                modifier = Modifier.menuAnchor(),
                                readOnly = true,
                                value = gender,
                                onValueChange = {},
                                placeholder = { Text(stringResource(id = R.string.gender)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
                                options.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            gender = selectionOption
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        TextField(
                            value = "",
                            onValueChange = {/*TODO*/ },
                            singleLine = true,
                            placeholder = { Text(stringResource(id = R.string.birthday)) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = if (passwordVisible) {
                                            stringResource(id = R.string.hide_password)
                                        } else {
                                            stringResource(id = R.string.show_password)
                                        }
                                    )
                                }
                            },
                            modifier = modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                Button(
                    onClick = {},
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.sign_up))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 650)
@Preview(showBackground = true, heightDp = 650, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun UserSignupScreenPreview() {
    GRETAAppTheme {
        UserSignupScreen(navigateUp = {})
    }
}