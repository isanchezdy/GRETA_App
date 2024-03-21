package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class that represents a User from the database
 */
@Serializable
data class User (
    @SerialName("BirthDate")
    val birthDate: String,

    @SerialName("DrivingLicenseYear")
    val drivingLicenseYear: String,

    @SerialName("Email")
    val email: String,

    @SerialName("Gender")
    val gender: String,

    @SerialName("Name")
    val name: String,

    @SerialName("Password")
    val password: String,

    @SerialName("UserID")
    val userID: Long? = null
)
