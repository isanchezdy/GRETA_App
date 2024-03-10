package upm.gretaapp.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import upm.gretaapp.model.Route
import upm.gretaapp.model.RouteEvaluation
import upm.gretaapp.model.RouteEvaluationInput
import upm.gretaapp.model.User
import upm.gretaapp.model.UserRoute
import upm.gretaapp.model.UserStats
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle

/**
 * Interface for [retrofit2] to retrieve and upload resources from the dedicated API
 */
interface GretaApiService {
    // User methods
    @GET("users/{email}")
    suspend fun getUserByEmail(
        @Path("email") email: String,
        @Query("api_key") apiKey: String
    ) : User

    @POST("users")
    suspend fun createUser(
        @Body user: User,
        @Query("api_key") apiKey: String
    ): User

    @PUT("users/{id}")
    suspend fun updateUser(
        @Body user: User,
        @Path("id") id: Long = user.userID!!,
        @Query("api_key") apiKey: String
    ): User

    // Vehicle methods
    @GET("vehicles")
    suspend fun getVehicles(
        @Query("q") query: String,
        @Query("api_key") apiKey: String
    ): List<Vehicle>

    @GET("vehicles/{id}")
    suspend fun getVehicle(
        @Path("id") id: Long,
        @Query("api_key") apiKey: String
    ): Vehicle

    // UserVehicle methods
    @GET("user_vehicles/user/{user_id}")
    suspend fun getUserVehicles(
        @Path("user_id") userId: Long,
        @Query("api_key") apiKey: String
    ): List<UserVehicle>

    @POST("user_vehicles")
    suspend fun createUserVehicle(
        @Body userVehicle: UserVehicle,
        @Query("api_key") apiKey: String
    ): UserVehicle

    @PUT("user_vehicles/{id}")
    suspend fun updateUserVehicle(
        @Body userVehicle: UserVehicle,
        @Path("id") id: Long = userVehicle.id!!,
        @Query("api_key") apiKey: String
    ): UserVehicle

    @DELETE("user_vehicles/{id}")
    suspend fun deleteUserVehicle(
        @Path("id") id: Long,
        @Query("api_key") apiKey: String
    ): Int

    // Route methods
    @GET("routes")
    suspend fun calculateRoutes(
        @Query("source") source: String,
        @Query("destination") destination: String,
        @Query("inner_coords") innerCoords: String = "",
        @Query("user_id") userId: Long,
        @Query("vehicle_id") vehicleId: Long,
        @Query("additional_mass") additionalMass: Long,
        @Query("api_key") apiKey: String
    ): Map<String, Route>

    @POST("consumption")
    suspend fun getScore(
        @Body routeEvaluationInput: RouteEvaluationInput,
        @Query("api_key") apiKey: String
    ): RouteEvaluation

    // UserRoute methods
    @POST("user_routes")
    suspend fun createUserRoute(
        @Body userRoute: UserRoute,
        @Query("api_key") apiKey: String
    ): UserRoute

    // UserStats methods
    @GET("user_stats/{user_id}")
    suspend fun getStatsUser(
        @Path("user_id") userId: Long,
        @Query("api_key") apiKey: String
    ): List<UserStats>

    @POST("user_stats")
    suspend fun createUserStats(
        @Body userStats: UserStats,
        @Query("api_key") apiKey: String
    ): UserStats

    // TODO AppReview methods


    // TODO FeatureReview methods
}