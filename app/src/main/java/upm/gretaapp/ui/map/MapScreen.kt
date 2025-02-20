package upm.gretaapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import upm.gretaapp.model.Route
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import upm.gretaapp.model.processedRoute
import upm.gretaapp.ui.AppViewModelProvider
import upm.gretaapp.ui.navigation.NavigationDestination

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
    // All the vehicles from the user are retrieved
    val vehicles by viewModel.vehicleList.collectAsState()
    // The favourite one is set as default
    var favouriteVehicle: Pair<UserVehicle, Vehicle>?
    // Stores the current vehicle used for the route, or null if there are no eligible vehicles
    val selectedVehicle: MutableState<Pair<Long, Long>?> = rememberSaveable {
        mutableStateOf(null)
    }
    // Checks if the current vehicle is electric to show correct units for the results
    val isElectric = rememberSaveable {
        mutableStateOf(false)
    }

    // The favourite is selected when the list is retrieved
    LaunchedEffect(vehicles) {
        // If this event is thrown again, the vehicle is not changed
        if(selectedVehicle.value != null) {
            return@LaunchedEffect
        }
        favouriteVehicle = vehicles.find {
            it.first.isFav == 1
        }
        selectedVehicle.value = if(favouriteVehicle != null) {
            isElectric.value = favouriteVehicle!!.second.motorType == "ELECTRIC"
            Pair(favouriteVehicle!!.first.id!!, favouriteVehicle!!.second.vehicleID)
        } else {
            null
        }
    }

    // Values of the route
    val numberOfPersons = rememberSaveable{ mutableIntStateOf(1) }
    val numberOfBulks: MutableState<Int?> = rememberSaveable{ mutableStateOf(null) }
    val visible = rememberSaveable{ mutableStateOf(true) }

    // Current state of the ui
    val uiState by viewModel.uiState.collectAsState()
    // Context to access the recordings of the app
    val context = LocalContext.current

    // The screen for selecting parameters is shown only when the button is pressed
    RouteParams(
        visible = visible,
        vehicles = vehicles,
        selectedVehicle = selectedVehicle,
        numberOfPersons = numberOfPersons,
        numberOfBulks = numberOfBulks,
        isElectric = isElectric,
        sendFiles = { viewModel.sendFiles(context) }
    )

    // Function for centering the map when location is available
    val center: MutableState<(() -> Unit)?> = remember{ mutableStateOf(null) }
    // Current state of the recording process
    val recordingUiState by viewModel.recordingUiState.collectAsState()

    Scaffold(
        topBar = {
            // The menu can only be used when a route is not being recorded
            GretaTopAppBar(
                canUseMenu = recordingUiState !is RecordingUiState.Loading,
                openMenu = openMenu,
                navigateUp = { viewModel.cancelRecording() }
            )
        },
        floatingActionButton = {
            Column {
                // Current state of the pause button
                var isPaused by rememberSaveable{ mutableStateOf(false) }
                // Buttons shown only when a route is being recorded
                if (recordingUiState is RecordingUiState.Loading) {
                    // Button to finish the route
                    FloatingActionButton(
                        onClick = {
                            viewModel.updateStateRoute(context, "finished")
                            isPaused = false
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = stringResource(id = R.string.finish_route)
                        )
                    }
                    // Button to stop or continue the route
                    FloatingActionButton(
                        onClick = {
                            isPaused = if(isPaused) {
                                viewModel.updateStateRoute(context, "started")
                                false
                            } else {
                                viewModel.updateStateRoute(context, "paused")
                                true
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = if(isPaused) {
                                Icons.Filled.PlayArrow
                            } else {
                                Icons.Filled.Pause
                            },
                            contentDescription = if(isPaused) {
                                stringResource(id = R.string.continue_route)
                            } else {
                                stringResource(id = R.string.pause_route)
                            }
                        )
                    }
                }
                // These buttons are shown only before starting a route
                else if (uiState is MapUiState.Start || uiState is MapUiState.Error) {
                    isPaused = false
                    // The button for centering the map is available only after accepting permissions
                    if( center.value != null) {
                        FloatingActionButton(
                            onClick = center.value!!,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = stringResource(id = R.string.center_map)
                            )
                        }
                    }

                    // Button to configure the options for the route
                    FloatingActionButton(
                        onClick = { visible.value = true },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .padding(horizontal = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = stringResource(id = R.string.route_options)
                        )
                    }

                    // Button to start recording the route, only if a vehicle is selected
                    if(selectedVehicle.value != null) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.startRecording(
                                    context,
                                    selectedVehicle.value!!.second,
                                    selectedVehicle.value!!.first,
                                    (numberOfPersons.intValue * 75 + (numberOfBulks.value ?: 0) * 5)
                                        .toLong()
                                )
                                viewModel.setDefaultRoute()
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    ) { it ->
        // The options for the search bar are retrieved for observing
        val options by viewModel.searchResults.collectAsState()

        MapBody(
            uiState = uiState,
            recordingUiStateFlow = viewModel.recordingUiState,
            isElectric = isElectric.value,
            options = options,
            center = center,
            isVehicleSelected = when(selectedVehicle.value) { 
                 null -> false
                 else -> true
            },
            search = {
                viewModel.getDestination(it)
            },
            clearOptions = {
                viewModel.clearOptions()
            },
            searchRoutes = { source, destination ->
                viewModel.getRoutes(
                    source = source,
                    destination = destination,
                    vehicleId = selectedVehicle.value?.second ?: -1,
                    additionalMass = (numberOfPersons.intValue * 75 + (numberOfBulks.value ?: 0) * 5).toLong()
                )
            },
            setCurrentRoute = viewModel::fillCurrentRoute,
            startRecording = {
                viewModel.startRecording(
                    context,
                    selectedVehicle.value?.second ?: -1,
                    selectedVehicle.value?.first ?: -1,
                    (numberOfPersons.intValue * 75 + (numberOfBulks.value ?: 0) * 5).toLong()
                )
            },
            cancelRecording = viewModel::cancelRecording,
            getScore = {
                viewModel.getScore(
                    context = context,
                )
            },
            sendFiles = { viewModel.sendFiles(context) },
            clearScore = { viewModel.clearResults() },
            updateFactor = { recordedConsumption, performedConsumption100km, performedRouteDistance ->
                viewModel.updateConsumptionFactor(recordedConsumption, performedConsumption100km,
                    performedRouteDistance, selectedVehicle.value?.second ?: -1)
            },
            modifier = Modifier.padding(it)
        )
    }
}

/**
 * Body of the map screen with all of its functions
 *
 * @param uiState The current state of the ui(start, loading, showing routes, on navigation, etc...)
 * @param recordingUiStateFlow The [StateFlow] that observes the current state of a route recording
 * to show results when finished
 * @param isElectric Flag to detect if the current vehicle is electric to show the result in the
 * corresponding units
 * @param options List of results to choose one with the search bar
 * @param center A function to center the screen when the location permission is obtained
 * @param isVehicleSelected A flag to detect if a vehicle has been selected to deny queries without
 * a proper vehicle
 * @param search Function to update results based on a location query
 * @param clearOptions Function to remove results when one is selected
 * @param searchRoutes Function to search the routes from an origin to a destination [GeoPoint]
 * @param setCurrentRoute Function to register the current route once it is selected
 * @param startRecording Function to start recording a route
 * @param cancelRecording Function to cancel a route recording in course
 * @param getScore Function to get the score of a route using the data obtained from the records
 * @param sendFiles Function to send all the recording files from the phone through another app
 * @param clearScore Function to clear the information of the results from the phone to avoid
 *  showing them multiple times
 * @param updateFactor Function to update the consumption factor after the route if needed
 */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapBody(
    uiState: MapUiState,
    recordingUiStateFlow: StateFlow<RecordingUiState>,
    isElectric: Boolean,
    options: List<NominatimResult>,
    center: MutableState<(() -> Unit)?>,
    isVehicleSelected: Boolean,
    search: (String) -> Unit,
    clearOptions: () -> Unit,
    searchRoutes: (GeoPoint, GeoPoint) -> Unit,
    setCurrentRoute: (Route, String) -> Unit,
    startRecording: () -> Unit,
    cancelRecording: () -> Unit,
    getScore: () -> Unit,
    sendFiles: () -> Unit,
    clearScore: () -> Unit,
    updateFactor: (Double, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // View which contains the map and its functions
    val mapView = rememberMapViewWithLifecycle()
    // State of the recording process to update the ui
    val recordingUiState by recordingUiStateFlow.collectAsState()

    // Location permission in case it is required to show a message
    val fineLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarseLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        null
    }

    // The layout inflater is retrieved to generate popup windows for routes and markers
    val context = LocalContext.current
    val layoutInflater = LayoutInflater.from(context)

    // Overlay to show the current position on the map
    val locationOverlay = rememberLocationOverlayWithLifecycle(mapView)

    // Save the position of the destination point
    var destinationPointPosition by rememberSaveable { mutableStateOf<GeoPoint?>(null) }

    // Asks permission to use location from the phone
    LaunchedEffect(
        fineLocationPermissionState.status.isGranted,
        coarseLocationPermissionState.status.isGranted,
        backgroundLocationState?.status?.isGranted
    ) {
        // Asks only if they are not granted
        if(
            (!fineLocationPermissionState.status.isGranted
            && !coarseLocationPermissionState.status.isGranted) ||
            (backgroundLocationState != null && !backgroundLocationState.status.isGranted)
        ) {
            // If the state of the permissions is changed, the position is removed from the map
            locationOverlay.disableMyLocation()
            mapView.overlayManager.remove(locationOverlay)
            mapView.invalidate()

            // The center button is removed
            center.value = null

            // If the app considers it, a dialog with a warning for the user is shown
            if(fineLocationPermissionState.status.shouldShowRationale) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(R.string.enable_location)
                builder.setMessage(R.string.enable_location_text)
                builder.setPositiveButton("OK") { _, _ ->
                    fineLocationPermissionState.launchPermissionRequest()
                    backgroundLocationState?.launchPermissionRequest()
                }
                builder.show()
            }
            // The permissions are requested otherwise
            else {
                fineLocationPermissionState.launchPermissionRequest()
                backgroundLocationState?.launchPermissionRequest()
            }
        // If the location is granted, the screen is centered towards the current position
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener {
                    if ((it != null) && destinationPointPosition == null) {
                        mapView.controller.setCenter(GeoPoint(it))
                    }
                }

            // A marker for the current location is enabled
            locationOverlay.enableMyLocation()
            mapView.overlayManager.add(locationOverlay)
            mapView.invalidate()

            // The button to center the map is placed
            center.value = {
                if(locationOverlay.myLocation != null) {
                    mapView.controller.setZoom(18.0)
                    mapView.controller.setCenter(locationOverlay.myLocation)
                }
            }
        }
    }

    // Marker which represents the destination point on the map
    val destinationPoint = remember {
        Marker(mapView).apply {
            // A window with a button to search the routes is added when clicking the marker
            this.infoWindow = MyInfoWindow(
                view = MarkerWindowFragment(onClick = {
                    // Checks if the location is available to avoid crash
                    if(locationOverlay.myLocation != null) {
                        searchRoutes(locationOverlay.myLocation, this.position)
                    }
                }).onCreateView(layoutInflater, null, null),
                mapView = mapView
            )

            // Set the position if it was saved
            destinationPointPosition?.let {
                position = it
                mapView.overlayManager.add(this)
                if(uiState is MapUiState.Start) {
                    mapView.controller.setCenter(position)
                    this.showInfoWindow()
                }

                mapView.invalidate()
            }
        }
    }

    // Box with the elements of the map
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // View which contains the map
        AndroidView(
            factory = {
                mapView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Value of the search bar
        var destination by rememberSaveable{ mutableStateOf("") }

        // Events receiver to add markers on click
        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            @SuppressLint("DefaultLocale")
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                // The position of the previous marker is updated
                mapView.overlayManager.remove(destinationPoint)
                destinationPoint.position = p
                destinationPointPosition = p
                mapView.overlayManager.add(destinationPoint)

                // The map centers to the marker with an animation
                mapView.controller.animateTo(destinationPoint.position)
                // The information window is shown to select the route
                destinationPoint.showInfoWindow()
                // The destination from the search bar is updated
                destination = String.format("%.8f", p.latitude) + ";" + String.format("%.8f", p.longitude)
                mapView.invalidate()

                return false
            }

            // Nothing happens when the screen is pressed for long time periods
            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
        // A layer is attached to the map to allow selection of destination points with a click
        val overlayEvents = remember{ MapEventsOverlay(mReceive) }

        // The layer for selecting destination is added or removed based on the vehicle selection
        LaunchedEffect(isVehicleSelected) {
            if(isVehicleSelected) {
                if(!mapView.overlays.contains(overlayEvents)) {
                    mapView.overlays.add(overlayEvents)
                }
            } else {
                mapView.overlays.remove(overlayEvents)
            }
        }

        // A flag to remember if the options from the search are shown
        var searchExpanded by rememberSaveable { mutableStateOf(false) }
        // Manager to remove focus from the search bar when an option is selected
        val focusManager = LocalFocusManager.current

        // List with the routes represented on the map
        val routeOverlays: MutableList<Polyline> = remember { mutableListOf() }

        // The current route selected, being the index of the list of polylines
        var selectedRoute by rememberSaveable { mutableIntStateOf(0) }

        // Search bar with address options to select from
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
                    destination = it
                    clearOptions()
                },
                singleLine = true,
                enabled = isVehicleSelected,
                readOnly = recordingUiState is RecordingUiState.Loading,
                label = { if(isVehicleSelected) Text(stringResource(id = R.string.destination))
                        else Text(stringResource(id = R.string.select_vehicle))},
                trailingIcon = {
                    // Button to remove the contents of the search bar and the marker
                    if (destination.isNotBlank()) {
                        IconButton(
                            onClick = {
                                destination = ""
                                clearOptions()
                                searchExpanded = false
                                if(mapView.overlayManager.remove(destinationPoint)) {
                                    destinationPoint.closeInfoWindow()
                                    destinationPointPosition = null
                                    for(route in routeOverlays) {
                                        route.closeInfoWindow()
                                    }
                                    mapView.invalidate()
                                }
                                cancelRecording()
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
            // List with the address options of the search bar
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
                        // Clickable for an address
                        DropdownMenuItem(
                            text = { Text(it.displayName) },
                            onClick = {
                                // The search bar is updated
                                destination = it.displayName
                                destinationPoint.position = GeoPoint(
                                    it.lat,
                                    it.lon
                                )
                                destinationPointPosition = destinationPoint.position
                                // The info window is closed and the destination gets updated
                                destinationPoint.closeInfoWindow()
                                mapView.controller.setCenter(destinationPoint.position)
                                mapView.overlayManager.remove(destinationPoint)
                                mapView.overlayManager.add(destinationPoint)

                                // The search bar loses focus
                                searchExpanded = false
                                focusManager.clearFocus()
                                mapView.invalidate()

                                // The info window of the destination is shown
                                destinationPoint.showInfoWindow()
                            },
                            contentPadding = PaddingValues(10.dp),
                            modifier = Modifier.requiredHeight(40.dp)
                        )

                        // A divider is added to every element in between
                        if (options.last() != it) {
                            HorizontalDivider(thickness = 1.dp)
                        }
                    }
                }
            }

            // When the recording ends, the results are sent once
            LaunchedEffect(recordingUiState) {
                when (recordingUiState) {
                    is RecordingUiState.Complete -> {
                        // The map stops following the phone position
                        locationOverlay.disableFollowLocation()
                        locationOverlay.enableAutoStop = true
                        mapView.mapOrientation = 0F
                        // Enable clicking on markers to calculate routes
                        destinationPoint.setOnMarkerClickListener(null)
                        // The scores are obtained and the ui updates
                        getScore()
                    }

                    is RecordingUiState.Default -> {
                        // The map stops following the phone position
                        locationOverlay.disableFollowLocation()
                        locationOverlay.enableAutoStop = true
                        mapView.mapOrientation = 0F

                        // Enable clicking on markers to calculate routes
                        destinationPoint.setOnMarkerClickListener(null)

                        // The layer to select destination with a click is added
                        if(!mapView.overlays.contains(overlayEvents)) {
                            if(isVehicleSelected) {
                                mapView.overlays.add(overlayEvents)
                            }
                        }
                    }

                    // If a recording is taking place
                    else -> {
                        // The map follows the position of the phone
                        locationOverlay.enableFollowLocation()
                        locationOverlay.enableAutoStop = false
                        mapView.controller.setZoom(18.0)

                        // The window of the marker is closed and click is disabled to avoid errors
                        destinationPoint.closeInfoWindow()
                        destinationPoint.setOnMarkerClickListener { _, _ ->  true}

                        // Markers cannot be placed during a route
                        if(mapView.overlays.contains(overlayEvents))
                            mapView.overlays.remove(overlayEvents)

                        // The current route is drawn, removing others
                        if(uiState is MapUiState.CompleteRoutes) {
                            // If the screen resets, routes are drawn again to restore it
                            while (routeOverlays.isEmpty()) {
                                delay(1000)
                            }
                            mapView.overlayManager.removeAll(routeOverlays)
                            val polyline = routeOverlays[selectedRoute]
                            routeOverlays.clear()
                            routeOverlays.add(polyline)
                            mapView.insertPolyline(polyline)

                            mapView.invalidate()
                        }

                        // The screen rotates based on the current orientation of the car
                        while (!locationOverlay.enableAutoStop) {
                            // If the location is not available, the loop is skipped
                            if (locationOverlay.lastFix == null) {
                                delay(1000)
                                continue
                            }
                            val bearing: Float = locationOverlay.lastFix.bearing

                            var t = (360 - bearing)
                            if (t < 0) {
                                t += 360f
                            }
                            if (t > 360) {
                                t -= 360f
                            }

                            mapView.mapOrientation = t
                            delay(1000)
                        }
                    }
                }
            }
        }

        // If there is a change on the ui, the routes on the map change
        LaunchedEffect(uiState) {
            // If there is a response from the server with the routes, they are shown
            if (uiState is MapUiState.CompleteRoutes) {
                // The layer for putting a marker with a click is removed
                mapView.overlays.remove(overlayEvents)

                // The bounds are computed to focus the screen with all the routes visible
                var bounds = destinationPoint.bounds
                // If the location is not available, it waits until it is
                while (locationOverlay.lastFix == null) {
                    delay(1000)
                }
                bounds = bounds.concat(BoundingBox.fromGeoPoints(
                    listOf(locationOverlay.myLocation))
                )

                // For each route
                for((index, route) in uiState.routes.withIndex()) {
                    // A polyline is drawn with its parameters
                    val polyline = Polyline(mapView)

                    polyline.outlinePaint.strokeCap = Paint.Cap.ROUND
                    polyline.outlinePaint.strokeWidth = 20F

                    // Most important label of the route
                    val label: String

                    // The color is decided based on the most important label it contains
                    polyline.outlinePaint.color = if(route.first.contains("eco")) {
                        label = "eco"
                        Color.GREEN
                    } else if(route.first.contains("fastest")) {
                        label = "fastest"
                        Color.BLUE
                    } else if (route.first.contains("shortest")) {
                        label = "shortest"
                        Color.MAGENTA
                    } else {
                        label = "undefined"
                        Color.RED
                    }

                    // The coordinates are added to the line
                    val coordinates = route.second.processedRoute
                    for(cord in coordinates) {
                        polyline.addPoint(GeoPoint(cord.first, cord.second))
                    }

                    // A window is added to show information about the route when it is clicked
                    polyline.infoWindow = MyInfoWindow(
                        view = RouteWindowFragment(
                            _uiState = recordingUiStateFlow,
                            route = route.second,
                            isElectric = isElectric,
                            onClick = {
                                // The data of the current route is stored
                                setCurrentRoute(route.second, label)
                                selectedRoute = index
                                // A recording starts and the map focuses on the location of the car
                                startRecording()

                                mapView.invalidate()
                            },
                            onCancel = cancelRecording
                        ).onCreateView(layoutInflater,null,null),
                        mapView = mapView
                    )

                    // The bounds get bigger using the ones from the new route to show
                    bounds = bounds.concat(polyline.bounds)

                    // The route is drawn keeping the markers as the head objects from the map
                    if(recordingUiState is RecordingUiState.Default) {
                        mapView.insertPolyline(polyline)
                    }
                    routeOverlays.add(polyline)
                }
                // The map view is updated with the new changes, and it zooms to the new routes
                mapView.invalidate()
                mapView.zoomToBoundingBox(bounds.increaseByScale(1.3f),
                    true)
            }
            // If the routes are not selected
            else if (uiState is MapUiState.Start || uiState is MapUiState.LoadingRoute) {
                // The layer to select destination with a click is added
                if(!mapView.overlays.contains(overlayEvents)) {
                    if(isVehicleSelected && recordingUiState is RecordingUiState.Default) {
                        mapView.overlays.add(overlayEvents)
                    }
                }
                // All the routes from the map are removed if they existed previously
                if(routeOverlays.isNotEmpty()) {
                    // All the info windows are closed
                    for(route in routeOverlays) {
                        route.closeInfoWindow()
                    }
                    mapView.overlayManager.removeAll(routeOverlays)
                    mapView.invalidate()

                    routeOverlays.clear()
                }
            }
        }

        // Changes the popups that should show based on the current ui state
        when (uiState) {
            is MapUiState.LoadingRoute -> {
                LoadingRouteDialog()
            }

            is MapUiState.CompleteRoutes -> {
                if(recordingUiState is RecordingUiState.Default) {
                    // A legend with information of the routes is added at the bottom of the screen
                    RoutesLegend(
                        routes = uiState.routes,
                        onClick = {
                            // The selected route is set as the head of the layers list
                            mapView.overlayManager.remove(routeOverlays[it])
                            mapView.insertPolyline(routeOverlays[it])
                            mapView.invalidate()

                            // All the info windows are closed
                            for(route in routeOverlays) {
                                route.closeInfoWindow()
                            }

                            // A window is opened for the route in the middle point
                            routeOverlays[it].infoWindowLocation =
                                routeOverlays[it].actualPoints[routeOverlays[it].actualPoints.size/2]
                            routeOverlays[it].showInfoWindow()
                        },
                        modifier = Modifier.align(Alignment.BottomEnd))
                }
            }

            is MapUiState.Error -> {
                ErrorMessage(uiState.code)
            }

            is MapUiState.CompleteScore -> {
                ScoresResult(score = uiState.scores, isElectric = isElectric,
                    sendFiles = sendFiles, clearScore = clearScore,
                    needsConsumption = uiState.needsConsumption,
                    updateFactor = {
                        updateFactor(
                            uiState.scores.performedRouteConsumption,
                            it,
                            uiState.scores.performedRouteDistance
                        )
                    }
                )
            }

            else -> {}
        }

    }
}

/**
 * Legend to show the labels of each route drawn on the map
 *
 * @param routes The routes to show information about
 * @param onClick The function that focuses the information of a route when clicking its name
 */
@Composable
fun RoutesLegend(
    routes: List<Pair<List<String>,Route>>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // The possible labels
            val labels = mapOf(
                "fastest" to stringResource(id = R.string.fastest),
                "shortest" to stringResource(id = R.string.shortest),
                "eco" to stringResource(id = R.string.eco_route)
            )
            // For each route, the colors and labels are decided
            val legend = routes.map {
                val color = if (it.first.contains("eco")) {
                    Green
                } else if (it.first.contains("fastest")) {
                    Blue
                } else if (it.first.contains("shortest")) {
                    Magenta
                } else {
                    Red
                }

                // The label is formed joining all the string with a comma in the middle
                val label = it.first.joinToString(separator = ", ") { type ->
                    labels[type] ?: ""
                }

                // A list with the color and label of each route is returned
                Pair(color, label)
            }

            // For each route, a Row with its information is shown
            legend.forEachIndexed { index, (color, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                        .clickable { onClick(index) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(color)
                    )
                    Text(text = " $label", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}