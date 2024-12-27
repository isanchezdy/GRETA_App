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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import upm.gretaapp.ui.map.Score
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

/**
 * Object that represents the route of the Route History screen
 */
object RouteHistoryDestination : NavigationDestination {
    override val route = "routes_history"
    override val titleRes = R.string.route_history
    override val icon: ImageVector = Icons.Filled.Route
}

/**
 * Composable that represents the Route History screen
 *
 * @param openMenu Function to open the menu of the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteHistoryScreen(
    openMenu: () -> Unit,
    viewModel: RouteHistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // The state of the ui is retrieved to represent it
    val routesHistoryUiState = viewModel.routesHistoryUiState
    // A flag used to reverse the order of the list
    var reversedList by rememberSaveable{ mutableStateOf(false) }

    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = true, openMenu = openMenu, navigateUp = { })
        },
        // Floating button to reverse the order of the list of the routes
        floatingActionButton = {
            // If the routes have loaded successfully and the list is not empty
            if(routesHistoryUiState is RoutesHistoryUiState.Success
                && routesHistoryUiState.routeHistory.isNotEmpty()) {
                // The button changes the flag
                FloatingActionButton(
                    onClick = { reversedList = !reversedList },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = stringResource(R.string.reverse_list)
                    )
                }
            }
        }
    ) {
        RoutesHistoryBody(
            uiState = routesHistoryUiState,
            reversedList = reversedList,
            modifier = Modifier.padding(it)
        )
    }
}

/**
 * Body of the route history screen
 *
 * @param uiState Object that represents the current state of the screen (loading, showing routes, error)
 * @param reversedList Flag that indicates if the list of routes should show reversed
 */
@Composable
private fun RoutesHistoryBody(
    uiState: RoutesHistoryUiState,
    reversedList: Boolean,
    modifier: Modifier = Modifier
) {
    // The routes are loaded from the uiState
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
            modifier = modifier.padding(8.dp).fillMaxWidth()
        ) {
            // A message is shown if an error happens while loading the routes
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
                // A circular indicator is shown while loading the routes
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                )
            } else if (routesHistoryList.isEmpty()) {
                // A message is shown if there are no registered routes
                Text(
                    text = stringResource(id = R.string.empty_route_history),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.padding(22.dp)
                )
            } else {
                RoutesHistoryList(
                    routesHistoryList = routesHistoryList.reversed(),
                    reversedList = reversedList,
                    modifier = modifier
                )
            }
        }
    }
}

/**
 * List of [UserRoute] items represented on the screen
 *
 * @param routesHistoryList List of routes to represent along the vehicle used during the recording
 * @param reversedList Flag that indicates if the list of routes should show reversed
 */
@Composable
private fun RoutesHistoryList(
    routesHistoryList: List<Pair<UserRoute, Vehicle>>,
    reversedList: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    // For each user route, an element of the column is represented
    LazyColumn(state = listState, reverseLayout = reversedList, modifier = modifier.fillMaxWidth()) {
        items(items = routesHistoryList, key = { it.first.id!! }) { userRoute ->
            UserRouteItem(
                userRoute = userRoute.first,
                vehicle = userRoute.second
            )
        }
    }
}

/**
 * Composable that represents the information of a [UserRoute] and the [Vehicle] used in the record
 *
 * @param userRoute The object from which the information of the route is obtained to represent it
 * @param vehicle [Vehicle] used during the recording of the route
 */
@Composable
private fun UserRouteItem(
    userRoute: UserRoute,
    vehicle: Vehicle
) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // The date of the route is represented
            Text(
                text = userRoute.recordDate.replace("T", " "),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )

            // The name of the vehicle is represented just below
            Text(
                text = vehicle.name.replace("_-_", " "),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )

            // A row with the general information of the route is placed
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Text(text = "${userRoute.performedRouteDistance / 1000} km")

                Text(text = "${userRoute.performedRouteTime/ 60} min")

                Text(text = "${userRoute.performedRouteConsumption} " +
                        if(vehicle.motorType == "ELECTRIC") {
                            "kW/h"
                        } else "l"
                )
            }

            // Finally, the scores obtained during the route are shown
            Text(
                text = stringResource(id = R.string.slow_driving),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top=8.dp)
            )

            Score(score = userRoute.drivingAggressiveness)

            Text(
                text = stringResource(id = R.string.speeding),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top=8.dp)
            )

            Score(score = userRoute.speedVariationNum)
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
            uiState = RoutesHistoryUiState.Loading,
            false
        )
    }
}