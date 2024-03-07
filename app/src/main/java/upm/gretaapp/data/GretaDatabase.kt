package upm.gretaapp.data

/*@Database(entities = [User::class, Vehicle::class], version = 1, exportSchema = false)
abstract class GretaDatabase : RoomDatabase() {
    // abstract fun userDao(): UserDao
    //abstract fun vehicleDao(): VehicleDao
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
}*/