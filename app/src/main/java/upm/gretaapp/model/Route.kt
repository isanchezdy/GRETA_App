package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import upm.gretaapp.ui.map.decodePoly


@Serializable
data class Route (
    @SerialName("Distance")
    val distance: Double,

    @SerialName("EnergyConsumption")
    val energyConsumption: Double,

    @SerialName("Route")
    val route: String,

    @SerialName("Time")
    val time: Double
): java.io.Serializable

/**
 * Property that provides direct access to the route coordinates
 */
val Route.processedRoute: List<Pair<Double,Double>>
    get() = decodePoly(encoded = this.route, precision = 6)