package upm.gretaapp.data

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import upm.gretaapp.KEY_FILENAME
import upm.gretaapp.workers.RecordingWorker
import upm.gretaapp.workers.writeCsvHeader
import java.util.concurrent.TimeUnit

/**
 * Implementation of [RecordingRepository] to start recording a route using the phone information
 */
class LocalRecordingRepository(private val context: Context): RecordingRepository {

    // WorkManager to control all background processes
    private val workManager = WorkManager.getInstance(context)
    // Flow to observe the state of the current work from the UI
    override val outputWorkInfo: Flow<WorkInfo?> =
        workManager.getWorkInfosByTagLiveData("OUTPUT").asFlow().map {
            if (it.isNotEmpty()) it.first() else null
        }

    /**
     * Function to record a route until it finishes to calculate consumption values
     *
     * @param userId The id of the user recording a route
     * @param vehicleId The id of the vehicle used to record a route
     * @param filename Name that the recording file will have
     */
    override fun recordRoute(userId: Long, vehicleId: Long, filename: String) {
        // Constraints of the worker to assure it functions
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()

        // The header of the file is written once
        writeCsvHeader(context = context, "$filename.csv")

        // Builder for requesting the work one time
        val recordingBuilder = OneTimeWorkRequestBuilder<RecordingWorker>()
        recordingBuilder.setInputData(
            createInputDataForWorkRequest(filename)
        )
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
     * @param filename Name of the file that will be created to store the recording data
     * @return [Data] containing the provided parameters inside
     */
    private fun createInputDataForWorkRequest(filename: String): Data {
        return workDataOf(KEY_FILENAME to filename)
    }

    /**
     * Function to clear results stored from previous works to prevent them to appear more than once
     */
    override fun clearResults() {
        workManager.pruneWork()
    }
}