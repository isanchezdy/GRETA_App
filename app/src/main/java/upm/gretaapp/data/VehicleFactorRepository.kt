package upm.gretaapp.data

import kotlinx.coroutines.flow.Flow
import upm.gretaapp.model.VehicleFactor

/**
 * Repository that provides insert, update, delete, and retrieve of [VehicleFactor] from a given data source.
 */
interface VehicleFactorRepository {

    /**
     * Retrieve a vehicle factor from the given data source that matches with the [id].
     */
    fun getVehicleFactorStream(id: Long): Flow<VehicleFactor?>

    /**
     * Insert vehicle factor in the data source
     */
    suspend fun insertVehicleFactor(vehicleFactor: VehicleFactor)

    /**
     * Delete vehicle factor from the data source
     */
    suspend fun deleteVehicleFactor(vehicleFactor: VehicleFactor)

    /**
     * Update vehicle factor in the data source
     */
    suspend fun updateVehicleFactor(vehicleFactor: VehicleFactor)
}