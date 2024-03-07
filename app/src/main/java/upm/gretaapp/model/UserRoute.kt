package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRoute(
    @SerialName("ID")
    val id: Long,

    @SerialName("UserID")
    val userId: Long,

    @SerialName("UserVehicleID")
    val userVehicleId: Long,

    @SerialName("AdditionalMass")
    val additionalMass: Long,

    @SerialName("Source")
    val source: String,

    @SerialName("Destination")
    val destination: String,

    @SerialName("RoutePolyline")
    val routePolyline: String,

    @SerialName("RouteType")
    val routeType: String,

    @SerialName("EstimatedConsumption")
    val estimatedConsumption: Double,

    @SerialName("EstimatedTimeSeconds")
    val estimatedTimeSeconds: Long,

    @SerialName("EstimatedDistanceMeters")
    val estimatedDistanceMeters: Long,

    @SerialName("RecordDate")
    val recordDate: String,

    @SerialName("ActualConsumption")
    val actualConsumption: Double,

    @SerialName("ActualTimeSeconds")
    val actualTimeSeconds: Long,

    @SerialName("ActualDistanceMeters")
    val actualDistanceMeters: Long
)