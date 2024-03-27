package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerformedRouteMetrics (
    @SerialName("PerformedRouteConsumption")
    val performedRouteConsumption: Double,

    @SerialName("PerformedRouteTime")
    val performedRouteTime: Double,

    @SerialName("PerformedRouteDistance")
    val performedRouteDistance: Double,

    @SerialName("PerformedRouteEstimatedConsumption")
    val performedRouteEstimatedConsumption: Double,

    @SerialName("PerformedRouteEstimatedTime")
    val performedRouteEstimatedTime: Double,

    @SerialName("PerformedRouteEstimatedDistance")
    val performedRouteEstimatedDistance: Double,

    @SerialName("NumStopsKm")
    val numStopsKm: Int,

    @SerialName("SpeedVariationNum")
    val speedVariationNum: Int,

    @SerialName("DrivingAggressiveness")
    val drivingAggressiveness: Int
)