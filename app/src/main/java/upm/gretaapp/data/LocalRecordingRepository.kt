package upm.gretaapp.data

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.osmdroid.util.GeoPoint
import upm.gretaapp.KEY_DESTINATION_LAT
import upm.gretaapp.KEY_DESTINATION_LON
import upm.gretaapp.KEY_FILENAME
import upm.gretaapp.workers.RecordingWorker
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Implementation of [RecordingRepository] to start recording a route using the phone information
 */
class LocalRecordingRepository(context: Context): RecordingRepository {

    // WorkManager to control all background processes
    private val workManager = WorkManager.getInstance(context)
    // Flow to observe the state of the current work from the UI
    override val outputWorkInfo: Flow<WorkInfo> =
        workManager.getWorkInfosByTagLiveData("OUTPUT").asFlow().mapNotNull {
            if (it.isNotEmpty()) it.first() else null
        }

    /**
     * Function to record a route until it finishes to calculate consumption values
     *
     * @param destination Destination point to check if current position is near enough to stop
     */
    override fun recordRoute(destination: GeoPoint) {
        // Constraints of the worker to assure it functions
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Builder for requesting the work one time
        val recordingBuilder = OneTimeWorkRequestBuilder<RecordingWorker>()
        recordingBuilder.setInputData(createInputDataForWorkRequest(
            destination, Date().toString()
        ))
        recordingBuilder.setConstraints(constraints)
        recordingBuilder.addTag("RecordingWorker")
        recordingBuilder.keepResultsForAtLeast(5, TimeUnit.MINUTES)

        // The work is started
        val continuation = workManager.beginUniqueWork(
            "RECORDING_WORK",
            ExistingWorkPolicy.REPLACE,
            recordingBuilder.addTag("OUTPUT").build()
        )
        continuation.enqueue()
    }

    /**
     * Function to stop current work in case it is asked
     */
    override fun cancelWork() {
        workManager.cancelUniqueWork("RECORDING_WORK")
    }

    /**
     * Function to create Input [Data] for the next worker to start recording using it
     *
     * @param destination Destination point to check if current position is near enough to stop
     * @param filename Name of the file that will be created to store the recording data
     * @return [Data] containing the provided parameters inside
     */
    private fun createInputDataForWorkRequest(destination: GeoPoint, filename: String): Data {
        val builder = Data.Builder()
        builder.putDouble(KEY_DESTINATION_LAT, destination.latitude)
            .putDouble(KEY_DESTINATION_LON, destination.longitude)
            .putString(KEY_FILENAME, filename)
        return builder.build()
    }

    override fun clearResults() {
        workManager.pruneWork()
    }
}