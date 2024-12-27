package upm.gretaapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import upm.gretaapp.network.GretaApiService
import upm.gretaapp.network.NominatimApiService
import java.util.concurrent.TimeUnit


/**
 * App container for Dependency injection.
 */
interface AppContainer {

    val phoneSessionRepository: PhoneSessionRepository
    val recordingRepository: RecordingRepository
    val nominatimRepository: NominatimRepository
    val gretaRepository: GretaRepository
    val vehicleFactorRepository: VehicleFactorRepository
}

/**
 * [Interceptor] implementation to add GRETA header to the requests for Nominatim
 */
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("User-Agent", "GRETAApp/1.0 " + System.getProperty("http.agent"))
            .build()
        return chain.proceed(request)
    }
}

/**
 * [AppContainer] implementation that provides instance of all repositories
 */
class AppDataContainer(private val context: Context) : AppContainer {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_session_preferences"
    )

    /**
     * Implementation of [PhoneSessionRepository]
     */
    override val phoneSessionRepository: PhoneSessionRepository =
        PhoneSessionRepository(context.dataStore)


    /**
     * Link to nominatim service
     */
    private val nominatimUrl = "https://nominatim.openstreetmap.org"

    private val nominatimRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(nominatimUrl)
        .client(OkHttpClient.Builder().addInterceptor(HeaderInterceptor()).build())
        .build()

    /**
     * Implementation of [NominatimApiService] using [Retrofit]
     */
    private val nominatimService: NominatimApiService by lazy {
        nominatimRetrofit.create(NominatimApiService::class.java)
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    private val gretaRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(API_URL)
        .client(client)
        .build()

    /**
     * Implementation of [GretaApiService] using [Retrofit]
     */
    private val gretaApiService: GretaApiService by lazy {
        gretaRetrofit.create(GretaApiService::class.java)
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

    /**
     * Implementation for [GretaRepository]
     */
    override val gretaRepository: GretaRepository by lazy {
        OnlineGretaRepository(gretaApiService)
    }

    /**
     * Implementation for [VehicleFactorRepository]
     */
    override val vehicleFactorRepository: VehicleFactorRepository by lazy {
        OfflineVehicleFactorRepository(VehicleFactorDatabase.getDatabase(context).vehicleFactorDao())
    }
}