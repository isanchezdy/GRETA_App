package upm.gretaapp.ui.route_history

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
import upm.gretaapp.model.UserRoute
import java.net.ConnectException

/**
 * ViewModel to retrieve all vehicles from user in the database.
 *
 * @param userSessionRepository Repository for obtaining the current user of the app
 * @param gretaRepository Repository for obtaining all user routes of the current user and show them
 */
class RouteHistoryViewModel(
    userSessionRepository: PhoneSessionRepository,
    private val gretaRepository: GretaRepository
) : ViewModel() {

    // Id of the current user
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            // The id of the current user is retrieved
            userSessionRepository.user.collectLatest {
                userId = it
                getUserRoutes()
            }
        }
    }

    /**
     * Variable for representing the current state of the ui, starting with a loading screen
     */
    var routesHistoryUiState: RoutesHistoryUiState by mutableStateOf(RoutesHistoryUiState.Loading)
        private set

    /**
     * Function to retrieve all the user routes of the current user to represent them in a list
     */
    fun getUserRoutes() {
        viewModelScope.launch {
            routesHistoryUiState = try {
                // The RoutesHistory are retrieved
                val routesHistory = gretaRepository.getRoutesHistoryUser(userId)
                // For each UserVehicle, the corresponding Vehicle is associated
                //
                val list: MutableList<UserRoute> = mutableListOf()
                for (route in routesHistory) {
                    list.add(route)
                }
                // The ui is updated with the results
                RoutesHistoryUiState.Success(list)

            } catch (connectException: ConnectException) {
                // A message is shown for a connection error
                RoutesHistoryUiState.Error(1)
            } catch (throwable: Throwable) {
                Log.e("Error_route_history", throwable.stackTraceToString())
                // Another message is shown for server errors of other type
                RoutesHistoryUiState.Error(2)
            }
        }
    }
}

/**
 * Ui State for UserVehicleListScreen
 */
sealed interface RoutesHistoryUiState {
    data class Success(val routeHistory: List<UserRoute>) : RoutesHistoryUiState
    data class Error(val code: Int) : RoutesHistoryUiState
    data object Loading : RoutesHistoryUiState
}
