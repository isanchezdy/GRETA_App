package upm.gretaapp.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    // val usersRepository: UsersRepository
    val vehiclesRepository: VehiclesRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineVehiclesRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [VehiclesRepository]
     */
    override val vehiclesRepository: VehiclesRepository by lazy {
        OfflineVehiclesRepository(GretaDatabase.getDatabase(context).vehicleDao())
    }
}