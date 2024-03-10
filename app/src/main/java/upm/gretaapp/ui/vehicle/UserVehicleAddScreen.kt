package upm.gretaapp.ui.vehicle

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewmodel.compose.viewModel
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.model.Vehicle
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

object VehicleAddDestination : NavigationDestination {
    override val route = "add_vehicle"
    override val titleRes = R.string.add_vehicle
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleAddScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserVehicleAddViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = false, navigateUp = onNavigateUp)
        }
    ) { paddingValues ->
        val uiState by viewModel.vehicleUiState.collectAsState()
        VehicleEntryBody(
            navigateBack = navigateBack,
            uiState = uiState,
            onCreate = viewModel::saveVehicle,
            search = viewModel::getVehicles,
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
fun VehicleEntryBody(
    navigateBack: () -> Unit,
    uiState: List<Vehicle>,
    onCreate: (Long, Long?, Long?) -> Unit,
    search: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.add_vehicle),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        VehicleInputForm(
            navigateBack = navigateBack,
            uiState = uiState,
            onCreate = onCreate,
            search = search
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleInputForm(
    navigateBack: () -> Unit,
    uiState: List<Vehicle>,
    onCreate: (Long, Long?, Long?) -> Unit,
    search: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {
        var selectedDefaultVehicle by rememberSaveable { mutableIntStateOf(-1) }
        var searchVehicle by rememberSaveable { mutableStateOf("") }
        var selectedVehicleID: Long? by rememberSaveable { mutableStateOf(null) }
        val defaultLabels = listOf(
            stringResource(id = R.string.segment_b), stringResource(id = R.string.segment_c),
            stringResource(id = R.string.segment_d), stringResource(id = R.string.segment_e)
        )

        Text(
            text = stringResource(id = R.string.default_vehicles),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier.padding(top = 16.dp)
        )

        for (i in 0..3 step 2) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            ) {
                Row {
                    RadioButton(
                        modifier = modifier.size(20.dp),
                        selected = (i == selectedDefaultVehicle),
                        onClick = {
                            selectedDefaultVehicle = i
                            searchVehicle = ""
                            selectedVehicleID = -(i + 1).toLong()
                        }
                    )
                    Text(
                        text = defaultLabels[i],
                        modifier = modifier
                            .padding(horizontal = 8.dp)
                            .clickable {
                                selectedDefaultVehicle = i
                                searchVehicle = ""
                                selectedVehicleID = -(i + 1).toLong()
                            }
                    )
                }

                Row {
                    RadioButton(
                        modifier = modifier.size(20.dp),
                        selected = ((i + 1) == selectedDefaultVehicle),
                        onClick = {
                            selectedDefaultVehicle = i + 1
                            searchVehicle = ""
                            selectedVehicleID = -(i + 2).toLong()
                        }
                    )
                    Text(
                        text = defaultLabels[i + 1],
                        modifier = modifier
                            .padding(start = 8.dp)
                            .clickable {
                                selectedDefaultVehicle = (i + 1)
                                searchVehicle = ""
                                selectedVehicleID = -(i + 2).toLong()
                            }
                    )
                }
            }
        }

        var expanded by remember { mutableStateOf(false) }

        Text(
            text = stringResource(id = R.string.advanced_search),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier.padding(8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(16.dp)
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                label = { Text(stringResource(id = R.string.brand_model)) },
                value = searchVehicle,
                singleLine = true,
                onValueChange = { searchVehicle = it.trim()
                        expanded = true },
                keyboardOptions = KeyboardOptions(
                    imeAction = if(searchVehicle.isNotBlank()) ImeAction.Search else ImeAction.None
                ),
                keyboardActions = KeyboardActions(onSearch = { search(searchVehicle) }),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                uiState.forEach {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = { searchVehicle = it.name
                                selectedVehicleID = it.vehicleID
                                selectedDefaultVehicle = -1
                                expanded = false
                            }
                        )
                    }
            }
        }


        Text(
            text = stringResource(id = R.string.veh_additional_info),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier.padding(8.dp)
        )

        var age: Long? by remember { mutableStateOf(null) }
        TextField(
            value = if (age == null) {
                ""
            } else {
                age.toString()
            },
            onValueChange = {
                if (it.length < 3) {
                    if (it.isBlank()) {
                        age = null
                    } else if (it.isDigitsOnly()) {
                        age = it.toLong()
                    }
                }
            },
            label = { Text(stringResource(id = R.string.age)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.padding(8.dp)
        )

        var kmUsed: Long? by remember { mutableStateOf(null) }
        TextField(
            value = if (kmUsed == null) {
                ""
            } else {
                kmUsed.toString()
            },
            onValueChange = {
                if (it.isBlank()) {
                    kmUsed = null
                } else if (it.isDigitsOnly()) {
                    kmUsed = it.toLong()
                }
            },
            label = { Text(stringResource(id = R.string.travelledKm)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier.padding(8.dp)
        )


        Button(
            onClick = {
                selectedVehicleID?.let { onCreate(it, age, kmUsed) }
                navigateBack()
            },
            shape = MaterialTheme.shapes.small,
            enabled = (selectedVehicleID != null),
            modifier = modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
        ) {
            Text(stringResource(id = R.string.save))
        }
    }
}

@Preview(showBackground = true, heightDp = 650)
@Preview(
    showBackground = true,
    heightDp = 650,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "es"
)
@Composable
fun VehicleAddScreenPreview() {
    GRETAAppTheme {
        VehicleEntryBody(uiState = emptyList(), onCreate = { _, _, _ -> }, search = {},
            navigateBack = {})
    }
}

/*@Composable
fun ItemsVehicle(
    vehicle: String,
    onSelect: (String) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect(vehicle)
            }
            .padding(10.dp)
    ) {
        Text(text = vehicle, fontSize = 16.sp)
    }

}*/