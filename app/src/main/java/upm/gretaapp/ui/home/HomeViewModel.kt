package upm.gretaapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import upm.gretaapp.data.PhoneSessionRepository

/**
 * [ViewModel] that manages the logout and skip of the screen if necessary
 *
 * @param userSessionRepository Repository to manage the session of the app
 */
class HomeViewModel(private val userSessionRepository: PhoneSessionRepository): ViewModel() {

    private var userId: Long = -1

    /**
     * The current user is collected
     */
    init {
        viewModelScope.launch {
            userSessionRepository.user.collectLatest {
                userId = it
            }
        }
    }

    /**
     * Function to remove current user from the session
     */
    fun logout() {
        viewModelScope.launch {
            userSessionRepository.logout()
        }
    }

    /**
     * Function to check if there is a user logged in the app
     */
    fun isUserLoggedIn(): Boolean {
        return userId != (-1).toLong()
    }
}