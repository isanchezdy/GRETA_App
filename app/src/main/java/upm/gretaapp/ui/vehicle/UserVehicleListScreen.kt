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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import upm.gretaapp.model.getMotorType
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

/**
 * Object that represents the route of the User Vehicle List screen
 */
object VehicleListDestination : NavigationDestination {
    override val route = "vehicles"
    override val titleRes = R.string.my_vehicles
    override val icon: ImageVector = Icons.Filled.DirectionsCar
}

/**
 * Composable that represents the User Vehicle List screen
 *
 * @param onVehicleAdd Function to add a vehicle to the list
 * @param openMenu Function to open the menu of the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    onVehicleAdd: () -> Unit,
    openMenu: () -> Unit,
    viewModel: UserVehicleListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val userVehicleListUiState = viewModel.userVehicleListUiState
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = true, openMenu = openMenu, navigateUp = { })
        },
        floatingActionButton = {
            // Button to add new vehicles to the list
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
        // Observer to reload vehicles in case a new one is added with the button
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val lifecycleObserver = rememberVehiclesLifecycleObserver(viewModel = viewModel)
        DisposableEffect(lifecycle) {
            lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycle.removeObserver(lifecycleObserver)
            }
        }

        VehicleListBody(
            uiState = userVehicleListUiState,
            onVehicleDelete = viewModel::deleteVehicle,
            onVehicleFav = viewModel::setFavourite,
            modifier = Modifier.padding(it)
        )
    }
}

/**
 * Body of the user vehicle list
 *
 * @param uiState State of the screen, containing the information about its elements
 * @param onVehicleDelete Function to delete vehicles from the list with a button
 * @param onVehicleFav Function to select a vehicle as favourite with a button
 */
@Composable
private fun VehicleListBody(
    uiState: UserVehicleListUiState,
    onVehicleDelete: (Long) -> Unit,
    onVehicleFav: (UserVehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    // The vehicles are loaded if the loading process completed
    val vehicleList = if(uiState is UserVehicleListUiState.Success) {
        uiState.vehicleList
    } else {
        emptyList()
    }
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

            // A message shows if an error happened
            if(uiState is UserVehicleListUiState.Error) {
                Text(
                    text = stringResource(id = if(uiState.code == 2) {
                        R.string.error_signup
                    } else R.string.server_available),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.padding(22.dp)
                )
            }

            // Indicator while vehicles are loading
            else if(uiState is UserVehicleListUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                )
            }

            // Message when there are no vehicles in the list
            else if (vehicleList.isEmpty()) {
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
                    onVehicleFav = onVehicleFav,
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * List with all the user vehicles represented
 *
 * @param vehicleList List of the user vehicles with details about the model and if it should be
 * deleted
 * @param onVehicleDelete Function to delete one of the vehicles of the list
 * @param onVehicleFav Function to set one of the vehicles of the list as favourite
 */
@Composable
private fun VehicleList(
    vehicleList: List<Triple<UserVehicle,Vehicle, Boolean>>,
    onVehicleDelete: (Long) -> Unit,
    onVehicleFav: (UserVehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState, modifier = modifier) {
        // For each user vehicle, an element of the list is displayed
        items(items = vehicleList, key = { it.first.id!! }) { vehicle ->
            VehicleItem(
                vehicle = vehicle.second,
                userVehicle = vehicle.first,
                canDelete = vehicle.third,
                onVehicleDelete = onVehicleDelete,
                onVehicleFav = onVehicleFav,
            )
        }
    }
}

/**
 * Composable that represents a user vehicle of the list
 *
 * @param vehicle Model and details of the vehicle
 * @param userVehicle Details about the vehicle owned by the user
 * @param canDelete Flag that indicates if the vehicle should be capable of being removed from the list
 * @param onVehicleDelete Function to delete the vehicle if the button is pressed
 * @param onVehicleFav Function to set the vehicle as favourite if the button is pressed
 */
@Composable
private fun VehicleItem(
    vehicle: Vehicle,
    userVehicle: UserVehicle,
    canDelete: Boolean,
    onVehicleDelete: (Long) -> Unit,
    onVehicleFav: (UserVehicle) -> Unit,
) {
    // The brand and name are obtained from splitting the name
    var brand: String
    var name: String
    vehicle.name.split("_-_").apply {
        brand = this.first()
        name = this.last()
        // If they did not split, only one of them is shown
        if (brand == name) name = ""
    }
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
                // The image of the vehicle is loaded from the URL
                AsyncImage(
                    model = ImageRequest.Builder(context = LocalContext.current)
                        .data(vehicle.imageURL)
                        .crossfade(true)
                        .build(),
                    // If if could not be loaded, a default image shows
                    error = painterResource(R.drawable.car_model),
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
                    Text(text = brand, style = MaterialTheme.typography.titleMedium)
                    // Only the brand is shown if the name did not split
                    if(name.isNotBlank()) {
                        Text(text = name, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                // The age is shown only if it was introduced
                if(userVehicle.age != null) {
                    Text(
                        text = userVehicle.age.toString() + " " + stringResource(id = R.string.years),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(text = stringResource(id = vehicle.getMotorType()),
                    style = MaterialTheme.typography.titleMedium)
            }
            
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                // If it can be deleted, the delete button is shown
                if(canDelete) {
                    IconButton(onClick = { onVehicleDelete(userVehicle.id!!) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(id = R.string.delete_vehicle)
                        )
                    }
                }

                // The button changed depending if it is already the favourite or not
                IconButton(onClick = { onVehicleFav(userVehicle) }) {
                    Icon(
                        imageVector = if(userVehicle.isFav == 0) {
                            Icons.Filled.FavoriteBorder
                        } else {
                            Icons.Filled.Favorite
                        },
                        contentDescription = if(userVehicle.isFav == 0) {
                            stringResource(id = R.string.mark_vehicle)
                        } else stringResource(id = R.string.unmark_vehicle)
                    )
                }
            }
        }
    }
}

/**
 * Composable to enable reloading the list when the view is focused again after a change of screen
 *
 * @param viewModel Object to retrieve the vehicles and reload the list
 * @return A [LifecycleEventObserver] that reloads the list on resume
 */
@Composable
fun rememberVehiclesLifecycleObserver(viewModel: UserVehicleListViewModel): LifecycleEventObserver =
    remember(viewModel) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.getVehicles()
                else -> {}
            }
        }
    }

@Preview(showBackground = true, heightDp = 650)
@Preview(showBackground = true, heightDp = 650, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun VehicleEmptyListScreenPreview() {
    GRETAAppTheme {
        VehicleListBody(
            uiState = UserVehicleListUiState.Loading,
            onVehicleDelete = {},
            onVehicleFav = {}
        )
    }
}


