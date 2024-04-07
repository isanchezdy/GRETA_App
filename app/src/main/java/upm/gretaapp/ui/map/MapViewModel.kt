package upm.gretaapp.ui.map

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerializationException
import org.osmdroid.util.GeoPoint
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.NominatimRepository
import upm.gretaapp.data.RecordingRepository
import upm.gretaapp.data.PhoneSessionRepository
import upm.gretaapp.data.VehicleFactorRepository
import upm.gretaapp.model.NominatimResult
import upm.gretaapp.model.Route
import upm.gretaapp.model.PerformedRouteMetrics
import upm.gretaapp.model.InputPerformedRoute
import upm.gretaapp.model.UserRoute
import upm.gretaapp.model.UserStats
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import upm.gretaapp.model.VehicleFactor
import upm.gretaapp.model.fillFromPerformedMetrics
import upm.gretaapp.model.fillFromRoute
import java.net.ConnectException
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private const val MAP_UI_SAVED_STATE_KEY = "MapUiStateKey"
private const val FILENAME_KEY = "FilenameKey"
private const val ROUTE_KEY = "RouteKey"

/**
 * [ViewModel] that manages the current UI state of the Map Screen (search a destination, a route,
 * showing results, etc...)
 *
 * @param nominatimRepository Repository class to retrieve destinations from a text query and place
 *  them on the map
 *  @param phoneSessionRepository Repository class to retrieve the current user of the app and other
 *  information contained in the phone
 *  @param gretaRepository Repository class to retrieve information from the dedicated server
 *  @param recordingRepository Repository class to start recording a route when selected
 *  @param vehicleFactorRepository Repository class to manage consumption data about the vehicles
 *  @param state Object to safely store information in a persistent volume of the state of the UI
 */
class MapViewModel(
    private val nominatimRepository: NominatimRepository,
    private val phoneSessionRepository: PhoneSessionRepository,
    private val gretaRepository: GretaRepository,
    private val recordingRepository: RecordingRepository,
    private val vehicleFactorRepository: VehicleFactorRepository,
    private val state: SavedStateHandle
): ViewModel() {

    // Current user
    private var userId: Long = 0

    // List of vehicles of the current user
    private val _vehicleList = MutableStateFlow<List<Pair<UserVehicle, Vehicle>>>(emptyList())
    val vehicleList = _vehicleList.asStateFlow()

    // Data recollected from the current route to send when it finishes
    private val currentRoute: StateFlow<UserRoute> = state.getStateFlow(ROUTE_KEY,
        UserRoute(
            userId = userId,
            userVehicleId = -1,
            additionalMass = 0,
            sourceCoords = "",
            destinationCoords = "",
            recordDate = "",
            selectedRoutePolyline = "",
            selectedRouteType = "",
            selectedRouteConsumption = 0.0,
            selectedRouteDistance = 0,
            selectedRouteTime = 0,
            performedRoutePolyline = "",
            performedRouteConsumption = 0.0,
            performedRouteTime = 0,
            performedRouteDistance = 0,
            performedRouteEstimatedConsumption = 0.0,
            performedRouteEstimatedTime = 0,
            performedRouteEstimatedDistance = 0,
            numStopsKm = 0,
            speedVariationNum = 0,
            drivingAggressiveness = 0
        )
    )

    /**
     * Method to update the current [UserRoute] stored in [SavedStateHandle]
     *
     * @param route [UserRoute] to store
     */
    private fun setCurrentRoute(route: UserRoute) {
        state[ROUTE_KEY] = route
    }

    init {
        viewModelScope.launch {
            // The current user is retrieved and updated
            phoneSessionRepository.user.collectLatest {
                userId = it
                // The current route's user is updated
                setCurrentRoute(currentRoute.value.copy(userId = userId))
                // All of its vehicles are retrieved
                _vehicleList.value = try {
                    val userVehicles = gretaRepository.getUserVehicles(userId)
                    val list: MutableList<Pair<UserVehicle, Vehicle>> = mutableListOf()
                    for (userVehicle in userVehicles) {
                        list.add(
                            Pair(
                                userVehicle,
                                gretaRepository.getVehicle(userVehicle.vehicleId)
                            )
                        )
                    }
                    list
                } catch (_: Throwable) {
                    emptyList()
                }
            }
        }
    }

    // Variable that manages the current state of the ui, showing a read-only value to the screen
    val uiState: StateFlow<MapUiState> = state.getStateFlow(MAP_UI_SAVED_STATE_KEY, MapUiState.Start)

    /**
     * Method to update the current [MapUiState] stored in [SavedStateHandle]
     *
     * @param uiState [MapUiState] to store
     */
    private fun setMapUiState(uiState: MapUiState) {
        state[MAP_UI_SAVED_STATE_KEY] = uiState
    }

    // Variable that stores the current name of the file of the recording
    private val filename: StateFlow<String> = state.getStateFlow(FILENAME_KEY, "")

    /**
     * Method to update the current filename stored in [SavedStateHandle]
     *
     * @param filename The filename to update
     */
    private fun setFilename(filename: String) {
        state[FILENAME_KEY] = filename
    }

    /**
     * Method to update the current route using the fields of a [Route] and its label
     *
     * @param route [Route] with the fields to fill the current route
     * @param label Label of the current route being recorded
     */
    fun fillCurrentRoute(route: Route, label: String) {
        setCurrentRoute(currentRoute.value.fillFromRoute(route).copy(selectedRouteType = label))
    }

    // Search results based on the user query
    private val _searchResults: MutableStateFlow<List<NominatimResult>> =
        MutableStateFlow(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Flow that represents the current state of recording
    val recordingUiState: StateFlow<RecordingUiState> = recordingRepository.outputWorkInfo
        .map { info ->
            val filename = info?.outputData?.getString("filename")
            when  {
                info == null ->
                    RecordingUiState.Default
                // If the recording is finished
                info.state.isFinished && filename != null -> {
                    RecordingUiState.Complete(
                        filename = filename
                    )
                }
                // When it has been cancelled
                info.state == WorkInfo.State.CANCELLED -> {
                    RecordingUiState.Default
                }
                // If it is still running
                !info.state.isFinished -> RecordingUiState.Loading
                // If it has finished with other state
                else -> RecordingUiState.Default
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // It always starts with the option to start recording
            initialValue = RecordingUiState.Default
        )

    /**
     * Function to update search results based on a query
     *
     * @param destination The query from which the results are obtained
     */
    fun getDestination(destination: String) {
        viewModelScope.launch {
            _searchResults.value = try {
                nominatimRepository.getNominatimResults(destination)
            } catch (throwable: Throwable) {
                emptyList()
            }
        }
    }

    /**
     * Function to clear the select options of the search bar
     */
    fun clearOptions() {
        _searchResults.value = emptyList()
        setMapUiState(MapUiState.Start)
    }

    /**
     * Function to retrieve the route results based on a query and update the ui
     *
     * @param source The starting point of the route
     * @param destination The final point of the route
     * @param userId The id of the current user
     * @param vehicleId The id of the selected vehicle for the routes
     * @param additionalMass Additional mass to take into account for the consumption
     */
    fun getRoutes(
        source: GeoPoint,
        destination: GeoPoint,
        userId: Long = this.userId,
        vehicleId: Long = -1,
        additionalMass: Long = 0
    ) {
        viewModelScope.launch {
            try{
                // The ui is updated to show a loading screen
                setMapUiState(MapUiState.LoadingRoute)

                // Previous results are cleaned if an error happened to prevent inconsistencies
                if(recordingUiState.value is RecordingUiState.Complete) {
                    recordingRepository.clearResults()
                }

                // Saving data of the current route such as the coordinates or the vehicle used
                setCurrentRoute(currentRoute.value.copy(
                    userVehicleId = vehicleList.value.find {
                        it.first.vehicleId == vehicleId
                    }?.first?.id ?: -1,
                    additionalMass = additionalMass,
                    sourceCoords = "${source.latitude};${source.longitude}",
                    destinationCoords = "${destination.latitude};${destination.longitude}"
                ))

                // The routes are retrieved from the database
                val routes = gretaRepository.getRoutes(
                    source = source.latitude.toFloat().toString() + ","
                            + source.longitude.toFloat().toString(),
                    destination = destination.latitude.toFloat().toString() + ","
                            + destination.longitude.toFloat().toString(),
                    innerCoords = "",
                    additionalMass = additionalMass,
                    vehicleId = vehicleId,
                    userId = userId
                )

                // The routes get grouped to remove duplicates and their labels are redistributed
                val processedRoutes = routes.entries.groupBy(
                    keySelector = { it.value },
                    valueTransform = { it.key }
                ).map { (value, keys) -> keys to value }

                // The routes are shown in the ui
                setMapUiState(MapUiState.CompleteRoutes(routes = processedRoutes))
            } catch(connectException: ConnectException) {
                // If the app can't connect to the server
                setMapUiState(MapUiState.Error(1))
            } catch (throwable: Throwable) {
                setMapUiState(MapUiState.Error(2))
                Log.e("Error_route", throwable.stackTraceToString())
            }
        }
    }


    /**
     * Function to start recording a route
     *
     * @param context [Context] to initialize the state of the route
     * @param vehicleId The id of the vehicle performing the route
     */
    fun startRecording(context: Context, vehicleId: Long) {
        // The current date is retrieved and formatted
        val date = Date()
        setCurrentRoute(currentRoute.value.copy(
            recordDate = SimpleDateFormat("yyyy-MM-Ya dd'T'HH:mm:ss", Locale.getDefault()).format(date))
        )
        // The filename of the recording is stored
        setFilename("user " + userId.toString() + " vehicle " + vehicleId.toString() +
                " " + Date().toString())
        // The state of the recording process is initialized
        updateStateRoute(context, "started")
        // The recording process is started
        recordingRepository.recordRoute(userId = userId, vehicleId = vehicleId, filename.value)
    }

    /**
     * Function to stop recording a route and restart the ui
     */
    fun cancelRecording() {
        recordingRepository.cancelWork()
        setMapUiState(MapUiState.Start)
    }

    /**
     * Function to retrieve the score of a route after a recording
     *
     * @param context Context to read the recording file
     * @param vehicleId Id of the [Vehicle] that was used for the route
     * @param additionalMass Additional mass on the vehicle for the route
     */
    fun getScore(
        context: Context,
        vehicleId: Long = -1,
        additionalMass: Long = 0
    ) {
        viewModelScope.launch {
            val vehicleFactor = withContext(Dispatchers.IO) {
                // The current vehicle factor is retrieved if it exists
                return@withContext vehicleFactorRepository.getVehicleFactorStream(vehicleId)
            }

            // If the recording finished successfully
            if(recordingUiState.value is RecordingUiState.Complete) {
                try {
                    // The results are loading
                    setMapUiState(MapUiState.LoadingRoute)

                    // The data is retrieved from the record file
                    val recordingResults = withContext(Dispatchers.IO) {
                        return@withContext readFile(
                            context,
                            (recordingUiState.value as RecordingUiState.Complete).filename
                        )
                    }

                    // The input for the request is initialized
                    val input =
                        InputPerformedRoute(vehicleId = vehicleId,
                            additionalMass = additionalMass,
                            speeds = recordingResults.first, heights = recordingResults.second,
                            times = (recordingResults.first.indices).toList()
                                .map {
                                    it.toDouble()
                                },
                            routePolyline = recordingResults.third
                        )
                    // The information about the route polyline is updated in the current route
                    setCurrentRoute(currentRoute.value.copy(performedRoutePolyline = recordingResults.third))

                    // The scores are retrieved and the ui is updated
                    var scores = gretaRepository.calculatePerformedRouteMetrics(input)
                    // The consumption applies the stored factor for the vehicle if it exists
                    scores = scores.copy(performedRouteConsumption =
                        scores.performedRouteConsumption
                            * (vehicleFactor?.factor ?: 1.0))
                    // The ui is updated with the results
                    setMapUiState(MapUiState.CompleteScore(
                        scores,
                        vehicleFactor?.needsConsumption ?: true
                    ))

                    // The current route updates its data about the scores obtained
                    setCurrentRoute(currentRoute.value.fillFromPerformedMetrics(scores))
                } catch(connectException: ConnectException) {
                    // If there is a connection error, a message is shown
                    setMapUiState(MapUiState.Error(1))
                } catch (throwable : Throwable) {
                    // If there is other kind of error, another message is shown
                    setMapUiState(MapUiState.Error(2))
                    Log.e("Error_route", throwable.stackTraceToString())
                }
            }
        }
    }

    /**
     * Function to update the state of a route being recorded
     *
     * @param context [Context] to write the file
     * @param state New state to apply to the recording (started, paused, finished)
     */
    fun updateStateRoute(context: Context, state: String) {
        writeState(context, "${filename.value} state.txt", state)
    }

    /**
     * Function to clear the results of the recording after accepting
     */
    fun clearResults() {
        recordingRepository.clearResults()
        viewModelScope.launch {
            // The user stats are created if there are none for the current user
            var userStats: UserStats? = null
            try{
                gretaRepository.getStatsUser(userId)
            } catch(serializationException: SerializationException) {
                // If an empty object is returned, an object is inserted
                userStats = UserStats(
                    consumptionSaving = currentRoute.value
                        .performedRouteEstimatedConsumption
                            - currentRoute.value.performedRouteConsumption,
                    driveRating = listOf(currentRoute.value.numStopsKm,
                        currentRoute.value.drivingAggressiveness,
                        currentRoute.value.speedVariationNum).average(),
                    ecoDistance = if(currentRoute.value.selectedRouteType == "eco") {
                        currentRoute.value.performedRouteDistance.toDouble()
                    } else 0.0,
                    ecoRoutesNum = if(currentRoute.value.selectedRouteType == "eco") {
                        1
                    } else 0,
                    ecoTime = if(currentRoute.value.selectedRouteType == "eco") {
                        currentRoute.value.performedRouteTime.toDouble()
                    } else 0.0,
                    userID = userId
                )
            } catch (_: Throwable) {

            }
            try {
                // If there are no user stats, an object is created
                if(userStats != null) {
                    gretaRepository.createUserStats(
                        userStats
                    )
                }
                // A user route is added to the history
                gretaRepository.createUserRoute(currentRoute.value)
            } catch (throwable: Throwable) {
                Log.e("Clear_results", throwable.stackTraceToString())
            }
        }
    }

    /**
     * Function to send the recording files through another app
     *
     * @param context The [Context] used to send the file
     */
    fun sendFiles(context: Context) {
        sendFiles(context, userId)
    }

    /**
     * Updates the consumption factor of the [Vehicle] with the given [vehicleId]
     *
     * @param recordedConsumption The consumption obtained from the score given to a recording
     * @param performedConsumption100km The consumption indicated by a vehicle
     * @param performedRouteDistance The distance obtained from a recording
     * @param vehicleId The id of a certain vehicle of the database
     */
    fun updateConsumptionFactor(
        recordedConsumption: Double,
        performedConsumption100km: Double,
        performedRouteDistance: Double,
        vehicleId: Long
    ) {
        viewModelScope.launch {
            val vehicleFactor = withContext(Dispatchers.IO) {
                // The current vehicle factor is retrieved if it exists
                return@withContext vehicleFactorRepository.getVehicleFactorStream(vehicleId)
            }

            // The consumption is retrieved from the mean consumption every 100 km
            val performedConsumption =
                (performedConsumption100km / 100000.0) * performedRouteDistance

            // If the difference between consumptions is less than 0.05
            if (abs(performedConsumption - recordedConsumption) <= 0.05) {
                // The flag needsConsumption is set to false
                if(vehicleFactor == null) {
                    vehicleFactorRepository.insertVehicleFactor(
                        VehicleFactor(id = vehicleId, needsConsumption = false)
                    )
                }
                else {
                    vehicleFactor.copy(needsConsumption = false).let {
                        vehicleFactorRepository.updateVehicleFactor(
                            it
                        )
                    }
                }
            } else {
                // The new consumption factor is calculated and updated
                val newConsumptionFactor = (performedConsumption / recordedConsumption) *
                        (vehicleFactor?.factor ?: 1.0)
                if(vehicleFactor == null) {
                    vehicleFactorRepository.insertVehicleFactor(
                        VehicleFactor(id = vehicleId, factor = newConsumptionFactor)
                    )
                }
                else {
                    vehicleFactor.copy(factor = newConsumptionFactor).let {
                        vehicleFactorRepository.updateVehicleFactor(
                            it
                        )
                    }
                }
            }
        }
    }

}

/**
 * Interface that represents the different states of the screen
 */
@Parcelize
sealed interface MapUiState : Parcelable {
    @Parcelize
    data object Start: MapUiState
    @Parcelize
    data object LoadingRoute: MapUiState
    @Parcelize
    data class Error(val code: Int): MapUiState
    @Parcelize
    data class CompleteRoutes(val routes: List<Pair<List<String>, Route>>): MapUiState
    @Parcelize
    data class CompleteScore(val scores: PerformedRouteMetrics, val needsConsumption: Boolean): MapUiState
}

/**
 * Interface that represents the different states of the recording process for a route
 */
sealed interface RecordingUiState {
    data object Default : RecordingUiState
    data object Loading : RecordingUiState
    data class Complete(val filename: String ): RecordingUiState
}

/**
 * Extension function to convert a [GeoPoint] to a [Pair]<latitude, longitude>
 *
 * @return The resulting [Pair] of coordinates from the [GeoPoint]
 */
fun GeoPoint.toPair(): Pair<Double, Double> {
    return Pair(this.latitude, this.longitude)
}