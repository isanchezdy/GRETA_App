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
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.7f)
            ) {
                item {
                    TextField(
                        value = userUiState.userDetails.name,
                        onValueChange = {
                            onUserValueChange(
                                userUiState.userDetails.copy(name = it)
                            )
                        },
                        enabled = userUiState.userState != UserState.LOADING,
                        singleLine = true,
                        label = { Text(stringResource(id = R.string.name)) },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    TextField(
                        value = userUiState.userDetails.email,
                        onValueChange = {
                            onUserValueChange(
                                userUiState.userDetails.copy(email = it)
                            )
                        },
                        enabled = userUiState.userState != UserState.LOADING,
                        singleLine = true,
                        label = { Text(stringResource(id = R.string.email)) },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    var passwordVisible by remember { mutableStateOf(false) }
                    TextField(
                        value = userUiState.userDetails.password,
                        onValueChange = {
                            onUserValueChange(
                                userUiState.userDetails.copy(password = it)
                            )
                        },
                        enabled = userUiState.userState != UserState.LOADING,
                        singleLine = true,
                        label = { Text(stringResource(id = R.string.password)) },
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
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    var expanded by remember { mutableStateOf(false) }
                    val options = listOf(
                        stringResource(id = R.string.male),
                        stringResource(id = R.string.female),
                        stringResource(id = R.string.other)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        TextField(
                            modifier = Modifier.menuAnchor(),
                            readOnly = true,
                            value = if(userUiState.userDetails.gender == -1) {
                                ""
                            } else {
                                options[userUiState.userDetails.gender]
                            },
                            onValueChange = {},
                            enabled = userUiState.userState != UserState.LOADING,
                            label = { Text(stringResource(id = R.string.gender)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
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
                    var showBirthdayPicker by remember { mutableStateOf(false) }
                    TextField(
                        value = userUiState.userDetails.birthday,
                        onValueChange = {},
                        singleLine = true,
                        readOnly = true,
                        enabled = userUiState.userState != UserState.LOADING,
                        label = { Text(stringResource(id = R.string.birthday)) },
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
                    var showLicensePicker by remember { mutableStateOf(false) }
                    TextField(
                        value = userUiState.userDetails.drivingLicenseDate,
                        onValueChange = { },
                        enabled = userUiState.userState != UserState.LOADING,
                        singleLine = true,
                        readOnly = true,
                        label = { Text(stringResource(id = R.string.driving_license)) },
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

                    if(showLicensePicker) {
                        DatePickerField(
                            onDateSelected = {
                                onUserValueChange(
                                    userUiState.userDetails.copy(drivingLicenseDate = it)
                                )
                            },
                            onDismiss = { showLicensePicker = false }
                        )
                    }
                }
            }

            if(userUiState.userState == UserState.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                )
            }
            else {
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

            if(userUiState.userState == UserState.ERROR) {
                Text(
                    text = stringResource(id = R.string.error_signup),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }

            LaunchedEffect(userUiState.userState) {
                if(userUiState.userState == UserState.COMPLETE) {
                    onNavigate()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates{
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    val selectedDate = datePickerState.selectedDateMillis?.let {
        SimpleDateFormat("dd - MMMM - yyyy", Locale.getDefault()).format(Date(it))
    } ?: ""

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
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