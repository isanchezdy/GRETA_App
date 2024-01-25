package upm.gretaapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vehicle: Vehicle)

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)

    @Query("SELECT * from vehicles WHERE id = :id")
    fun getVehicle(id: Int): Flow<Vehicle>

    @Query("SELECT * from vehicles WHERE user = :user")
    fun getAllVehiclesFromUser(user: Int): Flow<List<Vehicle>>
}