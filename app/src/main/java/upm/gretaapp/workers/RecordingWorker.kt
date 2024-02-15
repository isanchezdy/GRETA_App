package upm.gretaapp.workers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import upm.gretaapp.DELAY_TIME_MICRO
import upm.gretaapp.DELAY_TIME_MILLIS
import upm.gretaapp.KEY_DESTINATION_LAT
import upm.gretaapp.KEY_DESTINATION_LON
import upm.gretaapp.KEY_FILENAME
import upm.gretaapp.MainActivity
import upm.gretaapp.TIMER_MILLIS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt


private const val TAG = "BlurWorker"

/**
 * Data class that represents the data recorded during a second of the service
 */
data class Recording(
    val timestamp: String,
    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,
    val speed: Double,
    val linearAcceleration: Float?,
    val lax: Float?,
    val lay: Float?,
    val laz: Float?,
    val numSatellites: Int?,
) {
    override fun toString(): String {
        return "${this.timestamp},${this.latitude}," +
                "${this.longitude},${this.altitude}," +
                "${this.speed},${this.linearAcceleration}," +
                "${this.lax},${this.lay},${this.laz}," +
                "${this.numSatellites}\n"
    }
}

/**
 * Worker class to record data during a route to estimate consumption of the vehicle
 */
class RecordingWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    // Values to retrieve data from the phone sensors
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorEventListener: SensorEventListener
    private lateinit var linearAccelerometer: Sensor

    // Instance of a recording to keep updating its data each second
    private var recording = Recording("",null,null,null,0.0,
        null, null, null, null, null)

    // Values to retrieve the current location of the phone
    private var location = mutableStateOf<Location?>(null)
    private val fusedLocationClient = LocationServices
        .getFusedLocationProviderClient(applicationContext)

    override suspend fun doWork(): Result {

        return withContext(Dispatchers.IO) {
            // Input values are retrieved such as the destination or the filename
            val destinationLat = inputData.getDouble(KEY_DESTINATION_LAT, 0.0)
            val destinationLon = inputData.getDouble(KEY_DESTINATION_LON, 0.0)
            val filename = inputData.getString(KEY_FILENAME)!! + ".csv"

            // A timer is started to stop service in case of need
            var timer = TIMER_MILLIS

            return@withContext try{
                // Start file with the csv header
                val destination = Location("").apply {
                    this.latitude = destinationLat
                    this.longitude = destinationLon
                }
                writeCsvHeader(context = applicationContext, filename)

                // Initialize all sensors
                initializeSensors()

                // Start timer to stop in case of error
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                while(timer > 0) {

                    // GPS position permission is checked
                    if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        break
                    }

                    // Location and speed parameters are updated
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener {
                            val speed = when {
                                // Retrieve speed if available
                                it != null && it.hasSpeed() -> it.speed.toDouble()
                                // If there is previous location info calculate it
                                it != null && location.value != null -> {
                                    // Convert milliseconds to seconds
                                    val elapsedTimeInSeconds =
                                        (it.time - (location.value?.time ?: 0)) / 1000
                                    // Calculate distance in meters from previous to current location
                                    val distanceInMeters = location.value?.distanceTo(it) ?: 0.0f
                                    // Parse from m/s to km/h
                                    (distanceInMeters / elapsedTimeInSeconds) * 3.6
                                }
                                // Otherwise set default to 0.0
                                else -> 0.0
                            }
                            // Update previous location with new location info
                            location.value = it
                            // Store speed
                            recording = recording.copy(speed = speed)
                        }

                    // The recording is updated
                    recording = recording.copy(latitude = location.value?.latitude,
                        longitude = location.value?.longitude,
                        altitude = location.value?.altitude,
                        timestamp = dateFormat.format(Date()))

                    writeRecordingToCsv(applicationContext, recording, filename)

                    // Exit loop when destination is reached
                    if((recording.latitude != null) and (recording.longitude != null)) {
                        val dist = destination.distanceTo(location.value!!)
                        if (dist <= 50) {
                            break
                        }
                    }

                    // Decrement timer
                    delay(DELAY_TIME_MILLIS.toLong())
                    timer -= DELAY_TIME_MILLIS
                }
                // When the loop ends, remove all sensors and restart app
                closeSensors()

                // Define notification intent
                val notificationIntent = Intent(applicationContext, MainActivity::class.java)
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(notificationIntent)

                // Return success
                Result.success()
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    "Error",
                    throwable
                )
                Result.failure()
            }
        }
    }

    /**
     * Initialize all the used sensors
     */
    private fun initializeSensors() {
        // Get sensor manager from system
        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Get sensors for linear accelerometer
        linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!

        // Create sensor event listener
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                    // Retrieve accelerometer info from sensor -> Linear = Without gravity
                    recording = recording.copy(
                        lax = event.values[0], // acceleration on X-axis
                        lay = event.values[1], // acceleration on Y-axis
                        laz = event.values[2], // acceleration on Z-axis
                        // Calculate the magnitude of the acceleration with the Pythagorean theorem
                        linearAcceleration = sqrt(
                            event.values[0] * event.values[0] + event.values[1] *
                                    event.values[1] + event.values[2] * event.values[2]
                        )
                    )
                }
            }

            override fun onAccuracyChanged(event: Sensor?, p1: Int) {}

        }

        // Register a listener for linear accelerometer events each second
        sensorManager.registerListener(
            sensorEventListener, linearAccelerometer, DELAY_TIME_MICRO
        )
    }

    /**
     * Close all sensors to optimize performance
     */
    private fun closeSensors() {
        sensorManager.unregisterListener(sensorEventListener, linearAccelerometer)
    }
}