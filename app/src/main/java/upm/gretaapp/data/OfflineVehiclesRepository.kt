package upm.gretaapp.data

import kotlinx.coroutines.flow.Flow
import upm.gretaapp.model.Vehicle

/*class OfflineVehiclesRepository(/*private val vehicleDao: VehicleDao*/) : VehiclesRepository {
    override fun getAllVehiclesFromUserStream(user: Int): Flow<List<Vehicle>> = vehicleDao.getAllVehiclesFromUser(user)
    override fun getVehicleStream(id: Int): Flow<Vehicle?> = vehicleDao.getVehicle(id)

    override suspend fun insertVehicle(vehicle: Vehicle) = vehicleDao.insert(vehicle)

    override suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.delete(vehicle)

    override suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.update(vehicle)
}*/