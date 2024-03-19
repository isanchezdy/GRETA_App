package upm.gretaapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import upm.gretaapp.network.GretaApiService
import upm.gretaapp.network.NominatimApiService
import java.util.concurrent.TimeUnit


/**
 * App container for Dependency injection.
 */
interface AppContainer {

    val userSessionRepository: UserSessionRepository
    val recordingRepository: RecordingRepository
    val nominatimRepository: NominatimRepository
    val gretaRepository: GretaRepository
}

/**
 * [AppContainer] implementation that provides instance of all repositories
 */
class AppDataContainer(private val context: Context) : AppContainer {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_session_preferences"
    )

    /**
     * Implementation of [UserSessionRepository]
     */
    override val userSessionRepository: UserSessionRepository =
        UserSessionRepository(context.dataStore)

    private val nominatimUrl = "https://nominatim.openstreetmap.org"

    private val nominatimRetrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(nominatimUrl)
        .build()

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

    override val gretaRepository: GretaRepository by lazy {
        OnlineGretaRepository(gretaApiService)
    }
}