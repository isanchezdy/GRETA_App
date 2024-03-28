package upm.gretaapp.ui.route_history

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.model.UserRoute
import upm.gretaapp.model.Vehicle
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

object RouteHistoryDestination : NavigationDestination {
    override val route = "routes_history"
    override val titleRes = R.string.route_history
    override val icon: ImageVector = Icons.Filled.Route
}

// TODO add userVehicle to get the vehicle name

/**
 * Composable that represents the Route History screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteHistoryScreen(
    openMenu: () -> Unit,
    viewModel: RouteHistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val routesHistoryUiState = viewModel.routesHistoryUiState
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = true, openMenu = openMenu, navigateUp = { })
        }
    ) {
        RoutesHistoryBody(
            uiState = routesHistoryUiState,
            modifier = Modifier.padding(it)
        )
    }
}

@Composable
private fun RoutesHistoryBody(
    uiState: RoutesHistoryUiState,
    modifier: Modifier = Modifier
) {
    val routesHistoryList = if (uiState is RoutesHistoryUiState.Success) {
        uiState.routeHistory
    } else {
        emptyList()
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.route_history),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (routesHistoryList.isEmpty()) {
                Arrangement.Center
            } else {
                Arrangement.Top
            },
            modifier = modifier.padding(8.dp)
        ) {

            if (uiState is RoutesHistoryUiState.Error) {
                Text(
                    text = stringResource(
                        id = if (uiState.code == 2) {
                            R.string.error_signup
                        } else R.string.server_available
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.padding(22.dp)
                )
            } else if (uiState is RoutesHistoryUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                )
            } else if (routesHistoryList.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.empty_route_history),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.padding(22.dp)
                )
            } else {
                RoutesHistoryList(
                    routesHistoryList = routesHistoryList,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun RoutesHistoryList(
    routesHistoryList: List<Pair<UserRoute, Vehicle>>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LazyColumn(state = listState, modifier = modifier) {
        items(items = routesHistoryList, key = { it.first.id }) { userRoute ->
            UserRouteItem(
                userRoute = userRoute.first,
                vehicle = userRoute.second
            )
        }
    }
}

@Composable
private fun UserRouteItem( // TODO finish this!
    userRoute: UserRoute,
    vehicle: Vehicle
) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = userRoute.recordDate.replace("T", " "),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Put name instead of ID
                Text(
                    text = userRoute.userVehicleId.toString(),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = userRoute.additionalMass.toString(),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = userRoute.sourceCoords,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = userRoute.destinationCoords,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = userRoute.selectedRouteType,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = userRoute.selectedRouteConsumption.toString(),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = userRoute.selectedRouteTime.toString(),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = userRoute.selectedRouteDistance.toString(),
                    style = MaterialTheme.typography.titleMedium
                )

                // TODO continue here with UI improvement and adding other fields
            }
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
fun UserRouteEmptyListScreenPreview() {
    GRETAAppTheme {
        RoutesHistoryBody(
            uiState = RoutesHistoryUiState.Loading
        )
    }
}