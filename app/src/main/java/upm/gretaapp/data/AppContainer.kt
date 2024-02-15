package upm.gretaapp.data

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import upm.gretaapp.network.NominatimApiService

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    // val usersRepository: UsersRepository
    val vehiclesRepository: VehiclesRepository
    val recordingRepository: RecordingRepository
    val nominatimRepository: NominatimRepository
}

/**
 * [AppContainer] implementation that provides instance of all repositories
 */
class AppDataContainer(private val context: Context) : AppContainer {
    private val nominatimUrl = "https://nominatim.openstreetmap.org"

    private val nominatimRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(nominatimUrl)
        .build()

    private val nominatimService: NominatimApiService by lazy {
        nominatimRetrofit.create(NominatimApiService::class.java)
    }

    /**
     * Implementation for [VehiclesRepository]
     */
    override val vehiclesRepository: VehiclesRepository by lazy {
        OfflineVehiclesRepository(GretaDatabase.getDatabase(context).vehicleDao())
    }
    /**
     * Implementation for [RecordingRepository]
     */
    override val recordingRepository: RecordingRepository by lazy {
        LocalRecordingRepository(context)
    }
    /**
     * Implementation for [NominatimRepository]
     */
    override val nominatimRepository: NominatimRepository by lazy {
        NetworkNominatimRepository(nominatimService)
    }
}