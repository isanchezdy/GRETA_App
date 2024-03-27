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
import kotlin.math.ceil

object StatsDestination : NavigationDestination {
    override val route = "stats"
    override val titleRes = R.string.stats
    override val icon: ImageVector = Icons.Filled.QueryStats
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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

@Composable
fun StatsBody(uiState: StatsUiState, modifier: Modifier = Modifier) {
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
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                )
            } else if (userStats == null) {
                Text(
                    text = stringResource(id = R.string.empty_stats),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = modifier.padding(22.dp)
                )
            } else {
                Text(stringResource(id = R.string.consumption_saving) + ": " +
                        String.format("%.3f",userStats.consumptionSaving) + "%", modifier = Modifier.padding(16.dp))

                Text(stringResource(id = R.string.drive_rating) + ": " +
                        String.format("%.1f",userStats.driveRating) + " / 5", modifier = Modifier.padding(16.dp))

                Text(stringResource(id = R.string.eco_distance) + ": " +
                        String.format("%.3f",userStats.ecoDistance / 1000.0) + " km", modifier = Modifier.padding(16.dp))

                Text(stringResource(id = R.string.eco_routes_num) + ": " +
                        userStats.ecoRoutesNum, modifier = Modifier.padding(16.dp))

                val time = if(userStats.ecoTime >= 3600.0) {
                    ceil(userStats.ecoTime/3600).toInt().toString() + " h"
                } else {
                    ceil(userStats.ecoTime/60).toInt().toString() + " min"
                }
                Text(stringResource(id = R.string.eco_time) + ": " +
                        time, modifier = Modifier.padding(16.dp))
            }
        }
    }
}