package upm.gretaapp.network

import retrofit2.http.GET
import retrofit2.http.Query
import upm.gretaapp.model.NominatimResult

/**
 * Interface for [retrofit2] to retrieve a list of [NominatimResult] from the API
 */
interface NominatimApiService {
    @GET("search")
    suspend fun getResults(
        @Query("format") format: String = "jsonv2",
        @Query("q") query: String
    ): List<NominatimResult>
}