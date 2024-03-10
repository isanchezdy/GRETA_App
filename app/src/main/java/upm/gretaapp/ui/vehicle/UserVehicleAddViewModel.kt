package upm.gretaapp.ui.vehicle

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
 */
class UserVehicleAddViewModel(private val gretaRepository: GretaRepository,
                              private val userSessionRepository: UserSessionRepository) : ViewModel() {
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            userSessionRepository.user.collectLatest {
                userId = it
            }
        }
    }

    /**
     * Holds current item ui state
     */
    private val _vehicleUiState = MutableStateFlow(emptyList<Vehicle>())
    val vehicleUiState = _vehicleUiState.asStateFlow()

    fun getVehicles(query: String) {
        viewModelScope.launch {
            try {
                _vehicleUiState.value = gretaRepository.getVehicles(query = query).filter {
                    it.vehicleID > 0
                }.map {
                    it.copy(name = it.name.replace("_-_", " - "))
                }
            } catch(_: Throwable) {

            }
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
            try{
                gretaRepository.createUserVehicle(userVehicle)
            } catch(_: Throwable) {}
        }
    }
}