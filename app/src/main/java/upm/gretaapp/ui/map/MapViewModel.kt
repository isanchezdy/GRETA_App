package upm.gretaapp.ui.map

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.NominatimRepository
import upm.gretaapp.data.RecordingRepository
import upm.gretaapp.data.UserSessionRepository
import upm.gretaapp.model.NominatimResult
import upm.gretaapp.model.Route
import upm.gretaapp.model.RouteEvaluation
import upm.gretaapp.model.RouteEvaluationInput
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import java.net.ConnectException

/**
 * [ViewModel] that manages the current UI state of the Map Screen (search a destination, a route,
 * showing statistics, etc...)
 *
 * @param nominatimRepository Repository class to retrieve destinations from a text query and place
 *  them on the map
 *  @param recordingRepository Repository class to start recording a route when selected
 */
class MapViewModel(
    private val nominatimRepository: NominatimRepository,
    userSessionRepository: UserSessionRepository,
    private val gretaRepository: GretaRepository,
    private val recordingRepository: RecordingRepository
): ViewModel() {

    private var userId: Long = 0

    private val _vehicleList = MutableStateFlow<List<Pair<UserVehicle, Vehicle>>>(emptyList())
    val vehicleList = _vehicleList.asStateFlow()

    init {
        viewModelScope.launch {
            userSessionRepository.user.collectLatest {
                userId = it
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

    val recordingUiState: StateFlow<RecordingUiState> = recordingRepository.outputWorkInfo
        .map { info ->
            val outputSpeeds = info.outputData.getDoubleArray("speeds")
            val outputHeights = info.outputData.getDoubleArray("heights")
            when  {
                info.state.isFinished && outputSpeeds != null
                        && outputHeights != null -> {
                    RecordingUiState.Complete(
                        speeds = outputSpeeds.asList(),
                        heights = outputHeights.asList()
                    )
                }
                info.state == WorkInfo.State.CANCELLED -> {
                    RecordingUiState.Default
                }
                else -> RecordingUiState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
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

    fun getRoutes(
        source: GeoPoint,
        destination: GeoPoint,
        userId: Long = this.userId,
        vehicleId: Long = -1,
        additionalMass: Long = 0
    ) {
        viewModelScope.launch {
            try{
                _uiState.value = MapUiState.LoadingRoute

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

                val processedRoutes = routes.entries.groupBy(
                    keySelector = { it.value },
                    valueTransform = { it.key }
                ).map { (value, keys) -> keys to value }

                _uiState.value = MapUiState.CompleteRoutes(routes = processedRoutes)
            } catch(connectException: ConnectException) {
                _uiState.value = MapUiState.Error(1)
            } catch (throwable: Throwable) {
                _uiState.value = MapUiState.Error(2)
                Log.e("Error_route", throwable.stackTraceToString())
            }
        }
    }


    /**
     * Function to start recording a route
     */
    fun startRecording(vehicleId: Long, destination: GeoPoint) {
        recordingRepository.recordRoute(userId = userId, vehicleId = vehicleId, destination)
        recordingUiState
    }

    fun cancelRecording() {
        recordingRepository.cancelWork()
    }

    fun getScore(
        speeds: List<Double>,
        heights: List<Double>,
        vehicleId: Long = -1,
        additionalMass: Long = 0
    ) {
        viewModelScope.launch {
            if(recordingUiState.value is RecordingUiState.Complete) {
                try {
                    _uiState.value = MapUiState.LoadingRoute
                    val input =
                        RouteEvaluationInput(userId = userId, vehicleId = vehicleId,
                            additionalMass = additionalMass,
                            speeds = speeds, heights = heights, times = (speeds.indices).toList()
                                .map {
                                    it.toDouble()
                                }
                        )

                    val scores = gretaRepository.getScore(input)
                    _uiState.value = MapUiState.CompleteScore(scores)
                } catch(connectException: ConnectException) {
                    _uiState.value = MapUiState.Error(1)
                } catch (_ : Throwable) {
                    _uiState.value = MapUiState.Error(2)
                }
            }
        }
    }

    fun clearResults() {
        recordingRepository.clearResults()
    }

    fun sendFiles(context: Context) {
        sendFiles(context, userId)
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

    data class CompleteScore(val scores: RouteEvaluation): MapUiState
}

sealed interface RecordingUiState {
    data object Default : RecordingUiState
    data object Loading : RecordingUiState
    data class Complete(val heights: List<Double>, val speeds: List<Double> ): RecordingUiState
}

/**
 * Extension function to convert a [GeoPoint] to a [Pair]<latitude, longitude>
 *
 * @return The resulting [Pair] of coordinates from the [GeoPoint]
 */
fun GeoPoint.toPair(): Pair<Double, Double> {
    return Pair(this.latitude, this.longitude)
}