package eu.tutorials.twocars.data.datasource.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStore(private val context: Context) {

    companion object {
        private const val PREF_NAME = "game_prefs"
        private val Context.dataStore by preferencesDataStore(name = PREF_NAME)

        val HIGH_SCORE_KEY = longPreferencesKey("high_score")
        val CUMULATIVE_SCORE_KEY = longPreferencesKey("cumulative_score")
        val UNLOCKED_TEAMS_KEY = stringSetPreferencesKey("unlocked_teams")
        val EARNED_ACHIEVEMENTS_KEY = stringSetPreferencesKey("earned_achievements")
        val ONBOARDING_SEEN_KEY = booleanPreferencesKey("onboarding_seen")

        fun highScoreKeyForTeam(teamId: String) = longPreferencesKey("high_score_$teamId")
    }

    suspend fun saveBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getBoolean(key: Preferences.Key<Boolean>, defaultValue: Boolean): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
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

    suspend fun saveStringSet(key: Preferences.Key<Set<String>>, value: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getStringSet(key: Preferences.Key<Set<String>>, defaultValue: Set<String> = emptySet()): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    suspend fun addToStringSet(key: Preferences.Key<Set<String>>, value: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[key] ?: emptySet()
            preferences[key] = current + value
        }
    }

    suspend fun addCumulativeScore(score: Long) {
        context.dataStore.edit { preferences ->
            val current = preferences[CUMULATIVE_SCORE_KEY] ?: 0L
            preferences[CUMULATIVE_SCORE_KEY] = current + score
        }
    }
}
