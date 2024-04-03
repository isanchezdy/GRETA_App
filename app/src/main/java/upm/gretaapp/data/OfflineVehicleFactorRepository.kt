package upm.gretaapp.data

import kotlinx.coroutines.flow.Flow
import upm.gretaapp.model.VehicleFactor

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