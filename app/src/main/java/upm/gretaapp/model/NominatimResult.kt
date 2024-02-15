package upm.gretaapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class to retrieve search results from Nominatim and place then on the map
 */
@Serializable
data class NominatimResult(
    @SerialName(value = "place_id")
    val placeId: Long,
    val licence: String,
    @SerialName(value = "osm_type")
    val osmType: String,
    @SerialName(value = "osm_id")
    val osmId: Long,
    val lat: Double,
    val lon: Double,
    val category: String,
    val type: String,
    @SerialName(value = "place_rank")
    val placeRank: Long,
    val importance: Double,
    @SerialName(value = "addresstype")
    val addressType: String,
    val name: String,
    @SerialName(value = "display_name")
    val displayName: String,
    @SerialName(value = "boundingbox")
    val boundingBox: List<Double>
)