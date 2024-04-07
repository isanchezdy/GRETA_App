package upm.gretaapp.ui.stats

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.PhoneSessionRepository
import upm.gretaapp.model.UserStats
import java.net.ConnectException

/**
 * ViewModel to retrieve all stats from user in the database.
 *
 * @param phoneSessionRepository Repository for obtaining the current user of the app
 * @param gretaRepository Repository for obtaining all stats of the current user and show them
 */
class StatsViewModel(
    phoneSessionRepository: PhoneSessionRepository,
    private val gretaRepository: GretaRepository
): ViewModel() {

    // Id of the current user
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            // The id of the current user is retrieved
            phoneSessionRepository.user.collectLatest {
                userId = it
                // The stats are retrieved for the current user
                getStats()
            }
        }
    }

    /**
     * Variable for representing the current state of the ui, starting with a loading screen
     */
    var statsUiState: StatsUiState by mutableStateOf(StatsUiState.Loading)
        private set

    /**
     * Function to retrieve all the stats of the current user to represent them
     */
    @OptIn(ExperimentalSerializationApi::class)
    private fun getStats() {
        viewModelScope.launch {
            statsUiState = try {
                // The stats are retrieved and the ui is updated
                val userStats = gretaRepository.getStatsUser(userId).last()
                StatsUiState.Success(userStats)
            } catch(missingFieldException: MissingFieldException) {
                // If the stats are blank
                StatsUiState.Success(null)
            } catch(connectException: ConnectException) {
                // A message is shown for a connection error
                StatsUiState.Error(1)
            } catch (throwable: Throwable) {
                Log.e("Error_stats", throwable.stackTraceToString())
                // Another message is shown for server errors of other type
                StatsUiState.Error(2)
            }
        }
    }
}

/**
 * Ui State for Stats Screen
 */
sealed interface StatsUiState {
    data class Success(val userStats: UserStats?): StatsUiState
    data object Loading: StatsUiState
    data class Error(val code: Int): StatsUiState
}