package upm.gretaapp.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
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

/**
 * Function to encode a polyline to a [String]
 *
 * @param points List of points to encode
 * @param precision Parameter of the precision of the process
 * @return The [String] obtained from the process of encoding [points]
 */
fun encodePoly(points: List<Pair<Double, Double>>, precision: Int = 5): String {
    val encodedPoly = StringBuilder()
    val factor = 10.0.pow(precision.toDouble())

    var prevLat = 0
    var prevLng = 0

    for (point in points) {
        val lat = (point.first * factor).toInt()
        val lng = (point.second * factor).toInt()

        val dLat = lat - prevLat
        val dLng = lng - prevLng

        prevLat = lat
        prevLng = lng

        encodeCoordinate(dLat, encodedPoly)
        encodeCoordinate(dLng, encodedPoly)
    }

    return encodedPoly.toString()
}

/**
 * Function to store a point inside the encoded string
 *
 * @param value The value to encode
 * @param encodedPoly [StringBuilder] that contains the current encoded string
 */
private fun encodeCoordinate(value: Int, encodedPoly: StringBuilder) {
    var v = if (value < 0) (value shl 1).inv() else value shl 1
    while (v >= 0x20) {
        encodedPoly.append((((v and 0x1f) or 0x20) + 63).toChar())
        v = v shr 5
    }
    encodedPoly.append((v + 63).toChar())
}

/**
 * Function to retrieve results from a recording file
 *
 * @param context The [Context] used to read the file
 * @param filename The name of the file to read from
 * @return The lists of speeds, heights, times and coordinates obtained from the recording
 */
fun readFile(context: Context, filename: String): Triple<List<Double>, List<Double>, Pair<List<Double>,String>> {
    val state = Environment.getExternalStorageState()
    if (Environment.MEDIA_MOUNTED == state) {
        // Get the app-specific directory on external storage
        val dir = context.getExternalFilesDir(null)
        val file = File(dir, filename)

        // Reading the lines of the file
        return file.useLines {
            // List of parameters
            val speeds = mutableListOf<Double>()
            val heights = mutableListOf<Double>()
            val coordinates = mutableListOf<Pair<Double, Double>>()
            val timestamps = mutableListOf<Double>()

            // Parser for obtaining dates of the file
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSS", Locale.getDefault())
            // Previous registered date for comparing
            var previousDate: Date? = null

            // For each non-null line, the data is retrieved
            it.filter{ line ->
                !line.contains(
                    "timestamp,latitude,longitude,altitude,speed_km_h," +
                            "acceleration,ax,ay,az,numSatellites"
                )
            }.forEach { line ->
                val values = line.split(",")
                // If the line is complete, the data is stored
                if (values.size == 10) {
                    if (values[3] != "null") {
                        heights.add(values[3].toDouble())
                        speeds.add(values[4].toDouble())
                        coordinates.add(Pair(values[1].toDouble(), values[2].toDouble()))

                        // The date is retrieved
                        val date = dateFormat.parse(values[0])

                        // If there is no previous date, the list is initialized
                        if(previousDate == null) {
                            if(timestamps.isEmpty()) {
                                timestamps.add(1.0)
                            }
                        } else {
                            val time = (date!!.time - previousDate!!.time) / 1000.0

                            // If the route was paused, the real difference is ignored
                            if(time > 3.0) {
                                timestamps.add(timestamps.last() + 1.0)
                            } else {
                                timestamps.add(timestamps.last() + time)
                            }
                        }

                        // The previous date is registered
                        previousDate = date
                    }
                }
            }
            Log.d("read_file", speeds.toString())
            Log.d("read_file", heights.toString())
            Log.d("Debug_coordinates", coordinates.toString())
            Log.d("Debug_times", timestamps.toString())

            // The data is returned
            Triple(speeds,heights, Pair(timestamps, encodePoly(coordinates, precision = 6)))
        }
    }

    // Default values if the file is not found
    return Triple(emptyList(), emptyList(), Pair(emptyList(), ""))
}

/**
 * Function to change the state of a recording (started, paused, finished)
 *
 * @param context The [Context] used to write the file
 * @param filename The name of the file to write
 * @param state The new state of the recording
 */
fun writeState(context: Context, filename: String, state: String) {
    val storageState = Environment.getExternalStorageState()
    if (Environment.MEDIA_MOUNTED == storageState) {
        // Get the app-specific directory on external storage
        val dir = context.getExternalFilesDir(null)
        val file = File(dir, filename)
        val writer = PrintWriter(file)
        // The state is written
        writer.use {
            it.print(state)
        }
    }
}

/**
 * Function to send a zip with all the recording files through another app
 *
 * @param context The [Context] used to send the file
 * @param userId The id of the current user of the app
 */
fun sendFiles(context: Context, userId: Long) {
    // Get file directory files
    val filePath = context.getExternalFilesDir(null)
    val filePathString = filePath.toString()
    val fileDir = File(filePathString)
    var files = fileDir.list()

    // Compressed file to be created
    val zipFile = File(filePath, "$userId.zip")
    // Deletes zip file if already exists
    if (zipFile.exists()){
        zipFile.delete()
    }

    // Creates zip file when there is at least one file
    if (files != null) {
        // Removes all states from previous recordings
        for (file in files.filter { it.endsWith(".txt") }) {
            val f = File(filePath, file)
            if(f.exists()) {
                f.delete()
            }
        }

        // Removes zip from directory to prevent recursive zipping and other files
        files = files.filter {
            !it.endsWith(".zip") && !it.endsWith(".txt") && it != "osmdroid"
        }.toTypedArray()

        if (files.isNotEmpty()) {
            // Stream for writing compressed contents
            val zipOutputStream = ZipOutputStream(FileOutputStream(zipFile))
            zipOutputStream.flush()

            // Compresses each file from the folder
            for (file in files) {
                val zipEntry = ZipEntry(file)
                zipOutputStream.putNextEntry(zipEntry)

                // Reads bytes from the file and writes inside ZIP
                val inputStream = FileInputStream(File(filePath, file))
                val buffer = ByteArray(1024)
                var len = inputStream.read(buffer)
                while (len > 0) {
                    zipOutputStream.write(buffer, 0, len)
                    len = inputStream.read(buffer)
                }

                // Closes streams
                zipOutputStream.closeEntry()
                inputStream.close()
            }

            zipOutputStream.close()

            // If file exists, start the intent
            if (zipFile.exists()) {
                // Get URI from the selected file
                val contentUri = FileProvider.getUriForFile(
                    context, "upm.gretaapp.fileprovider",
                    zipFile
                )
                // Create the intent to send the file, with type "application/zip" and granting read uri
                // permission
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                // Start intent activity
                context.startActivity(intent)
            }
        }
    }
}

/**
 * Function to remove all recording files from the phone
 *
 * @param context The [Context] used to retrieve the files
 */
fun clearFiles(context: Context) {
    // Get file directory files
    val filePath = context.getExternalFilesDir(null)
    val filePathString = filePath.toString()
    val fileDir = File(filePathString)
    val files = fileDir.list()

    for(file in files!!) {
        File(filePath,file).delete()
    }
}

/**
 * Sets [overlay] as the head of the list of overlays
 *
 * @param overlay [Overlay] to set as head
 */
fun MapView.setOverlayAsHead(overlay: Overlay) {
    this.overlayManager.remove(overlay)
    this.overlayManager.add(overlay)
}

/**
 * Inserts a [Polyline] on the map, setting the current markers as the heads of the overlays list
 *
 * @param polyline [Polyline] to insert on the map
 */
fun MapView.insertPolyline(polyline: Polyline) {
    // The markers are found within the list
    val markerOverlay = this.overlayManager.find { it.javaClass == Marker::class.java }!!
    val myLocationNewOverlay = this.overlayManager.find {
        it.javaClass == MyLocationNewOverlay::class.java
    }!!

    // The polyline is inserted
    this.overlayManager.add(polyline)

    // The markers are set as heads
    this.setOverlayAsHead(markerOverlay)
    this.setOverlayAsHead(myLocationNewOverlay)
}