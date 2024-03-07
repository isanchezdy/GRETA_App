package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import upm.gretaapp.ui.map.decodePoly


@Serializable
data class Route (
    val distance: Double,

    @SerialName("energy_consumption")
    val energyConsumption: Double,

    @SerialName("route")
    val route: String,

    val time: Double
)

val Route.processedRoute: List<Pair<Double,Double>>
    get() = decodePoly(encoded = this.route, precision = 6)