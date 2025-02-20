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
import upm.gretaapp.data.PhoneSessionRepository
import upm.gretaapp.model.Vehicle
import upm.gretaapp.model.UserVehicle
import java.net.ConnectException

/**
 * ViewModel to retrieve all vehicles from user in the database.
 *
 * @param phoneSessionRepository Repository for obtaining the current user of the app
 * @param gretaRepository Repository for obtaining all vehicles of the current user and show them
 */
class UserVehicleListViewModel(
    phoneSessionRepository: PhoneSessionRepository,
    private val gretaRepository: GretaRepository
) : ViewModel() {

    // Id of the current user
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            // The id of the current user is retrieved
            phoneSessionRepository.user.collectLatest {
                userId = it
                getVehicles()
            }
        }
    }

    /**
     * Variable for representing the current state of the ui, starting with a loading screen
      */
    var userVehicleListUiState: UserVehicleListUiState by mutableStateOf(UserVehicleListUiState.Loading)
        private set

    /**
     * Function to retrieve all the vehicles of the current user to represent them in a list
     */
    fun getVehicles() {
        viewModelScope.launch {
            userVehicleListUiState = try {
                // The UserVehicles are retrieved
                val userVehicles = gretaRepository.getUserVehicles(userId)
                // The userRoutes
                val userRoutes = gretaRepository.getRoutesHistoryUser(userId)
                // For each UserVehicle, the corresponding Vehicle is associated
                val list: MutableList<Triple<UserVehicle, Vehicle, Boolean>> = mutableListOf()
                for (userVehicle in userVehicles) {
                    // For each vehicle, its model is retrieved and if it can be deleted
                    list.add(
                        Triple(
                            userVehicle,
                            gretaRepository.getVehicle(userVehicle.vehicleId),
                            // The user vehicle can be deleted if it not appears in user routes
                            userRoutes.none { it.userVehicleId == userVehicle.id }
                        )
                    )
                }
                // The ui is updated with the results
                UserVehicleListUiState.Success(list)

            } catch(connectException: ConnectException) {
                // A message is shown for a connection error
                UserVehicleListUiState.Error(1)
            } catch (throwable: Throwable) {
                Log.e("Error_vehicles", throwable.stackTraceToString())
                // Another message is shown for server errors of other type
                UserVehicleListUiState.Error(2)
            }
        }
    }

    /**
     * Function to set a [UserVehicle] as the favourite of its list
     *
     * @param userVehicle The [UserVehicle] that the user selects as its new favourite
     */
    fun setFavourite(userVehicle: UserVehicle) {
        viewModelScope.launch {
            try {
                // If all the vehicles of the user have loaded successfully
                if (userVehicleListUiState is UserVehicleListUiState.Success) {
                    // The button removes the favourite if it was already set
                    if (userVehicle.isFav == 1) {
                        gretaRepository.updateUserVehicle(userVehicle.copy(isFav = 0))
                    } else {
                        // The previous favourite is retrieved to remove its favourite property
                        val previousFavourite = (userVehicleListUiState as UserVehicleListUiState.Success)
                            .vehicleList.find {
                                it.first.isFav == 1
                            }?.first

                        // If there is a previous favourite, its favourite flag is removed
                        if (previousFavourite != null) {
                            gretaRepository.updateUserVehicle(previousFavourite.copy(isFav = 0))
                        }
                        // The new favourite vehicle is updated
                        gretaRepository.updateUserVehicle(userVehicle.copy(isFav = 1))
                    }
                    // The ui refreshes to reflect changes
                    getVehicles()
                }
            } catch(_ : Throwable) {
            }
        }
    }

    /**
     * Function to delete a [UserVehicle] from the list of the current user
     *
     * @param id The identifier of the [UserVehicle] to remove
     */
    fun deleteVehicle(id: Long) {
        viewModelScope.launch {
            try {
                // If the vehicles from the user have been retrieved
                if (userVehicleListUiState is UserVehicleListUiState.Success) {
                    // The vehicle is removed from the view
                    userVehicleListUiState =
                        UserVehicleListUiState.Success((userVehicleListUiState as UserVehicleListUiState.Success)
                            .vehicleList.filter { it.first.id != id })
                    // The vehicle is removed from the database
                    gretaRepository.deleteUserVehicle(id)
                }
            } catch(connectException: ConnectException) {
                // If the server can't be reached, a message appears
                UserVehicleListUiState.Error(1)
            } catch (throwable: Throwable) {
                Log.e("Error_vehicles", throwable.stackTraceToString())
                UserVehicleListUiState.Error(2)
            }
        }
    }
}

/**
 * Ui State for UserVehicleListScreen
 */
sealed interface UserVehicleListUiState {
    data class Success(val vehicleList: List<Triple<UserVehicle, Vehicle, Boolean>>) : UserVehicleListUiState
    data class Error(val code: Int) : UserVehicleListUiState
    data object Loading : UserVehicleListUiState
}
