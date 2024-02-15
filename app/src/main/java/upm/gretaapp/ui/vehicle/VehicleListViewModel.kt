package upm.gretaapp.ui.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import upm.gretaapp.model.Vehicle
import upm.gretaapp.data.VehiclesRepository

/**
 * ViewModel to retrieve all vehicles from user in the Room database.
 */
class VehicleListViewModel(vehiclesRepository: VehiclesRepository) : ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    val vehicleListUiState: StateFlow<VehicleListUiState> =
        vehiclesRepository.getAllVehiclesFromUserStream(0).map { VehicleListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = VehicleListUiState()
            )
}

/**
 * Ui State for HomeScreen
 */
data class VehicleListUiState(val vehicleList: List<Vehicle> = listOf())