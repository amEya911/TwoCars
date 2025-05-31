package eu.tutorials.twocars.data.datasource.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStore(private val context: Context) {

    companion object {
        private const val PREF_NAME = "game_prefs"
        private val Context.dataStore by preferencesDataStore(name = PREF_NAME)

        val HIGH_SCORE_KEY = longPreferencesKey("high_score")
    }

    suspend fun saveData(key: Preferences.Key<Long>, value: Long) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getData(key: Preferences.Key<Long>, defaultValue: Long): Flow<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }
}
