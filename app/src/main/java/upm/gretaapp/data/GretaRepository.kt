package upm.gretaapp.data

import upm.gretaapp.model.Route
import upm.gretaapp.model.RouteEvaluation
import upm.gretaapp.model.RouteEvaluationInput
import upm.gretaapp.model.User
import upm.gretaapp.model.UserVehicle
import upm.gretaapp.model.Vehicle

interface GretaRepository {
    // User methods
    suspend fun getUserByEmail(email: String): User

    suspend fun createUser(user: User): User

    suspend fun updateUser(user: User): User

    // Vehicle methods
    suspend fun getVehicles(query: String = ""): List<Vehicle>

    suspend fun getVehicle(id: Long): Vehicle

    // UserVehicle methods
    suspend fun getUserVehicles(userId: Long): List<UserVehicle>

    suspend fun createUserVehicle(userVehicle: UserVehicle): UserVehicle

    suspend fun updateUserVehicle(userVehicle: UserVehicle): UserVehicle

    suspend fun deleteUserVehicle(id: Long) : Int

    // Route methods
    suspend fun getRoutes(
        source: String,
        destination: String,
        innerCoords: String = "",
        userId: Long,
        vehicleId: Long,
        additionalMass: Long
    ): Map<String, Route>

    suspend fun getScore(
        routeEvaluationInput: RouteEvaluationInput
    ): RouteEvaluation
}