package eu.tutorials.twocars.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import eu.tutorials.twocars.data.model.F1Team
import org.json.JSONObject

/**
 * Repository that manages F1 team display names from Firebase Remote Config.
 *
 * Team display names are stored under the "team_display_names" Remote Config key as a JSON object:
 * ```json
 * {
 *   "red_bull": "Oracle Red Bull Racing",
 *   "ferrari": "Scuderia Ferrari",
 *   ...
 * }
 * ```
 *
 * If a team ID is not found in Remote Config, the fallback display name from [F1Team] is used.
 */
class TeamRepository(private val remoteConfig: FirebaseRemoteConfig) {

    private var displayNameOverrides: Map<String, String> = emptyMap()

    /**
     * Refreshes display name overrides from Remote Config.
     * Call this after [FirebaseRemoteConfig.fetchAndActivate] completes.
     */
    fun refresh() {
        val json = remoteConfig.getString("team_display_names")
        displayNameOverrides = try {
            val obj = JSONObject(json)
            obj.keys().asSequence().associateWith { obj.getString(it) }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Returns the display name for the given team ID.
     * Priority: Remote Config override > F1Team default > formatted ID fallback.
     */
    fun getDisplayName(teamId: String): String {
        return displayNameOverrides[teamId]
            ?: F1Team.fromId(teamId)?.defaultDisplayName
            ?: teamId.replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    /**
     * Returns all [F1Team] entries.
     */
    fun getAllTeams(): List<F1Team> = F1Team.entries
}
