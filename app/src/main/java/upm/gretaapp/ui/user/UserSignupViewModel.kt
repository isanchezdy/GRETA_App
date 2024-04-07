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

/**
 * [ViewModel] for managing the signup process of the app
 *
 * @param gretaRepository Repository for creating users of the app
 * @param userSessionRepository Repository for storing the user that logs in for the rest of the session
 */
class UserSignupViewModel(
    private val gretaRepository: GretaRepository,
    private val userSessionRepository: PhoneSessionRepository
): ViewModel() {

    // Variable that manages the state of the ui
    var userUiState by mutableStateOf(UserUiState())
        private set

    /**
     * Function that updates the current information of the user to be created
     *
     * @param userDetails Details of the user to be created
     */
    fun updateUiState(userDetails: UserDetails) {
        userUiState =
            UserUiState(userDetails = userDetails, isEntryValid = validateInput(userDetails))
    }

    /**
     * Function to validate all the fields of the screen to sign up
     *
     * @param uiState Object that represents the state of the current user
     * @return If the user should be able to sign up in the app
     */
    private fun validateInput(uiState: UserDetails = userUiState.userDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                    && birthday.isNotBlank() && drivingLicenseYear.isNotBlank()
                    && (drivingLicenseYear.toIntOrNull() != null && drivingLicenseYear.length == 4)
        }
    }

    /**
     * Function to create a new user using the information of the ui state object
     */
    fun saveUser() {
        // If the user can be created
        if (validateInput()) {
            viewModelScope.launch {
                try{
                    // The screen is set to loading
                    userUiState = userUiState.copy(userState = UserState.Loading)
                    // A user is created from the information
                    val user = gretaRepository.createUser(userUiState.userDetails.toUser())
                    // The user is stored in the session
                    userSessionRepository.saveUserPreference(user.userID!!)
                    // The process is set as complete to go to the next screen
                    userUiState = userUiState.copy(userState = UserState.Complete)
                } catch(connectException: ConnectException) {
                    // If the server cannot be reached, an error message is shown
                    userUiState = userUiState.copy(userState = UserState.Error(1))
                } catch (throwable: Throwable) {
                    userUiState = userUiState.copy(userState = UserState.Error(2))
                    Log.e("Error_signup", throwable.stackTraceToString())
                }
            }
        }
    }
}

/**
 * Class that represents the state of the ui for the signup screen and its fields
 */
data class UserUiState(
    val userDetails: UserDetails = UserDetails(),
    val isEntryValid: Boolean = false,
    val userState: UserState = UserState.Start
)

/**
 * Represents the state of the signup screen (loading, error, complete)
 */
sealed interface UserState {
    data object Start: UserState
    data object Loading: UserState
    data object Complete: UserState
    data class Error(val code: Int): UserState
}

/**
 * Object that represents the details of a user being introduced into fields
 */
data class UserDetails(
    val id: Long? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val gender: Int = -1,
    val birthday: String = "",
    val drivingLicenseYear: String = ""
)

/**
 * Function to convert [UserDetails] into a [User] object
 */
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

/**
 * Function to convert [User] into a [UserDetails] object
 */
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