package upm.gretaapp.ui.review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.PhoneSessionRepository
import upm.gretaapp.model.AppReview
import upm.gretaapp.model.FeatureReview
import java.net.ConnectException

/**
 * [ViewModel] that manages the state of the UI of the Review Screen and its functions
 *
 * @param phoneSessionRepository Repository to obtain the current user that will send the review
 * @param gretaRepository Repository to send the information to the dedicated server
 */
class ReviewViewModel(phoneSessionRepository: PhoneSessionRepository,
                      private val gretaRepository: GretaRepository): ViewModel() {

    // Current user
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            // The current user is retrieved and updated
            phoneSessionRepository.user.collectLatest {
                userId = it
            }
        }
    }

    // Variables that manage the state of the UI for the screen
    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Default)
    val uiState = _uiState.asStateFlow()

    /**
     * Function to send a list of pairs score-label to the server
     *
     * @param scores List of scores classified by its corresponding label
     */
    fun sendReviews(scores: List<Pair<Int, String>>) {
        viewModelScope.launch {
            try {
                // The send button gets removed while it is loading
                _uiState.value = ReviewUiState.Loading

                // The global score is created
                val globalScore = gretaRepository.createAppReview(
                    AppReview(
                        userId = userId,
                        globalComments = scores.last().second,
                        globalScore = scores.last().first.toDouble()
                    )
                )

                // The other topics are enumerated
                val topics = listOf(
                    "ACCESABILITY", "RESPONSETIME", "CONFIGURABILITY", "USABILITY",
                    "TRUSTABILITY", "ROBUSTNESS", "UTILITY"
                )

                // For each topic, a FeatureReview object is created
                topics.forEachIndexed { index, topic ->
                    gretaRepository.createFeatureReview(
                        FeatureReview(
                            appReviewId = globalScore.appReviewId!!,
                            score = scores[index].first.toDouble(),
                            comments = scores[index].second,
                            topic = topic
                        )
                    )
                }
                // The ui is updated with a completion message
                _uiState.value = ReviewUiState.Complete
            } catch(connectException: ConnectException) {
                // If the app can't connect to the server
                _uiState.value = ReviewUiState.Error(1)
            } catch(throwable: Throwable) {
                Log.e("Error_review", throwable.stackTraceToString())
                _uiState.value = ReviewUiState.Error(2)
            }
        }
    }
}

/**
 * Interface that represents the different states of the screen
 */
sealed interface ReviewUiState {
    data object Default: ReviewUiState
    data object Loading: ReviewUiState
    data class Error(val code: Int): ReviewUiState
    data object Complete: ReviewUiState
}