package upm.gretaapp.ui.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.PhoneSessionRepository
import java.net.ConnectException

/**
 * [ViewModel] for managing the login process of the app
 *
 * @param gretaRepository Repository for obtaining the users of the app
 * @param userSessionRepository Repository for storing the user that logs in for the rest of the session
 */
class UserLoginViewModel(
    private val gretaRepository: GretaRepository,
    private val userSessionRepository: PhoneSessionRepository
): ViewModel() {
    // Variables that manage the state of the ui
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Start)
    val uiState = _uiState.asStateFlow()

    /**
     * Function to log in the app with the email and password
     *
     * @param email Email of the user
     * @param password Password of the user
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try{
                // A loading indicator is shown
                _uiState.value = LoginUiState.Loading
                // A user is retrieved using the email
                val user = gretaRepository.getUserByEmail(email)
                // If the password is the same, the process is complete
                if(user.password == password) {
                    _uiState.value = LoginUiState.Complete
                    userSessionRepository.saveUserPreference(user.userID!!)
                } else {
                    _uiState.value = LoginUiState.Error(2)
                }
            } catch(connectException: ConnectException) {
                // When the server cannot be reached, an error message is shown
                _uiState.value = LoginUiState.Error(1)
            } catch (throwable: Throwable) {
                _uiState.value = LoginUiState.Error(2)
                Log.e("Error_login", throwable.stackTraceToString())
            }
        }
    }
}

/**
 * Object that represents the state of the UI for the Login Screen (loading, error, complete)
 */
sealed interface LoginUiState {
    data object Start: LoginUiState
    data object Loading: LoginUiState
    data class Error(val code: Int): LoginUiState
    data object Complete: LoginUiState
}