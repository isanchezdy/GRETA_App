package upm.gretaapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import upm.gretaapp.model.VehicleFactor

@Database(entities = [VehicleFactor::class], version = 1, exportSchema = false)
abstract class VehicleFactorDatabase : RoomDatabase() {
    abstract fun vehicleFactorDao(): VehicleFactorDao
    companion object {
        @Volatile
        private var Instance: VehicleFactorDatabase? = null
        fun getDatabase(context: Context): VehicleFactorDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, VehicleFactorDatabase::class.java, "vehicle_factor_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}