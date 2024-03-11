package upm.gretaapp.ui.review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.UserSessionRepository
import upm.gretaapp.model.AppReview
import upm.gretaapp.model.FeatureReview
import java.net.ConnectException

class ReviewViewModel(userSessionRepository: UserSessionRepository,
    private val gretaRepository: GretaRepository): ViewModel() {
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            userSessionRepository.user.collectLatest {
                userId = it
            }
        }
    }

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Default)
    val uiState = _uiState.asStateFlow()

    fun sendReviews(scores: List<Pair<Int, String>>) {
        viewModelScope.launch {
            try {
                _uiState.value = ReviewUiState.Loading
                val globalScore = gretaRepository.createAppReview(
                    AppReview(
                        userId = userId,
                        globalComments = scores.last().second,
                        globalScore = scores.last().first.toDouble()
                    )
                )

                val topics = listOf(
                    "ACCESABILITY", "RESPONSETIME", "CONFIGURABILITY", "USABILITY",
                    "TRUSTABILITY", "ROBUSTNESS", "UTILITY"
                )

                topics.forEachIndexed { index, topic ->
                    gretaRepository.createFeatureReview(
                        FeatureReview(
                            appReviewId = globalScore.id!!,
                            score = scores[index].first.toDouble(),
                            comments = scores[index].second,
                            topic = topic
                        )
                    )
                }
                _uiState.value = ReviewUiState.Complete
            } catch(connectException: ConnectException) {
                _uiState.value = ReviewUiState.Error(1)
            } catch(throwable: Throwable) {
                Log.e("Error_review", throwable.stackTraceToString())
                _uiState.value = ReviewUiState.Error(2)
            }
        }
    }
}

sealed interface ReviewUiState {
    data object Default: ReviewUiState
    data object Loading: ReviewUiState
    data class Error(val code: Int): ReviewUiState
    data object Complete: ReviewUiState
}