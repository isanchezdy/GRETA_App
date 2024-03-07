package upm.gretaapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import upm.gretaapp.data.UserSessionRepository

class HomeViewModel(private val userSessionRepository: UserSessionRepository): ViewModel() {
    fun logout() {
        viewModelScope.launch {
            userSessionRepository.logout()
        }
    }
}