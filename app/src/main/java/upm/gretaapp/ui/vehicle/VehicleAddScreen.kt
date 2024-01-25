package upm.gretaapp.ui.vehicle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.ui.navigation.NavigationDestination

object VehicleAddDestination : NavigationDestination {
    override val route = "add_vehicle"
    override val titleRes = R.string.add_vehicle
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleAddScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    //viewModel: ItemEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = false, navigateUp = onNavigateUp)
        }
    ) { paddingValues ->
        VehicleEntryBody(modifier.padding(paddingValues))
    }
}

@Composable
fun VehicleEntryBody(
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.add_vehicle),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(16.dp)
        )

        VehicleInputForm()

        Button(
            onClick = {},
            modifier = Modifier
        ) {
            Text(stringResource(id = R.string.save))
        }
    }
}

@Composable
fun VehicleInputForm(

) {
    Column {

    }
}