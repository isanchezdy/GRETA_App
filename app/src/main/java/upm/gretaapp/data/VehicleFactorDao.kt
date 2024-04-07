package upm.gretaapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import upm.gretaapp.model.VehicleFactor

/**
 * Interface to define the operations in the database for managing [VehicleFactor] objects
 */
@Dao
interface VehicleFactorDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vehicleFactor: VehicleFactor)

    @Update
    suspend fun update(vehicleFactor: VehicleFactor)

    @Delete
    suspend fun delete(vehicleFactor: VehicleFactor)

    @Query("SELECT * from vehicles WHERE id = :id")
    fun getVehicleFactor(id: Long): VehicleFactor?
}