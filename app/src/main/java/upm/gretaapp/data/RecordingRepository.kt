package upm.gretaapp.data

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.GeoPoint

/**
 * Interface to define the main functions and parameters to record a route and keep track of
 * its progress
 */
interface RecordingRepository {
    val outputWorkInfo: Flow<WorkInfo>
    fun recordRoute(destination: GeoPoint)
    fun cancelWork()
}