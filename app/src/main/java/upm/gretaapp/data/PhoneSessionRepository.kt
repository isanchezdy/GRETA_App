package upm.gretaapp.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PhoneSessionRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val USER = longPreferencesKey("user")
        const val TAG = "PhoneSessionRepo"
        val VEHICLE_FACTOR: (Long) -> Preferences.Key<Double> = {
            doublePreferencesKey("vehicle_factor$it")
        }
        val NEEDS_CONSUMPTION: (Long) -> Preferences.Key<Boolean> = {
            booleanPreferencesKey("needs_consumption$it")
        }
    }

    val user: Flow<Long> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[USER] ?: -1
        }

    fun vehicleFactor(vehicleId: Long): Flow<Double> {
        return dataStore.data
            .catch {
                if (it is IOException) {
                    Log.e(TAG, "Error reading preferences.", it)
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map{ preferences ->
                preferences[VEHICLE_FACTOR(vehicleId)] ?: 1.0
            }
    }

    fun needsConsumption(vehicleId: Long): Flow<Boolean> {
        return dataStore.data
            .catch {
                if (it is IOException) {
                    Log.e(TAG, "Error reading preferences.", it)
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map{ preferences ->
                preferences[NEEDS_CONSUMPTION(vehicleId)] ?: true
            }
    }

    suspend fun saveUserPreference(userId: Long) {
        dataStore.edit { preferences ->
            preferences[USER] = userId
        }
    }

    suspend fun saveVehicleFactor(vehicleId: Long, vehicleFactor: Double) {
        dataStore.edit { preferences ->
            preferences[VEHICLE_FACTOR(vehicleId)] = vehicleFactor
        }
    }

    suspend fun saveNeedsConsumption(vehicleId: Long, needsConsumption: Boolean) {
        dataStore.edit { preferences ->
            preferences[NEEDS_CONSUMPTION(vehicleId)] = needsConsumption
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[USER] = -1
        }
    }
}