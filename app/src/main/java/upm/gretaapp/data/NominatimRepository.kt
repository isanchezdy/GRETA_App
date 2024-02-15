package upm.gretaapp.data

import upm.gretaapp.model.NominatimResult
import upm.gretaapp.network.NominatimApiService

/**
 * Interface to define the main operations that a repository needs to retrieve [NominatimResult]
 */
interface NominatimRepository {
    suspend fun getNominatimResults(query: String): List<NominatimResult>
}

/**
 * Implementation of [NominatimRepository] to retrieve results from the Nominatim API
 */
class NetworkNominatimRepository(
    private val nominatimApiService: NominatimApiService
) : NominatimRepository {
    override suspend fun getNominatimResults(query: String): List<NominatimResult> =
        nominatimApiService.getResults(query = "\"" + query + "\"")

}