package upm.gretaapp.data

import upm.gretaapp.model.AppReview
import upm.gretaapp.model.FeatureReview
import upm.gretaapp.model.Route
import upm.gretaapp.model.PerformedRouteMetrics
import upm.gretaapp.model.InputPerformedRoute
import upm.gretaapp.model.User
import upm.gretaapp.model.UserRoute
import upm.gretaapp.model.UserStats
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import upm.gretaapp.network.GretaApiService

/**
 * Implementation of [GretaRepository] to retrieve and store results of the app using [GretaApiService]
 */
class OnlineGretaRepository(
    private val gretaApiService: GretaApiService
) : GretaRepository {
    override suspend fun getUserByEmail(email: String): User = gretaApiService.getUserByEmail(
        email = email,
        apiKey = API_KEY
    ).first()

    override suspend fun createUser(user: User): User = gretaApiService.createUser(
        user = user,
        apiKey = API_KEY
    )

    override suspend fun updateUser(user: User): User = gretaApiService.updateUser(
        user = user,
        apiKey = API_KEY
    )

    override suspend fun getVehicles(query: String): List<Vehicle> = gretaApiService.getVehicles(
        query = query,
        apiKey = API_KEY
    )

    override suspend fun getVehicle(vehicleId: Long): Vehicle = gretaApiService.getVehicle(
        vehicleId = vehicleId,
        apiKey = API_KEY
    ).first()

    override suspend fun getUserVehicles(userId: Long): List<UserVehicle> = gretaApiService
        .getUserVehicles(userId = userId, apiKey = API_KEY)

    override suspend fun createUserVehicle(userVehicle: UserVehicle): UserVehicle = gretaApiService
        .createUserVehicle(userVehicle = userVehicle, apiKey = API_KEY)

    override suspend fun updateUserVehicle(userVehicle: UserVehicle): UserVehicle = gretaApiService
        .updateUserVehicle(userVehicle = userVehicle, apiKey = API_KEY)

    override suspend fun deleteUserVehicle(userVehicleId: Long) = gretaApiService.deleteUserVehicle(
        userVehicleId = userVehicleId,
        apiKey = API_KEY
    )

    override suspend fun getRoutes(
        source: String,
        destination: String,
        innerCoords: String,
        userId: Long,
        vehicleId: Long,
        additionalMass: Long
    ): Map<String, Route> = gretaApiService.calculateRoutes(
        source = source,
        destination = destination,
        innerCoords = innerCoords,
        vehicleId = vehicleId,
        additionalMass = additionalMass,
        apiKey = API_KEY
    )

    override suspend fun calculatePerformedRouteMetrics(
        inputPerformedRoute: InputPerformedRoute
    ): PerformedRouteMetrics = gretaApiService.calculatePerformedRouteMetrics(
        inputPerformedRoute = inputPerformedRoute,
        apiKey = API_KEY
    )

    override suspend fun createUserRoute(userRoute: UserRoute): UserRoute = gretaApiService
        .createUserRoute(
            userRoute = userRoute,
            apiKey = API_KEY
        )

    override suspend fun getRoutesHistoryUser(userId: Long): List<UserRoute> = gretaApiService
        .getUserRoutes(
            userId = userId,
            apiKey = API_KEY
        )

    override suspend fun getStatsUser(userId: Long): List<UserStats> = gretaApiService.getUserStats(
        userId = userId,
        apiKey = API_KEY
    )

    override suspend fun createUserStats(userStats: UserStats): UserStats = gretaApiService
        .createUserStats(
            userStats = userStats,
            apiKey = API_KEY
        )

    override suspend fun createAppReview(appReview: AppReview): AppReview = gretaApiService
        .createAppReview(appReview = appReview, apiKey = API_KEY)

    override suspend fun createFeatureReview(featureReview: FeatureReview): FeatureReview =
        gretaApiService.createFeatureReview(featureReview = featureReview, apiKey = API_KEY)

}