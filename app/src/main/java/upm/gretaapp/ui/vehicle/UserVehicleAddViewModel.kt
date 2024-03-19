package upm.gretaapp.ui.vehicle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.UserSessionRepository
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle

/**
 * ViewModel to validate and insert vehicles in the database.
 *
 * @param gretaRepository Repository to retrieve results from the dedicated server
 * @param userSessionRepository Repository to retrieve the current user of the app
 */
class UserVehicleAddViewModel(private val gretaRepository: GretaRepository,
                              private val userSessionRepository: UserSessionRepository) : ViewModel() {

    // Id of the current user
    private var userId: Long = 0
    // Number of vehicles of the current user
    private var userVehiclesCount: Int = 0

    init {
        viewModelScope.launch {
            // The current user and its vehicles are retrieved
            userSessionRepository.user.collectLatest {
                userId = it
                userVehiclesCount = try {
                    gretaRepository.getUserVehicles(userId).size
                } catch (_ : Throwable) {
                    0
                }
            }
        }
    }

    /**
     * Holds current vehicle ui state
     */
    private val _vehicleUiState = MutableStateFlow(emptyList<Vehicle>())
    val vehicleUiState = _vehicleUiState.asStateFlow()

    /**
     * Retrieves the vehicles that match a given query and updates the ui
     *
     * @param query Query to find vehicles with a similar name
     */
    fun getVehicles(query: String) {
        viewModelScope.launch {
            try {
                // Retrieves all the vehicles that are not default models
                _vehicleUiState.value = gretaRepository.getVehicles(query = query).filter {
                    it.vehicleID > 0
                }.map {
                    // The name is adjusted to look better for the ui
                    it.copy(name = it.name.replace("_-_", " - "))
                }
            } catch(throwable: Throwable) {
                Log.e("Error_add_vehicles", throwable.stackTraceToString())
            }
        }
    }

    /**
     *
     */
    fun saveVehicle(vehicleId: Long, age: Long?, kmTravelled: Long?) {
        val userVehicle = UserVehicle(
            userId = userId,
            vehicleId = vehicleId,
            age = age,
            kmUsed = kmTravelled,
            isFav = if(userVehiclesCount > 0) {
                0
            } else {
                1
            }
        )
        viewModelScope.launch {
            try{
                gretaRepository.createUserVehicle(userVehicle)
            } catch(throwable: Throwable) {
                Log.e("Error_add_vehicles", throwable.stackTraceToString())
            }
        }
    }
}