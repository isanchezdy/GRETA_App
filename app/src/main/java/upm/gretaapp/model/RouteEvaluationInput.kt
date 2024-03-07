package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteEvaluationInput(
    @SerialName("user_id")
    val userId: Long,
    @SerialName("vehicle_id")
    val vehicleId: Long,
    @SerialName("additional_mass")
    val additionalMass: Long,

    val speeds: List<Double>,
    val times: List<Double>,
    val heights: List<Double>
)