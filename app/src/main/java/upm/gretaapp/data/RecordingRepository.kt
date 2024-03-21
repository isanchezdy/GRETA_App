package upm.gretaapp.data

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

/**
 * Interface to define the main functions and parameters to record a route and keep track of
 * its progress
 */
interface RecordingRepository {
    val outputWorkInfo: Flow<WorkInfo?>
    fun recordRoute(userId: Long, vehicleId: Long, filename: String)
    fun cancelWork()

    fun clearResults()
}