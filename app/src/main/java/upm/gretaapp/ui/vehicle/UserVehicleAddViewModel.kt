package upm.gretaapp.ui.vehicle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.UserSessionRepository
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle

/**
 * ViewModel to validate and insert vehicles in the database.
 */
class UserVehicleAddViewModel(private val gretaRepository: GretaRepository,
                              private val userSessionRepository: UserSessionRepository) : ViewModel() {
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            userSessionRepository.user.collectLatest {
                userId = it
            }
            getVehicles()
        }
    }

    /**
     * Holds current item ui state
     */
    var vehicleUiState by mutableStateOf(emptyList<Vehicle>())
        private set

    private suspend fun getVehicles() {
        vehicleUiState = gretaRepository.getVehicles().filter {
            it.vehicleID > 0
        }.map{
            it.copy(name = it.name.replace("_-_", " - "))
        }
    }

    fun saveVehicle(vehicleId: Long, age: Long?, kmTravelled: Long?) {
        val userVehicle = UserVehicle(
            userId = userId,
            vehicleId = vehicleId,
            age = age,
            kmUsed = kmTravelled,
            isFav = 0
        )
        viewModelScope.launch {
            gretaRepository.createUserVehicle(userVehicle)
        }
    }
}