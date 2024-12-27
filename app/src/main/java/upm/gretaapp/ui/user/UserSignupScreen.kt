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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Object that represents the route of the User Signup screen
 */
object SignupDestination : NavigationDestination {
    override val route = "signup"
    override val titleRes = R.string.sign_up
}

/**
 * Composable that represents the signup screen
 *
 * @param navigateUp Function to go to the previous screen when the back arrow button is clicked
 * @param onNavigate Function to go to the next screen when the signup is complete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSignupScreen(
    navigateUp: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserSignupViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Scaffold(
        topBar = {
            GretaTopAppBar(
                canUseMenu = false,
                openMenu = { },
                navigateUp = navigateUp
            )
        }
    ) {innerPadding ->
        UserEntryBody(
            userUiState = viewModel.userUiState,
            onUserValueChange = viewModel::updateUiState,
            onSaveClick = viewModel::saveUser,
            onNavigate = onNavigate,
            modifier = modifier.padding(innerPadding)
        )
    }
}

/**
 * Body of the signup screen
 *
 * @param userUiState Object that represents the state of the screen (filled fields, loading, error)
 * @param onUserValueChange Function to update the data of the user that will be created
 * @param onSaveClick Function to save the user with all its data into the database of the app
 * @param onNavigate Function to go to the next screen when the process is complete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEntryBody(
    userUiState: UserUiState,
    onUserValueChange: (UserDetails) -> Unit,
    onSaveClick: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.sign_up),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(all = 18.dp)
        ) {
            // Scrollable column with all the fields of the form
            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.7f)
            ) {
                item {
                    // Field for the name
                    TextField(
                        value = userUiState.userDetails.name,
                        onValueChange = {
                            onUserValueChange(
                                userUiState.userDetails.copy(name = it)
                            )
                        },
                        enabled = userUiState.userState != UserState.Loading,
                        singleLine = true,
                        label = { Text(stringResource(id = R.string.name)) },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    // Field for the email
                    TextField(
                        value = userUiState.userDetails.email,
                        onValueChange = {
                            onUserValueChange(
                                userUiState.userDetails.copy(email = it)
                            )
                        },
                        enabled = userUiState.userState != UserState.Loading,
                        singleLine = true,
                        label = { Text(stringResource(id = R.string.email)) },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    // Field for the password
                    var passwordVisible by remember { mutableStateOf(false) }
                    TextField(
                        value = userUiState.userDetails.password,
                        onValueChange = {
                            onUserValueChange(
                                userUiState.userDetails.copy(password = it)
                            )
                        },
                        enabled = userUiState.userState != UserState.Loading,
                        singleLine = true,
                        label = { Text(stringResource(id = R.string.password)) },
                        // Filter to show the password or hide it depending of the button
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        // Button to show or hide the password
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
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    // Field for the gender
                    var expanded by remember { mutableStateOf(false) }
                    // Options for the gender
                    val options = listOf(
                        stringResource(id = R.string.male),
                        stringResource(id = R.string.female),
                        stringResource(id = R.string.other)
                    )

                    // Dropdown menu for selecting the gender
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        TextField(
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
                            readOnly = true,
                            value = if(userUiState.userDetails.gender == -1) {
                                ""
                            } else {
                                options[userUiState.userDetails.gender]
                            },
                            onValueChange = {},
                            enabled = userUiState.userState != UserState.Loading,
                            label = { Text(stringResource(id = R.string.gender)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            // For each option, an item of the menu is shown
                            options.forEachIndexed { index, selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        onUserValueChange(
                                            userUiState.userDetails.copy(gender = index)
                                        )
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    // Field for selecting the birthdate
                    var showBirthdayPicker by remember { mutableStateOf(false) }
                    TextField(
                        value = userUiState.userDetails.birthday,
                        onValueChange = {},
                        singleLine = true,
                        readOnly = true,
                        enabled = userUiState.userState != UserState.Loading,
                        label = { Text(stringResource(id = R.string.birthday)) },
                        // Button to show a menu to select the date
                        trailingIcon = {
                            IconButton(onClick = { showBirthdayPicker = true }) {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = stringResource(id = R.string.pick_date)
                                )
                            }
                        },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Menu for selecting the date
                    if(showBirthdayPicker) {
                        DatePickerField(
                            onDateSelected = {
                                onUserValueChange(
                                    userUiState.userDetails.copy(birthday = it)
                                )
                            },
                            onDismiss = { showBirthdayPicker = false }
                        )
                    }
                }

                item {
                    // Field for selecting the driving license year
                    var showLicensePicker by remember { mutableStateOf(false) }
                    TextField(
                        value = userUiState.userDetails.drivingLicenseYear,
                        onValueChange = { onUserValueChange(
                            userUiState.userDetails.copy(drivingLicenseYear = it)
                        ) },
                        enabled = userUiState.userState != UserState.Loading,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stringResource(id = R.string.driving_license)) },
                        // Button to select the year as a date
                        trailingIcon = {
                            IconButton(onClick = { showLicensePicker = true }) {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = stringResource(id = R.string.pick_date)
                                )
                            }
                        },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Menu for selecting a date for the field
                    if(showLicensePicker) {
                        DatePickerField(
                            onDateSelected = {
                                onUserValueChange(
                                    userUiState.userDetails.copy(drivingLicenseYear = it.split("-").first())
                                )
                            },
                            onDismiss = { showLicensePicker = false }
                        )
                    }
                }
            }

            // Indicator while the creation process is loading
            if(userUiState.userState is UserState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                )
            }
            else {
                // Button to create the user
                Button(
                    onClick = onSaveClick,
                    enabled = userUiState.isEntryValid,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth().padding(start=24.dp, end=24.dp)
                ) {
                    Text(text = stringResource(id = R.string.sign_up))
                }
            }

            // Message for errors
            if(userUiState.userState is UserState.Error) {
                Text(
                    text = stringResource(id = if (userUiState.userState.code == 2) {
                        R.string.error_signup
                    } else R.string.server_available),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // If the user is created, the app goes to the next screen
            LaunchedEffect(userUiState.userState) {
                if(userUiState.userState is UserState.Complete) {
                    onNavigate()
                }
            }
        }
    }
}

/**
 * Composable to select a date from a menu
 *
 * @param onDateSelected Function to select a date and save it
 * @param onDismiss Function to close the menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Object to select a date
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates{
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    // Variable that stores the selected date
    val selectedDate = datePickerState.selectedDateMillis?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
    } ?: ""

    // Dialog with a menu to select a date
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            // Button that saves the selected date
            Button(
                onClick = {
                    onDateSelected(selectedDate)
                    onDismiss()
                }
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            // Button that closes the dialog
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_search))
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
        )
    }
}

@Preview(showBackground = true, heightDp = 650)
@Preview(showBackground = true, heightDp = 650, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun UserSignupScreenPreview() {
    GRETAAppTheme {
        UserEntryBody(
            userUiState = UserUiState(),
            onUserValueChange = {},
            onNavigate = {},
            onSaveClick = {},
        )
    }
}