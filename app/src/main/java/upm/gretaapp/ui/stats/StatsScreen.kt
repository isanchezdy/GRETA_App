package upm.gretaapp.ui.stats

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryStats
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination

/**
 * Object that represents the route of the Stats screen
 */
object StatsDestination : NavigationDestination {
    override val route = "stats"
    override val titleRes = R.string.stats
    override val icon: ImageVector = Icons.Filled.QueryStats
}

/**
 * Composable that represents the Stats screen
 *
 * @param openMenu Function to open the menu of the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    openMenu: () -> Unit,
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = true, openMenu = openMenu, navigateUp = { })
        }
    ){
        StatsBody(viewModel.statsUiState, Modifier.padding(it))
    }
}

/**
 * Body of the Stats Screen
 *
 * @param uiState Object that represents the state of the screen (loading, error, etc)
 */
@SuppressLint("DefaultLocale")
@Composable
fun StatsBody(uiState: StatsUiState, modifier: Modifier = Modifier) {
    // The stats are retrieved for showing
    val userStats = if(uiState is StatsUiState.Success) {
        uiState.userStats
    } else {
        null
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.stats),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.padding(8.dp)
        ) {
            // Error message when the retrieving method fails
            if (uiState is StatsUiState.Error) {
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
            } else if (uiState is StatsUiState.Loading) {
                // Indicator while loading
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                )
            } else if (userStats == null) {
                // If there are no stats for the current user
                Text(
                    text = stringResource(id = R.string.empty_stats),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.padding(22.dp)
                )
            } else {
                // A field for every parameter is shown
                Text(stringResource(id = R.string.consumption_saving) + ": " +
                        String.format("%.1f",userStats.consumptionSaving) + "%", modifier = Modifier.padding(16.dp))

                Text(stringResource(id = R.string.drive_rating) + ": " +
                        String.format("%.1f",userStats.driveRating) + " / 5", modifier = Modifier.padding(16.dp))

                Text(stringResource(id = R.string.eco_distance) + ": " +
                        String.format("%.1f",userStats.ecoDistance) + " %", modifier = Modifier.padding(16.dp))

                Text(stringResource(id = R.string.eco_routes_num) + ": " +
                        userStats.ecoRoutesNum + " %", modifier = Modifier.padding(16.dp))


                Text(stringResource(id = R.string.eco_time) + ": " +
                        String.format("%.1f", userStats.ecoTime) + " %", modifier = Modifier.padding(16.dp))
            }
        }
    }
}