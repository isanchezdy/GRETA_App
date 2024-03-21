package upm.gretaapp.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import upm.gretaapp.model.AppReview
import upm.gretaapp.model.FeatureReview
import upm.gretaapp.model.Route
import upm.gretaapp.model.PerformanceRouteMetrics
import upm.gretaapp.model.InputPerformedRoute
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
    @GET("users")
    suspend fun getUserByEmail(
        @Query("email") email: String,
        @Query("api_key") apiKey: String
    ) : User

    @POST("users")
    suspend fun createUser(
        @Body user: User,
        @Query("api_key") apiKey: String
    ): User

    @PUT("users")
    suspend fun updateUser(
        @Body user: User,
        @Query("api_key") apiKey: String
    ): User

    // Vehicle methods
    @GET("vehicles")
    suspend fun getVehicles(
        @Query("q") query: String,
        @Query("api_key") apiKey: String
    ): List<Vehicle>

    @GET("vehicles")
    suspend fun getVehicle(
        @Query("vehicle_id") vehicleId: Long,
        @Query("api_key") apiKey: String
    ): Vehicle

    // UserVehicle methods
    @GET("user_vehicles")
    suspend fun getUserVehicles(
        @Query("user") userId: Long,
        @Query("api_key") apiKey: String
    ): List<UserVehicle>

    @POST("user_vehicles")
    suspend fun createUserVehicle(
        @Body userVehicle: UserVehicle,
        @Query("api_key") apiKey: String
    ): UserVehicle

    @PUT("user_vehicles")
    suspend fun updateUserVehicle(
        @Body userVehicle: UserVehicle,
        @Query("api_key") apiKey: String
    ): UserVehicle

    @DELETE("user_vehicles/{id}")
    suspend fun deleteUserVehicle(
        @Query("user_vehicle_id") userVehicleId: Long,
        @Query("api_key") apiKey: String
    ): Int

    // Route methods
    @GET("routes")
    suspend fun calculateRoutes(
        @Query("source") source: String,
        @Query("destination") destination: String,
        @Query("inner_coords") innerCoords: String = "",
        @Query("vehicle_id") vehicleId: Long,
        @Query("additional_mass") additionalMass: Long,
        @Query("api_key") apiKey: String
    ): Map<String, Route>

    @POST("routes/performed_route")
    suspend fun calculatePerformedRouteMetrics(
        @Body inputPerformedRoute: InputPerformedRoute,
        @Query("api_key") apiKey: String
    ): PerformanceRouteMetrics

    // UserRoute methods
    @POST("user_routes")
    suspend fun createUserRoute(
        @Body userRoute: UserRoute,
        @Query("api_key") apiKey: String
    ): UserRoute

    @GET("user_routes")
    suspend fun getUserRoutes(
        @Query("user") userId: Long,
        @Query("api_key") apiKey: String
    ): List<UserRoute>

    // UserStats methods
    @GET("user_stats")
    suspend fun getUserStats(
        @Query("user") userId: Long,
        @Query("api_key") apiKey: String
    ): List<UserStats>

    @POST("user_stats")
    suspend fun createUserStats(
        @Body userStats: UserStats,
        @Query("api_key") apiKey: String
    ): UserStats

    // AppReview methods
    @POST("app_review")
    suspend fun createAppReview(
        @Body appReview: AppReview,
        @Query("api_key") apiKey: String
    ): AppReview


    // FeatureReview methods
    @POST("feature_review")
    suspend fun createFeatureReview(
        @Body featureReview: FeatureReview,
        @Query("api_key") apiKey: String
    ): FeatureReview
}