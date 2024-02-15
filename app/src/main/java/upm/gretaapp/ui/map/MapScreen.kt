package upm.gretaapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Build
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.model.NominatimResult
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme

object MapDestination : NavigationDestination {
    override val route = "map"
    override val titleRes = R.string.map
    override val icon: ImageVector = Icons.Filled.Map
}

/**
 * Composable that represents the map screen
 *
 * @param openMenu Function to open the app menu when the button is clicked
 * @param viewModel ViewModel to hold the current state of the UI (start, loading, error, etc...)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    openMenu: () -> Unit,
    viewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Scaffold(
        topBar = {
            GretaTopAppBar(canUseMenu = true, openMenu = openMenu, navigateUp = { })
        }
    ) { it ->
        val options by viewModel.searchResults.collectAsState()
        MapBody(
            options = options,
            search = {
                viewModel.getDestination(it)
            },
            clearOptions = {
                viewModel.clearOptions()
            },
            startRecording = { point ->
                viewModel.startRecording(point)
            },
            modifier = Modifier.padding(it)
        )
    }
}

/**
 * Body of the map screen with all of its functions
 *
 * @param options List of results to choose one with the search bar
 * @param search Function to update results based on a location query
 * @param clearOptions Function to remove results when one is selected
 * @param startRecording Function to start recording a route
 */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapBody(
    options: List<NominatimResult>,
    search: (String) -> Unit,
    clearOptions: () -> Unit,
    startRecording: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    // View which contains the map and its functions
    val mapView = rememberMapViewWithLifecycle()
    // Variable that represents the center of the map
    var startPoint by remember{ mutableStateOf(GeoPoint(40.447234,-3.7348339)) }
    // Variable for managing the center of the map for each recomposition
    var shouldCenter by remember{ mutableStateOf(true) }
    // Location permission in case it is required to show a message
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        null
    }
    val context = LocalContext.current
    val layoutInflater = LayoutInflater.from(context)

    // Asks permission to use location from the phone
    LaunchedEffect(locationPermissionState, backgroundLocationState) {
        if(!locationPermissionState.status.isGranted ||
            (backgroundLocationState != null && !backgroundLocationState.status.isGranted)) {
            if(locationPermissionState.status.shouldShowRationale) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(R.string.enable_location)
                builder.setMessage(R.string.enable_location_text)
                builder.setPositiveButton("OK") { _, _ ->
                    locationPermissionState.launchPermissionRequest()
                    backgroundLocationState?.launchPermissionRequest()
                }
                builder.show()
            }
            else {
                locationPermissionState.launchPermissionRequest()
                backgroundLocationState?.launchPermissionRequest()
            }
        }
    }

    // If the location is granted, the screen is centered towards the current position
    if(locationPermissionState.status.isGranted) {
        // Get location manager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        var shouldUpdate by remember { mutableStateOf(true) }
        fusedLocationClient.lastLocation
            .addOnSuccessListener {
                if((it != null) and shouldUpdate) {
                    startPoint = GeoPoint(it)
                    shouldUpdate = false
                }
            }
    }

    if(shouldCenter) mapView.controller.setCenter(startPoint)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = {
                mapView
            },
            modifier = Modifier.fillMaxSize()
        )

        var destination by rememberSaveable{ mutableStateOf("") }
        val destinationPoint by remember{
            mutableStateOf(Marker(mapView).apply {
                this.infoWindow = MyInfoWindow(
                    view = MarkerWindowFragment(onClick = { /*startRecording(this.position)
                        openMaps(context, listOf(this.position.toPair()))*/
                     })
                        .onCreateView(layoutInflater,null,null),
                    mapView = mapView
                )
            })
        }

        var searchExpanded by rememberSaveable { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            TextField(
                value = destination,
                onValueChange = {
                    shouldCenter = false
                    destination = it
                    clearOptions()
                },
                singleLine = true,
                label = { Text(stringResource(id = R.string.destination)) },
                trailingIcon = {
                    if (destination.isNotBlank()) {
                        IconButton(
                            onClick = {
                                shouldCenter = false
                                destination = ""
                                clearOptions()
                                searchExpanded = false
                                if(mapView.overlayManager.remove(destinationPoint)) {
                                    destinationPoint.closeInfoWindow()
                                    mapView.invalidate()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = stringResource(id = R.string.cancel)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = if (destination.isNotBlank()) {
                        ImeAction.Search
                    } else ImeAction.None
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        search(destination)
                        searchExpanded = true
                    }
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            AnimatedVisibility(
                visible = searchExpanded
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .requiredHeightIn(
                            min = 0.dp,
                            max = 200.dp
                        )
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    items(items = options, key = { it.placeId }) {
                        DropdownMenuItem(
                            text = { Text(it.displayName) },
                            onClick = {
                                destination = it.displayName
                                destinationPoint.position = GeoPoint(
                                    it.lat,
                                    it.lon
                                )
                                destinationPoint.closeInfoWindow()
                                shouldCenter = true
                                startPoint = destinationPoint.position
                                mapView.overlayManager.remove(destinationPoint)
                                mapView.overlayManager.add(destinationPoint)

                                searchExpanded = false
                                focusManager.clearFocus()
                                mapView.invalidate()
                            },
                            contentPadding = PaddingValues(10.dp),
                            modifier = Modifier.requiredHeight(40.dp)
                        )

                        if (options.last() != it) {
                            HorizontalDivider(thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog that shows Eco-Driving phrases to the user while a query is being processed
 *
 * @param ecoDrivingPhrases Phrases shown while waiting, tapping on the screen changes the current
 * one
 */
@Composable
fun LoadingRouteDialog(ecoDrivingPhrases: List<String> =
                           stringArrayResource(id = R.array.eco_driving_messages).toList()){
    var currentPhrase by remember { mutableStateOf(ecoDrivingPhrases.random()) }
    Dialog(onDismissRequest = { }) {
        ElevatedCard(modifier = Modifier
            .fillMaxWidth(0.8f)
            .clickable {
                var newPhrase = ecoDrivingPhrases.random()
                while (newPhrase == currentPhrase) {
                    newPhrase = ecoDrivingPhrases.random()
                }
                currentPhrase = newPhrase
            }) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = currentPhrase,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 200, widthDp = 200)
@Preview(showBackground = true, heightDp = 200, widthDp = 200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingRouteDialogPreview(){
    GRETAAppTheme {
        LoadingRouteDialog(listOf("Keep a constant speed", "Turn off your engine for long pauses",
            "Stay alert for changes while driving", "Remember to monitor tire pressure"))
    }
}