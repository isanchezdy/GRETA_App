package upm.gretaapp.ui.vehicle

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.UserSessionRepository
import upm.gretaapp.model.Vehicle
import upm.gretaapp.model.UserVehicle

/**
 * ViewModel to retrieve all vehicles from user in the database.
 */
class VehicleListViewModel( userSessionRepository: UserSessionRepository,
    private val gretaRepository: GretaRepository
) : ViewModel() {
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            userSessionRepository.user.collectLatest {
                userId = it
                getVehicles()
            }
        }
    }

    var userVehicleListUiState: UserVehicleListUiState by mutableStateOf(UserVehicleListUiState.Loading)
        private set

    fun getVehicles() {
        viewModelScope.launch {
            userVehicleListUiState = try {
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
                UserVehicleListUiState.Success(list)
            } catch (throwable: Throwable) {
                Log.e("Error_vehicles", throwable.stackTraceToString())
                UserVehicleListUiState.Error
            }
        }
    }

    fun setFavourite(userVehicle: UserVehicle) {
        viewModelScope.launch {
            try {
                if (userVehicleListUiState is UserVehicleListUiState.Success) {
                    if (userVehicle.isFav == 1) {
                        gretaRepository.updateUserVehicle(userVehicle.copy(isFav = 0))
                    } else {
                        val previousFavourite = (userVehicleListUiState as UserVehicleListUiState.Success)
                            .vehicleList.find {
                                it.first.isFav == 1
                            }?.first

                        if (previousFavourite != null) {
                            gretaRepository.updateUserVehicle(previousFavourite.copy(isFav = 0))
                        }
                        gretaRepository.updateUserVehicle(userVehicle.copy(isFav = 1))
                    }
                    getVehicles()
                }
            } catch(_ : Throwable) {
            }
        }
    }

    fun deleteVehicle(id: Long) {
        viewModelScope.launch {
            try {
                if (userVehicleListUiState is UserVehicleListUiState.Success) {
                    userVehicleListUiState =
                        UserVehicleListUiState.Success((userVehicleListUiState as UserVehicleListUiState.Success)
                            .vehicleList.filter { it.first.id != id })
                    gretaRepository.deleteUserVehicle(id)
                }
            } catch (throwable: Throwable) {
                Log.e("Error_vehicles", throwable.stackTraceToString())
                UserVehicleListUiState.Error
            }
        }
    }
}

/**
 * Ui State for HomeScreen
 */
sealed interface UserVehicleListUiState {
    data class Success(val vehicleList: List<Pair<UserVehicle, Vehicle>>) : UserVehicleListUiState
    data object Error : UserVehicleListUiState
    data object Loading : UserVehicleListUiState
}
