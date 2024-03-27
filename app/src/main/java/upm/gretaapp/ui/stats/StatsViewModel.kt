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
                getStats()
            }
        }
    }

    var statsUiState: StatsUiState by mutableStateOf(StatsUiState.Loading)
        private set

    @OptIn(ExperimentalSerializationApi::class)
    private fun getStats() {
        viewModelScope.launch {
            statsUiState = try {
                val userStats = gretaRepository.getStatsUser(userId).last()
                StatsUiState.Success(userStats)
            } catch(missingFieldException: MissingFieldException) {
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

sealed interface StatsUiState {
    data class Success(val userStats: UserStats?): StatsUiState
    data object Loading: StatsUiState
    data class Error(val code: Int): StatsUiState
}