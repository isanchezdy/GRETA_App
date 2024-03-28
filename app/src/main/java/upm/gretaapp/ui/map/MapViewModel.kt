package upm.gretaapp.ui.map

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.NominatimRepository
import upm.gretaapp.data.RecordingRepository
import upm.gretaapp.data.PhoneSessionRepository
import upm.gretaapp.model.NominatimResult
import upm.gretaapp.model.Route
import upm.gretaapp.model.PerformedRouteMetrics
import upm.gretaapp.model.InputPerformedRoute
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import java.net.ConnectException
import java.util.Date
import kotlin.math.abs

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
 */
class MapViewModel(
    private val nominatimRepository: NominatimRepository,
    private val phoneSessionRepository: PhoneSessionRepository,
    private val gretaRepository: GretaRepository,
    private val recordingRepository: RecordingRepository
): ViewModel() {

    // Current user
    private var userId: Long = 0

    // List of vehicles of the current user
    private val _vehicleList = MutableStateFlow<List<Pair<UserVehicle, Vehicle>>>(emptyList())
    val vehicleList = _vehicleList.asStateFlow()

    private var filename: String = ""

    init {
        viewModelScope.launch {
            // The current user is retrieved and updated
            phoneSessionRepository.user.collectLatest {
                userId = it
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

    // Variables that manage the current state of the ui, showing a read-only value to the screen
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Start)
    val uiState = _uiState.asStateFlow()

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
        _uiState.value = MapUiState.Start
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
                _uiState.value = MapUiState.LoadingRoute

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
                _uiState.value = MapUiState.CompleteRoutes(routes = processedRoutes)
            } catch(connectException: ConnectException) {
                // If the app can't connect to the server
                _uiState.value = MapUiState.Error(1)
            } catch (throwable: Throwable) {
                _uiState.value = MapUiState.Error(2)
                Log.e("Error_route", throwable.stackTraceToString())
            }
        }
    }


    /**
     * Function to start recording a route
     *
     * @param vehicleId The id of the vehicle performing the route
     */
    fun startRecording(context: Context, vehicleId: Long) {
        filename = "user " + userId.toString() + " vehicle " + vehicleId.toString() +
                " " + Date().toString()
        continueRoute(context)
        recordingRepository.recordRoute(userId = userId, vehicleId = vehicleId, filename)
    }

    /**
     * Function to stop recording a route and restart the ui
     */
    fun cancelRecording() {
        recordingRepository.cancelWork()
        _uiState.value = MapUiState.Start
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
            // If the recording finished successfully
            if(recordingUiState.value is RecordingUiState.Complete) {
                try {
                    // The results are loading
                    _uiState.value = MapUiState.LoadingRoute

                    val recordingResults = withContext(Dispatchers.IO) {
                        return@withContext readFile(
                            context,
                            (recordingUiState.value as RecordingUiState.Complete).filename
                        )
                    }

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
                    // The scores are retrieved and the ui is updated
                    var scores = gretaRepository.calculatePerformedRouteMetrics(input)
                    scores = scores.copy(performedRouteConsumption =
                    scores.performedRouteConsumption
                            * phoneSessionRepository.vehicleFactor(vehicleId).last())
                    _uiState.value = MapUiState.CompleteScore(
                        scores,
                        phoneSessionRepository.needsConsumption(vehicleId).last()
                    )
                } catch(connectException: ConnectException) {
                    // If there is a connection error, a message is shown
                    _uiState.value = MapUiState.Error(1)
                } catch (throwable : Throwable) {
                    // If there is other kind of error, another message is shown
                    _uiState.value = MapUiState.Error(2)
                    Log.e("Error_route", throwable.stackTraceToString())
                }
            }
        }
    }

    fun finishRoute(context: Context) {
        writeState(context, "$filename state.txt", "finished")
    }

    fun pauseRoute(context: Context) {
        writeState(context, "$filename state.txt", "paused")
    }

    fun continueRoute(context: Context) {
        writeState(context, "$filename state.txt", "started")
    }

    /**
     * Function to clear the results of the recording after accepting
     */
    fun clearResults() {
        recordingRepository.clearResults()
    }

    /**
     * Function to send the recording files through another app
     *
     * @param context The [Context] used to send the file
     */
    fun sendFiles(context: Context) {
        sendFiles(context, userId)
    }

    fun updateConsumptionFactor(
        recordedConsumption: Double,
        performedConsumption100km: Double,
        performedRouteDistance: Double,
        vehicleId: Long
    ) {
        viewModelScope.launch {
            val performedConsumption =
                (performedConsumption100km / 100000.0) * performedRouteDistance
            if (abs(performedConsumption - recordedConsumption) <= 0.1) {
                phoneSessionRepository.saveNeedsConsumption(
                    vehicleId,
                    false
                )
            } else {
                val newConsumptionFactor = (performedConsumption / recordedConsumption) *
                        phoneSessionRepository.vehicleFactor(vehicleId).last()
                phoneSessionRepository.saveVehicleFactor(vehicleId, newConsumptionFactor)
            }
        }
    }

}

/**
 * Interface that represents the different states of the screen
 */
sealed interface MapUiState {
    data object Start: MapUiState
    data object LoadingRoute: MapUiState
    data class Error(val code: Int): MapUiState
    data class CompleteRoutes(val routes: List<Pair<List<String>, Route>>): MapUiState
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