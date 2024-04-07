package upm.gretaapp.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Repository to obtain values from the internal [DataStore] of the phone
 */
class PhoneSessionRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val USER = longPreferencesKey("user")
        const val TAG = "PhoneSessionRepo"
    }

    /**
     * Flow to retrieve the current user logged in the app
     */
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

    /**
     * Method to change the current user logged in the app
     *
     * @param userId The id of the current user logged in
     */
    suspend fun saveUserPreference(userId: Long) {
        dataStore.edit { preferences ->
            preferences[USER] = userId
        }
    }

    /**
     * Method to exit session for current user
     */
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[USER] = -1
        }
    }
}