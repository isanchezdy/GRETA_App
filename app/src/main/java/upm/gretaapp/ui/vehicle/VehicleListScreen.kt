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
import org.osmdroid.views.MapView
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import upm.gretaapp.model.getMotorType
import upm.gretaapp.ui.AppViewModelProvider
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
    openMenu: () -> Unit,
    viewModel: VehicleListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val vehicleListUiState = viewModel.vehicleListUiState
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
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val lifecycleObserver = rememberVehiclesLifecycleObserver(viewModel = viewModel)
        DisposableEffect(lifecycle) {
            lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycle.removeObserver(lifecycleObserver)
            }
        }

        VehicleListBody(
            uiState = vehicleListUiState,
            onVehicleDelete = viewModel::deleteVehicle,
            onVehicleFav = viewModel::setFavourite,
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
private fun VehicleListBody(
    uiState: VehicleListUiState,
    onVehicleDelete: (Long) -> Unit,
    onVehicleFav: (UserVehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicleList = if(uiState is VehicleListUiState.Success) {
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

            if(uiState is VehicleListUiState.Error) {
                Text(
                    text = stringResource(id = R.string.error_signup),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.padding(22.dp)
                )
            }

            else if(uiState is VehicleListUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                )
            }

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

@Composable
private fun VehicleList(
    vehicleList: List<Pair<UserVehicle,Vehicle>>,
    onVehicleDelete: (Long) -> Unit,
    onVehicleFav: (UserVehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState, modifier = modifier) {
        items(items = vehicleList, key = { it.first.id!! }) { vehicle ->
            VehicleItem(
                vehicle = vehicle.second,
                userVehicle = vehicle.first,
                onVehicleDelete = onVehicleDelete,
                onVehicleFav = onVehicleFav,
            )
        }
    }
}

@Composable
private fun VehicleItem(
    vehicle: Vehicle,
    userVehicle: UserVehicle,
    onVehicleDelete: (Long) -> Unit,
    onVehicleFav: (UserVehicle) -> Unit,
) {
    var brand: String
    var name: String
    vehicle.name.split("_-_").apply {
        brand = this.first()
        name = this.last()
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
                AsyncImage(
                    model = ImageRequest.Builder(context = LocalContext.current)
                        .data(vehicle.imageURL)
                        .crossfade(true)
                        .build(),
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
                    Text(text = name,  style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                IconButton(onClick = { onVehicleDelete(userVehicle.id!!) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.delete_vehicle)
                    )
                }

                IconButton(onClick = { onVehicleFav(userVehicle) }) {
                    Icon(
                        imageVector = if(userVehicle.isFav == 0) {
                            Icons.Filled.FavoriteBorder
                        } else {
                            Icons.Filled.Favorite
                        },
                        contentDescription = stringResource(id = R.string.mark_vehicle)
                    )
                }
            }
        }
    }
}

@Composable
fun rememberVehiclesLifecycleObserver(viewModel: VehicleListViewModel): LifecycleEventObserver =
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
            uiState = VehicleListUiState.Loading,
            onVehicleDelete = {},
            onVehicleFav = {}
        )
    }
}


