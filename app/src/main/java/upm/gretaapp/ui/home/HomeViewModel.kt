package upm.gretaapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import upm.gretaapp.data.UserSessionRepository

class HomeViewModel(private val userSessionRepository: UserSessionRepository): ViewModel() {

    private var userId: Long = -1

    init {
        viewModelScope.launch {
            userSessionRepository.user.collectLatest {
                userId = it
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userSessionRepository.logout()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return userId != (-1).toLong()
    }
}