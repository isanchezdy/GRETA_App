package upm.gretaapp.model

import androidx.compose.ui.res.stringResource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import upm.gretaapp.R


/**
 * Entity data class represents a single Vehicle from the database
 */
@Serializable
data class Vehicle (
    @SerialName("A")
    val a: Double,

    @SerialName("B")
    val b: Double,

    @SerialName("C")
    val c: Double,

    @SerialName("ImageUrl")
    val imageURL: String,

    @SerialName("LitersConversion")
    val litersConversion: Double,

    @SerialName("MotorType")
    val motorType: String,

    @SerialName("Name")
    val name: String,

    @SerialName("PMaxKw")
    val pMaxKw: Double,

    @SerialName("ResistanceFactor")
    val resistanceFactor: Double,

    @SerialName("UnladenVehMass")
    val unladenVehMass: Double,

    @SerialName("Url")
    val url: String,

    @SerialName("VehicleID")
    val vehicleID: Long
)

fun Vehicle.getMotorType(): Int {
    return when(this.motorType) {
        "DIESEL" -> R.string.diesel
        "GASOLINE" -> R.string.gasoline
        "HYBRID" -> R.string.hybrid
        "ELECTRIC" -> R.string.electric
        else -> R.string.gasoline
    }
}