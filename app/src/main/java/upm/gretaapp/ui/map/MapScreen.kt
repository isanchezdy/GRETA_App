package upm.gretaapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import upm.gretaapp.GretaTopAppBar
import upm.gretaapp.R
import upm.gretaapp.model.NominatimResult
import upm.gretaapp.model.RouteEvaluation
import upm.gretaapp.model.processedRoute
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination
import upm.gretaapp.ui.theme.GRETAAppTheme
import kotlin.math.ceil

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
        val uiState by viewModel.uiState.collectAsState()
        MapBody(
            uiState = uiState,
            recordingUiStateFlow = viewModel.recordingUiState,
            options = options,
            search = {
                viewModel.getDestination(it)
            },
            clearOptions = {
                viewModel.clearOptions()
            },
            searchRoutes = { source, destination ->
                viewModel.getRoutes(source = source, destination = destination)
            },
            startRecording = { point ->
                viewModel.startRecording(point)
            },
            cancelRecording = viewModel::cancelRecording,
            getScore = viewModel::getScore,
            clearScore = viewModel::clearResults,
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
    uiState: MapUiState,
    recordingUiStateFlow: StateFlow<RecordingUiState>,
    options: List<NominatimResult>,
    search: (String) -> Unit,
    clearOptions: () -> Unit,
    searchRoutes: (GeoPoint, GeoPoint) -> Unit,
    startRecording: (GeoPoint) -> Unit,
    cancelRecording: () -> Unit,
    getScore: (List<Double>, List<Double>) -> Unit,
    clearScore: () -> Unit,
    modifier: Modifier = Modifier
) {
    // View which contains the map and its functions
    val mapView = rememberMapViewWithLifecycle()
    // Variable that represents the center of the map
    var startPoint by remember{ mutableStateOf(GeoPoint(40.447234,-3.7348339)) }
    // Variable for managing the center of the map for each recomposition
    var shouldCenter by remember{ mutableStateOf(true) }

    val recordingUiState by recordingUiStateFlow.collectAsState()

    // Location permission in case it is required to show a message
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        null
    }

    val context = LocalContext.current
    val layoutInflater = LayoutInflater.from(context)

    val locationOverlay = rememberLocationOverlayWithLifecycle(mapView)

    // Asks permission to use location from the phone
    LaunchedEffect(locationPermissionState.status.isGranted,
        backgroundLocationState?.status?.isGranted
    ) {
        // Asks only if they are not granted
        if(!locationPermissionState.status.isGranted ||
            (backgroundLocationState != null && !backgroundLocationState.status.isGranted)) {
            // If the state of the permissions is changed, the position is removed from the map
            locationOverlay.disableMyLocation()
            mapView.overlayManager.remove(locationOverlay)
            mapView.invalidate()

            // If the app considers it, a dialog with a warning for the user is shown
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
            // The permissions are requested otherwise
            else {
                locationPermissionState.launchPermissionRequest()
                backgroundLocationState?.launchPermissionRequest()
            }
        // If the location is granted, the screen is centered towards the current position
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener {
                    if ((it != null)) {
                        startPoint = GeoPoint(it)
                    }
                }

            // A marker for the current location is enabled
            locationOverlay.enableMyLocation()
            mapView.overlayManager.add(locationOverlay)
            mapView.invalidate()
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
        val destinationPoint by remember {
            mutableStateOf(Marker(mapView).apply {
                this.infoWindow = MyInfoWindow(
                    view = MarkerWindowFragment(onClick = {
                        searchRoutes(locationOverlay.myLocation, this.position)
                    })
                        .onCreateView(layoutInflater, null, null),
                    mapView = mapView
                )
            })
        }

        // Events receiver to add markers on click
        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                if((uiState is MapUiState.Start || uiState is MapUiState.Error)
                    && destination.isBlank()) {
                    shouldCenter = false
                    mapView.overlayManager.remove(destinationPoint)
                    destinationPoint.position = p
                    mapView.overlayManager.add(destinationPoint)
                    mapView.controller.animateTo(destinationPoint.position)
                    destinationPoint.showInfoWindow()
                    destination = String.format("%.8f", p.latitude) + ";" + String.format("%.8f", p.longitude)
                    mapView.invalidate()
                }
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }

        val overlayEvents = MapEventsOverlay(mReceive)
        mapView.overlays.add(overlayEvents)

        var searchExpanded by rememberSaveable { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current

        val routeOverlays: MutableList<Polyline> = remember { mutableListOf() }

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
                                    for(route in routeOverlays) {
                                        route.closeInfoWindow()
                                    }
                                    mapView.invalidate()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = stringResource(id = R.string.cancel_search)
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

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp)
        ) {
            IconButton(
                onClick = {}
            ) {
                Icon(imageVector = Icons.Filled.DirectionsCar, contentDescription = null)
            }
        }

        LaunchedEffect(uiState) {
            if (uiState is MapUiState.CompleteRoutes) {
                var bounds = destinationPoint.bounds
                bounds = bounds.concat(BoundingBox.fromGeoPoints(
                    listOf(locationOverlay.myLocation))
                )
                for(route in uiState.routes) {
                    val polyline = Polyline(mapView)

                    polyline.outlinePaint.strokeCap = Paint.Cap.ROUND
                    polyline.outlinePaint.strokeWidth = 20F

                    if(route.first.size > 1) {
                        polyline.outlinePaint.color = Color.MAGENTA
                    }
                    else {
                        polyline.outlinePaint.color = when(route.first.first()) {
                            "ECO" -> Color.GREEN
                            "FASTEST" -> Color.BLUE
                            "SHORTEST" -> Color.YELLOW
                            else -> Color.RED
                        }
                    }

                    val coordinates = route.second.processedRoute
                    for(cord in coordinates) {
                        polyline.addPoint(GeoPoint(cord.first, cord.second))
                    }

                    polyline.infoWindow = MyInfoWindow(
                        view = RouteWindowFragment(
                            _uiState = recordingUiStateFlow,
                            route = route.second,
                            onClick = {
                                startRecording(destinationPoint.position)
                                openMaps(context, polyline.actualPoints.map { it.toPair() })
                            },
                            onCancel = cancelRecording
                        ).onCreateView(layoutInflater,null,null),
                        mapView = mapView
                    )

                    bounds = bounds.concat(polyline.bounds)

                    mapView.overlayManager.add(polyline)
                    routeOverlays.add(polyline)
                }
                mapView.invalidate()
                mapView.zoomToBoundingBox(bounds.increaseByScale(1.3f),
                    true)
            }
            else if (uiState is MapUiState.Start) {
                if(routeOverlays.isNotEmpty()) {
                    mapView.overlayManager.removeAll(routeOverlays)
                    mapView.invalidate()

                    routeOverlays.clear()
                }
            }
        }

        when (uiState) {
            is MapUiState.LoadingRoute -> {
                LoadingRouteDialog()
            }

            is MapUiState.Error -> {
                ErrorMessage()
            }

            is MapUiState.CompleteScore -> {
                ScoresResult(score = uiState.scores, clearScore = clearScore)
            }

            else -> {}
        }

    }

    LaunchedEffect(recordingUiState) {
        if(recordingUiState is RecordingUiState.Complete) {
            getScore((recordingUiState as RecordingUiState.Complete).speeds,
                (recordingUiState as RecordingUiState.Complete).heights)
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

    val calculateNewPhrase = { var newPhrase = ecoDrivingPhrases.random()
        while (newPhrase == currentPhrase) {
            newPhrase = ecoDrivingPhrases.random()
        }
        currentPhrase = newPhrase
    }

    LaunchedEffect(currentPhrase) {
        delay(10000)
        calculateNewPhrase()
    }

    Dialog(onDismissRequest = { }) {
        ElevatedCard(modifier = Modifier
            .fillMaxWidth(0.8f)
            .clickable {
                calculateNewPhrase()
            }) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = currentPhrase,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ErrorMessage() {
    var visible by remember{ mutableStateOf(true) }
    var timeLeft by remember{ mutableIntStateOf(5) }
    LaunchedEffect(visible) {
        while(timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        visible = false
    }

    if(visible) {
        Dialog(onDismissRequest = { visible = false }) {
            ElevatedCard(modifier = Modifier
                .fillMaxWidth(0.8f)
            ) {
                Text(
                    text = stringResource(id = R.string.error_signup),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ScoresResult(
    score: RouteEvaluation,
    clearScore: () -> Unit
) {
    var visible by remember{ mutableStateOf(true) }
    val close = {
        visible = false
        clearScore()
    }

    if(visible) {
        Dialog(onDismissRequest = close) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier =
                    Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.results),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                    
                    Text(
                        text = stringResource(id = R.string.stops),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Score(score = score.numStopsPerKm)

                    Text(
                        text = stringResource(id = R.string.speeding),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Score(score = score.accelerationGreaterThreshold)

                    Text(
                        text = stringResource(id = R.string.slow_driving),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )

                    Score(score = score.accelerationLowerThreshold)

                    Text(
                        text = stringResource(id = R.string.distance) + ": " +
                                String.format("%.3f", (score.distance/1000.0) )
                                + " km",
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.time) + ": " +
                                ceil(score.time/60).toInt().toString()
                                + " min",
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.consumption) + ": "
                                + String.format("%.3f", score.energyConsumption)
                                + " l",
                        modifier = Modifier.padding(16.dp)
                    )

                    Button(onClick = close, modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(id = R.string.accept))
                    }
                }
            }
        }
    }
}

@Composable
fun Score(score: Int) {
    Row {
        for (i in (1..score)) {
            Icon(
                imageVector = Icons.Filled.StarRate,
                contentDescription = null,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 480, widthDp = 320)
@Preview(showBackground = true, heightDp = 480, widthDp = 320, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoadingRouteDialogPreview(){
    GRETAAppTheme {
        LoadingRouteDialog(listOf("Keep a constant speed", "Turn off your engine for long pauses",
            "Stay alert for changes while driving", "Remember to monitor tire pressure"))
    }
}

@Preview(showBackground = true, heightDp = 480, widthDp = 320)
@Preview(showBackground = true, heightDp = 480, widthDp = 320, uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "es")
@Composable
fun ErrorMessagePreview() {
    GRETAAppTheme {
        ErrorMessage()
    }
}