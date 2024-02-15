package upm.gretaapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import upm.gretaapp.data.NominatimRepository
import upm.gretaapp.data.RecordingRepository
import upm.gretaapp.model.NominatimResult

/**
 * [ViewModel] that manages the current UI state of the Map Screen (search a destination, a route,
 * showing statistics, etc...)
 *
 * @param nominatimRepository Repository class to retrieve destinations from a text query and place
 *  them on the map
 *  @param recordingRepository Repository class to start recording a route when selected
 */
class MapViewModel(private val nominatimRepository: NominatimRepository,
    private val recordingRepository: RecordingRepository): ViewModel() {

    // Variables that manage the current state of the ui, showing a read-only value to the screen
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Start)
    val uiState = _uiState.asStateFlow()

    // Search results based on the user query
    private val _searchResults: MutableStateFlow<List<NominatimResult>> =
        MutableStateFlow(emptyList())
    val searchResults = _searchResults.asStateFlow()

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
    }

    /**
     * Function to start recording a route
     */
    fun startRecording(destination: GeoPoint) {
        recordingRepository.recordRoute(destination)
    }

}

/**
 * Interface that represents the different states of the screen
 */
sealed interface MapUiState {
    data object Start: MapUiState
    data object Loading: MapUiState
    data object Error: MapUiState
    data class Complete(val routes: List</*Route*/ Int>): MapUiState
}

/**
 * Extension function to convert a [GeoPoint] to a [Pair]<latitude, longitude>
 *
 * @return The resulting [Pair] of coordinates from the [GeoPoint]
 */
fun GeoPoint.toPair(): Pair<Double, Double> {
    return Pair(this.latitude, this.longitude)
}