package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRoute(
    // TODO also fix this
    @SerialName("ID")
    val id: Long,

    @SerialName("UserID")
    val userId: Long,

    @SerialName("UserVehicleID")
    val userVehicleId: String,

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
)