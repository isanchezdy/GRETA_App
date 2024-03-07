package upm.gretaapp.ui.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.UserSessionRepository

class UserLoginViewModel(
    private val gretaRepository: GretaRepository,
    private val userSessionRepository: UserSessionRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Start)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try{
                _uiState.value = LoginUiState.Loading
                val user = gretaRepository.getUserByEmail(email)
                if(user.password == password) {
                    _uiState.value = LoginUiState.Complete
                    userSessionRepository.saveUserPreference(user.userID!!)
                } else {
                    _uiState.value = LoginUiState.Error
                }
            } catch (throwable: Throwable) {
                _uiState.value = LoginUiState.Error
                Log.e("Error_login", throwable.stackTraceToString())
            }
        }
    }
}

sealed interface LoginUiState {
    data object Start: LoginUiState
    data object Loading: LoginUiState
    data object Error: LoginUiState
    data object Complete: LoginUiState
}