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

class UserSessionRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val USER = longPreferencesKey("user")
        const val TAG = "UserSessionRepo"
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

    suspend fun saveUserPreference(userId: Long) {
        dataStore.edit { preferences ->
            preferences[USER] = userId
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}