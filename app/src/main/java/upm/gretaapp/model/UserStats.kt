package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStats (
    @SerialName("ConsumptionSaving")
    val consumptionSaving: Double,

    @SerialName("DriveRating")
    val driveRating: Double,

    @SerialName("EcoDistance")
    val ecoDistance: Double,

    @SerialName("EcoRoutesNum")
    val ecoRoutesNum: Long,

    @SerialName("EcoTime")
    val ecoTime: Double,

    @SerialName("UserID")
    val userID: Long
)