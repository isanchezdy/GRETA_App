package upm.gretaapp.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides CRUD of [Vehicle] from a given data source.
 */
interface VehiclesRepository {
    /**
     * Retrieves all the vehicles from a certain [user] from the given data source.
     */
    fun getAllVehiclesFromUserStream(user: Int): Flow<List<Vehicle>>

    /**
     * Retrieves a vehicle from the given data source that matches with the [id].
     */
    fun getVehicleStream(id: Int): Flow<Vehicle?>

    /**
     * Insert vehicle in the data source
     */
    suspend fun insertVehicle(vehicle: Vehicle)

    /**
     * Delete vehicle from the data source
     */
    suspend fun deleteVehicle(vehicle: Vehicle)

    /**
     * Update vehicle in the data source
     */
    suspend fun updateVehicle(vehicle: Vehicle)
}