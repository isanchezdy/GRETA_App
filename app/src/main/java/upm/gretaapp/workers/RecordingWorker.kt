package upm.gretaapp.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import upm.gretaapp.DELAY_TIME_MICRO
import upm.gretaapp.DELAY_TIME_MILLIS
import upm.gretaapp.KEY_DESTINATION_LAT
import upm.gretaapp.KEY_DESTINATION_LON
import upm.gretaapp.KEY_FILENAME
import upm.gretaapp.R
import upm.gretaapp.TIMER_MILLIS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt


private const val TAG = "RecordingWorker"

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
    private lateinit var locationManager: LocationManager
    private lateinit var gnssStatusCallback: GnssStatus.Callback
    private lateinit var sensorEventListener: SensorEventListener
    private lateinit var linearAccelerometer: Sensor

    // Instance of a recording to keep updating its data each second
    private var recording = Recording("",null,null,null,0.0,
        null, null, null, null, null)

    // Values to retrieve the current location of the phone
    private var location = mutableStateOf<Location?>(null)
    private val locationListener = LocationListener { newLocation ->
        val speed = when {
            // Retrieve speed if available
            newLocation.hasSpeed() -> newLocation.speed.toDouble()
            // If there is previous location info calculate it
            location.value != null -> {
                // Convert milliseconds to seconds
                val elapsedTimeInSeconds =
                    (newLocation.time - (location.value?.time ?: 0)) / 1000
                // Calculate distance in meters from previous to current location
                val distanceInMeters = location.value?.distanceTo(newLocation) ?: 0.0f
                // Parse from m/s to km/h
                (distanceInMeters / elapsedTimeInSeconds) * 3.6
            }
            // Otherwise set default to 0.0
            else -> 0.0
        }
        // Update previous location with new location info
        location.value = newLocation
        // Store speed
        recording = recording.copy(speed = speed)
    }
    private val handlerThread = HandlerThread("LocationThread")

    override suspend fun doWork(): Result {
        val channelId = "greta_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            // Get notification manager
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            // Create notification channel
            manager.createNotificationChannel(serviceChannel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.recording_route))
            .build()

        val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            ForegroundInfo(1, notification)
        }
        setForeground(foregroundInfo)

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

                // Initialize all sensors
                initializeSensors()
                initializeLocation()

                // Start timer to stop in case of error
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                while(timer > 0) {
                    // The recording is updated
                    recording = recording.copy(latitude = location.value?.latitude,
                        longitude = location.value?.longitude,
                        altitude = location.value?.altitude,
                        timestamp = dateFormat.format(Date()))

                    writeRecordingToCsv(applicationContext, recording, filename)

                    // Initialize satellite number to 0
                    recording = recording.copy(numSatellites = 0)

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

                val output = workDataOf("filename" to filename)

                // Define notification intent
                /*val notificationIntent = Intent(applicationContext, MainActivity::class.java)
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(notificationIntent)*/

                // Return success
                Result.success(output)
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    "Error",
                    throwable
                )
                closeSensors()
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

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    private fun initializeLocation() {
        // Get location manager
        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE)
                as LocationManager

        // Create GNSS status callback that updated the number of satellites used
        // https://developer.android.com/reference/android/location/GnssStatus
        gnssStatusCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                for (i in 0 until status.satelliteCount) {
                    if (status.usedInFix(i))
                        recording = recording.copy(numSatellites = recording.numSatellites?.plus(1))
                }
            }
        }

        if(!handlerThread.isAlive) {
            handlerThread.start()
        }

        val handler = Handler(handlerThread.looper)

        // Register GNSS status callback
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.registerGnssStatusCallback(gnssStatusCallback, handler)
        // Request location updates with the previously define listener
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 0L, 0f, locationListener, handler.looper
        )
    }

    /**
     * Close all sensors to optimize performance
     */
    private fun closeSensors() {
        sensorManager.unregisterListener(sensorEventListener, linearAccelerometer)

        locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
        locationManager.removeUpdates(locationListener)

        if(handlerThread.isAlive) {
            handlerThread.quitSafely()
        }
    }
}