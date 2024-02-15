package upm.gretaapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import upm.gretaapp.model.Converters
import upm.gretaapp.model.User
import upm.gretaapp.model.Vehicle

@Database(entities = [User::class, Vehicle::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GretaDatabase : RoomDatabase() {
    // abstract fun userDao(): UserDao
    abstract fun vehicleDao(): VehicleDao
    companion object {
        @Volatile
        private var Instance: GretaDatabase? = null
        fun getDatabase(context: Context): GretaDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, GretaDatabase::class.java, "greta_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}