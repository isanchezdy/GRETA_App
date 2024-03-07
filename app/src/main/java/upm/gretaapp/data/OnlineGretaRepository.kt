package upm.gretaapp.data

import upm.gretaapp.model.Route
import upm.gretaapp.model.RouteEvaluation
import upm.gretaapp.model.RouteEvaluationInput
import upm.gretaapp.model.User
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle
import upm.gretaapp.network.GretaApiService

class OnlineGretaRepository(
    private val gretaApiService: GretaApiService
) : GretaRepository {
    override suspend fun getUserByEmail(email: String): User = gretaApiService.getUserByEmail(
        email = email,
        apiKey = API_KEY
    )

    override suspend fun createUser(user: User): User = gretaApiService.createUser(
        user = user,
        apiKey = API_KEY
    )

    override suspend fun updateUser(user: User): User = gretaApiService.updateUser(
        user = user,
        apiKey = API_KEY
    )

    override suspend fun getVehicles(): List<Vehicle> = gretaApiService.getVehicles(
        apiKey = API_KEY
    )

    override suspend fun getVehicle(id: Long): Vehicle = gretaApiService.getVehicle(
        id = id,
        apiKey = API_KEY
    )

    override suspend fun getUserVehicles(userId: Long): List<UserVehicle> = gretaApiService
        .getUserVehicles(userId = userId, apiKey = API_KEY)

    override suspend fun createUserVehicle(userVehicle: UserVehicle): UserVehicle = gretaApiService
        .createUserVehicle(userVehicle = userVehicle, apiKey = API_KEY)

    override suspend fun updateUserVehicle(userVehicle: UserVehicle): UserVehicle = gretaApiService
        .updateUserVehicle(userVehicle = userVehicle, apiKey = API_KEY)

    override suspend fun deleteUserVehicle(id: Long) = gretaApiService.deleteUserVehicle(
        id = id,
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
        userId = userId,
        vehicleId = vehicleId,
        additionalMass = additionalMass,
        apiKey = API_KEY
    )

    override suspend fun getScore(
        routeEvaluationInput: RouteEvaluationInput
    ): RouteEvaluation = gretaApiService.getScore(
        routeEvaluationInput = routeEvaluationInput,
        apiKey = API_KEY
    )

}