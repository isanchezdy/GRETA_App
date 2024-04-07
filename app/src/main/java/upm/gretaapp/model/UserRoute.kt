package upm.gretaapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UserRoute(
    @SerialName("ID")
    val id: Long? = null,

    @SerialName("UserID")
    val userId: Long,

    @SerialName("UserVehicleID")
    val userVehicleId: Long,

    @SerialName("AdditionalMass")
    val additionalMass: Long,

    @SerialName("SourceCoords")
    val sourceCoords: String,

    @SerialName("DestinationCoords")
    val destinationCoords: String,

    @SerialName("RecordDate")
    val recordDate: String,

    @SerialName("SelectedRoutePolyline")
    val selectedRoutePolyline: String,

    @SerialName("SelectedRouteType")
    val selectedRouteType: String,

    @SerialName("SelectedRouteConsumption")
    val selectedRouteConsumption: Double,

    @SerialName("SelectedRouteTime")
    val selectedRouteTime: Long,

    @SerialName("SelectedRouteDistance")
    val selectedRouteDistance: Long,

    @SerialName("PerformedRoutePolyline")
    val performedRoutePolyline: String,

    @SerialName("PerformedRouteConsumption")
    val performedRouteConsumption: Double,

    @SerialName("PerformedRouteTime")
    val performedRouteTime: Long,

    @SerialName("PerformedRouteDistance")
    val performedRouteDistance: Long,

    @SerialName("PerformedRouteEstimatedConsumption")
    val performedRouteEstimatedConsumption: Double,

    @SerialName("PerformedRouteEstimatedTime")
    val performedRouteEstimatedTime: Long,

    @SerialName("PerformedRouteEstimatedDistance")
    val performedRouteEstimatedDistance: Long,

    @SerialName("NumStopsKm")
    val numStopsKm: Int,

    @SerialName("SpeedVariationNum")
    val speedVariationNum: Int,

    @SerialName("DrivingAggressiveness")
    val drivingAggressiveness: Int
): Parcelable

/**
 * Function that fills fields from the object using a [Route] instance
 *
 * @param route The route from which the values are extracted
 * @return The [UserRoute] with the corresponding values replaced
 */
fun UserRoute.fillFromRoute(route: Route): UserRoute {

    return this.copy(
        selectedRoutePolyline = route.route,
        selectedRouteConsumption = route.energyConsumption,
        selectedRouteDistance = route.distance.toLong(),
        selectedRouteTime = route.time.toLong(),
    )
}

/**
 * Function that fills fields from the object using a [PerformedRouteMetrics] instance
 *
 * @param performedRouteMetrics The [PerformedRouteMetrics] from which the values are extracted
 * @return The [UserRoute] with the corresponding values replaced
 */
fun UserRoute.fillFromPerformedMetrics(performedRouteMetrics: PerformedRouteMetrics): UserRoute {
    return this.copy(
        performedRouteConsumption = performedRouteMetrics.performedRouteConsumption,
        performedRouteTime = performedRouteMetrics.performedRouteTime.toLong(),
        performedRouteDistance = performedRouteMetrics.performedRouteDistance.toLong(),
        performedRouteEstimatedConsumption = performedRouteMetrics.performedRouteEstimatedConsumption,
        performedRouteEstimatedTime = performedRouteMetrics.performedRouteEstimatedTime.toLong(),
        performedRouteEstimatedDistance = performedRouteMetrics.performedRouteEstimatedDistance.toLong(),
        numStopsKm = performedRouteMetrics.numStopsKm,
        speedVariationNum = performedRouteMetrics.speedVariationNum,
        drivingAggressiveness = performedRouteMetrics.drivingAggressiveness
    )
}
