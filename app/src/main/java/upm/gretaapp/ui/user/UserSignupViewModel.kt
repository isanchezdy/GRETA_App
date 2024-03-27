package upm.gretaapp.ui.user

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import upm.gretaapp.data.GretaRepository
import upm.gretaapp.data.PhoneSessionRepository
import upm.gretaapp.model.User
import java.net.ConnectException

class UserSignupViewModel(
    private val gretaRepository: GretaRepository,
    private val userSessionRepository: PhoneSessionRepository
): ViewModel() {

    var userUiState by mutableStateOf(UserUiState())
        private set

    fun updateUiState(userDetails: UserDetails) {
        userUiState =
            UserUiState(userDetails = userDetails, isEntryValid = validateInput(userDetails))
    }

    private fun validateInput(uiState: UserDetails = userUiState.userDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                    && birthday.isNotBlank() && drivingLicenseYear.isNotBlank()
                    && (drivingLicenseYear.toIntOrNull() != null && drivingLicenseYear.length == 4)
        }
    }

    fun saveUser() {
        if (validateInput()) {
            viewModelScope.launch {
                try{
                    userUiState = userUiState.copy(userState = UserState.Loading)
                    val user = gretaRepository.createUser(userUiState.userDetails.toUser())
                    userSessionRepository.saveUserPreference(user.userID!!)
                    userUiState = userUiState.copy(userState = UserState.Complete)
                } catch(connectException: ConnectException) {
                    userUiState = userUiState.copy(userState = UserState.Error(1))
                } catch (throwable: Throwable) {
                    userUiState = userUiState.copy(userState = UserState.Error(2))
                    Log.e("Error_signup", throwable.stackTraceToString())
                }
            }
        }
    }
}

data class UserUiState(
    val userDetails: UserDetails = UserDetails(),
    val isEntryValid: Boolean = false,
    val userState: UserState = UserState.Start
)

sealed interface UserState {
    data object Start: UserState
    data object Loading: UserState
    data object Complete: UserState
    data class Error(val code: Int): UserState
}

data class UserDetails(
    val id: Long? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val gender: Int = -1,
    val birthday: String = "",
    val drivingLicenseYear: String = ""
)

fun UserDetails.toUser(): User = User(
    userID = this.id,
    name = this.name,
    email = this.email,
    password = this.password,
    gender = when (this.gender) {
        0 -> {
            "Male"
        }
        1 -> {
            "Female"
        }
        else -> {
            "Other"
        }
    },
    birthDate = this.birthday + "T00:00:00",
    drivingLicenseYear = this.drivingLicenseYear
)

fun User.toUserDetails(): UserDetails = UserDetails(
    id = this.userID,
    name = name,
    email = email,
    password = password,
    gender = when (this.gender) {
        "Male" -> {
            0
        }
        "Female" -> {
            1
        }
        else -> {
            2
        }
    },
    birthday = this.birthDate.removeSuffix("T00:00:00"),
    drivingLicenseYear = this.drivingLicenseYear
)