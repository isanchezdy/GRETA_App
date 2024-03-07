package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteEvaluation (
    @SerialName("energy_consumption")
    val energyConsumption: Double,

    val distance: Double,

    val time: Double,

    @SerialName("num_stops_per_km")
    val numStopsPerKm: Int,

    @SerialName("acceleration_lower_threshold")
    val accelerationLowerThreshold: Int,

    @SerialName("acceleration_greater_threshold")
    val accelerationGreaterThreshold: Int,
)