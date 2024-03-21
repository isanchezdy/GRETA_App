package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InputPerformedRoute(
    @SerialName("VehicleID")
    val vehicleId: Long,

    @SerialName("AdditionalMass")
    val additionalMass: Long,

    @SerialName("Speeds")
    val speeds: List<Double>,

    @SerialName("Times")
    val times: List<Double>,

    @SerialName("Heights")
    val heights: List<Double>,

    @SerialName("RoutePolyline")
    val routePolyline: String
)