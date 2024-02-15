package upm.gretaapp.ui.vehicle

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.model.Vehicle
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

object VehicleListDestination : NavigationDestination {
    override val route = "vehicles"
    override val titleRes = R.string.my_vehicles
    override val icon: ImageVector = Icons.Filled.DirectionsCar
}

/**
 * Composable that represents the Vehicle List screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    onVehicleAdd: () -> Unit,
    onVehicleDelete: (Int) -> Unit,
    onVehicleEdit: (Int) -> Unit,
    onVehicleFav: (Int) -> Unit,
    openMenu: () -> Unit,
    //viewModel: VehicleListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    //val vehicleListUiState by viewModel.vehicleListUiState.collectAsState()
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = true, openMenu = openMenu, navigateUp = { })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onVehicleAdd,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_vehicle)
                )
            }
        }
    ) {
        VehicleListBody(
            //vehicleList = vehicleListUiState.vehicleList,
            vehicleList = emptyList(),
            onVehicleDelete = onVehicleDelete,
            onVehicleEdit = onVehicleEdit,
            onVehicleFav = onVehicleFav,
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
private fun VehicleListBody(
    vehicleList: List<Vehicle>,
    onVehicleDelete: (Int) -> Unit,
    onVehicleEdit: (Int) -> Unit,
    onVehicleFav: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.my_vehicles),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (vehicleList.isEmpty()) {
                 Arrangement.Center
            } else {
                 Arrangement.Top
            },
            modifier = modifier.padding(8.dp)
        ) {

            if (vehicleList.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.no_vehicle_description),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.padding(22.dp)
                )
            } else {
                VehicleList(
                    vehicleList = vehicleList,
                    onVehicleDelete = onVehicleDelete,
                    onVehicleEdit = onVehicleEdit,
                    onVehicleFav = onVehicleFav,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun VehicleList(
    vehicleList: List<Vehicle>,
    onVehicleDelete: (Int) -> Unit,
    onVehicleEdit: (Int) -> Unit,
    onVehicleFav: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState, modifier = modifier) {
        items(items = vehicleList, key = { it.id }) {vehicle ->
            VehicleItem(
                vehicle = vehicle,
                onVehicleDelete = onVehicleDelete,
                onVehicleEdit = onVehicleEdit,
                onVehicleFav = onVehicleFav,
            )
        }
    }
}

@Composable
private fun VehicleItem(
    vehicle: Vehicle,
    onVehicleDelete: (Int) -> Unit,
    onVehicleEdit: (Int) -> Unit,
    onVehicleFav: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context = LocalContext.current)
                        .data(vehicle.photo)
                        .crossfade(true)
                        .build(),
                    error = painterResource(R.drawable.ic_broken_image),
                    placeholder = painterResource(R.drawable.loading_img),
                    contentDescription = stringResource(R.string.vehicle_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(8.dp)
                )


                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(0.5f)
                        .aspectRatio(1.4f)
                ) {
                    Text(text = vehicle.brand, style = MaterialTheme.typography.titleMedium)
                    Text(text = vehicle.model, style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = vehicle.year.toString(), style = MaterialTheme.typography.titleMedium)
                Text(text = vehicle.motorType, style = MaterialTheme.typography.titleMedium)
            }
            
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onVehicleDelete(vehicle.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.delete_vehicle)
                    )
                }

                IconButton(onClick = { onVehicleEdit(vehicle.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.edit_vehicle)
                    )
                }

                IconButton(onClick = { onVehicleFav(vehicle.id) }) {
                    Icon(
                        imageVector = Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.mark_vehicle)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 650)
@Preview(showBackground = true, heightDp = 650, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun VehicleEmptyListScreenPreview() {
    GRETAAppTheme {
        VehicleListBody(
            vehicleList = emptyList(),
            onVehicleDelete = {},
            onVehicleEdit = {},
            onVehicleFav = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 650, widthDp = 400)
@Preview(showBackground = true, heightDp = 650, widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun VehicleListScreenPreview() {
    GRETAAppTheme {
        VehicleListBody(
            vehicleList = listOf(
                Vehicle(type = "Car", brand = "Tesla", model = "000", kilometers = 10000, motorType = "Gasoline", year = 2023, photo = "", user = 0),
                Vehicle(id = 1, type = "Car", brand = "Tesla", model = "000", kilometers = 10000, motorType = "Gasoline", year = 2023, photo = "", user = 0),
                Vehicle(id = 2, type = "Car", brand = "Tesla", model = "000", kilometers = 10000, motorType = "Gasoline", year = 2023, photo = "", user = 0)
            ),
            onVehicleDelete = {},
            onVehicleEdit = {},
            onVehicleFav = {})
    }
}

