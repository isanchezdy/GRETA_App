package upm.gretaapp.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlin.math.pow

/**
 * Function to redirect the app to Google Maps with a certain route
 *
 * @param context Context to create the [Intent]
 * @param coordinates Coordinates that define the route to open in Google Maps
 */
fun openMaps(context: Context, coordinates: List<Pair<Double,Double>>) {
    var url = "https://www.google.com/maps/dir"

    val slice = (coordinates.size / 10) or 1
    val size = if(coordinates.size >= 8) 8
    else coordinates.size - 2

    for (i in 0..size) {
        val cord = coordinates[i * slice]
        url += "/" + cord.first.toString() + "," + cord.second.toString()
    }

    val cord = coordinates.last()
    url += "/" + cord.first.toString() + "," + cord.second.toString()

    val intentUri = Uri.parse(url)
    val mapsIntent = Intent(Intent.ACTION_VIEW, intentUri)
    mapsIntent.setPackage("com.google.android.apps.maps")
    context.startActivity(mapsIntent)
}

/**
 * Function to decode a polyline from a [String]
 *
 * @param encoded String to get the polyline coordinates from
 * @param precision Parameter of the precision of the process
 * @return The coordinates obtained from the process of decoding [encoded], Pair<latitude,
 * longitude>
 */
fun decodePoly(encoded: String, precision: Int = 5): List<Pair<Double,Double>> {
    val poly = mutableListOf<Pair<Double, Double>>()
    var index = 0
    var lat = 0
    var lng = 0
    val factor = 10.0.pow(precision.toDouble())

    while (index < encoded.length) {
        // Decode latitude
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val decodedLat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += decodedLat

        // Decode longitude
        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val decodedLng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += decodedLng

        val latLng = Pair(lat / factor, lng / factor)
        poly.add(latLng)
    }

    return poly
}