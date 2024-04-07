package upm.gretaapp.data

import upm.gretaapp.model.VehicleFactor

/**
 * Implementation of [VehicleFactorRepository] to retrieve and store results using [VehicleFactorDao]
 */
class OfflineVehicleFactorRepository(private val vehicleFactorDao: VehicleFactorDao): VehicleFactorRepository {
    override fun getVehicleFactorStream(id: Long): VehicleFactor? = vehicleFactorDao
        .getVehicleFactor(id)

    override suspend fun insertVehicleFactor(vehicleFactor: VehicleFactor) = vehicleFactorDao
        .insert(vehicleFactor)

    override suspend fun deleteVehicleFactor(vehicleFactor: VehicleFactor) = vehicleFactorDao
        .delete(vehicleFactor)

    override suspend fun updateVehicleFactor(vehicleFactor: VehicleFactor) = vehicleFactorDao
        .update(vehicleFactor)

}