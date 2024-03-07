package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserVehicle(
    @SerialName("ID")
    val id: Long? = null,

    @SerialName("UserID")
    val userId: Long,

    @SerialName("VehicleID")
    val vehicleId: Long,

    @SerialName("Age")
    val age: Long?,

    @SerialName("KmUsed")
    val kmUsed: Long?,

    @SerialName("IsFav")
    val isFav: Int
)