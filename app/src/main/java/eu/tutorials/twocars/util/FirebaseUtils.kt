package eu.tutorials.twocars.util

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import eu.tutorials.twocars.data.model.RemoteBackground
import eu.tutorials.twocars.data.model.SeparationTime
import eu.tutorials.twocars.ui.theme.GameBackground
import eu.tutorials.twocars.ui.theme.getGameTheme
import eu.tutorials.twocars.ui.theme.mercedes
import org.json.JSONObject
import androidx.core.graphics.toColorInt

object FirebaseUtils {

    fun getSeparationTimeConfig(remoteConfig: FirebaseRemoteConfig): SeparationTime {
        val separationTime = remoteConfig.getLong("separation_time")

        return SeparationTime(
            separationTime = separationTime
                .takeIf { it in 200L..2000L }
                ?: 700L
        )
    }

    fun getGameThemeFromRemote(gameId: String?): ColorScheme {
        val themeKey = "theme_${gameId ?: "mercedes"}"
        val defaultColors = mercedes

        val remoteConfig = Firebase.remoteConfig
        //remoteConfig.fetchAndActivate().await()

        val json = remoteConfig.getString(themeKey)

        return try {
            val obj = JSONObject(json)
            darkColorScheme(
                primary = Color(obj.getString("primary").toColorInt()),
                secondary = Color(obj.getString("secondary").toColorInt()),
                tertiary = Color(obj.getString("tertiary").toColorInt())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            getGameTheme(gameId)
        }
    }

    fun getBackgroundImages(remoteConfig: FirebaseRemoteConfig): List<RemoteBackground> {
        val json = remoteConfig.getString("background_images")
        Log.d("FirebaseUtils", "json: $json")
        return try {
            val jsonObject = JSONObject(json)
            jsonObject.keys().asSequence().map { key ->

                Log.d("FirebaseUtils", "url: ${jsonObject.getString(key)}")
                RemoteBackground(name = key, url = jsonObject.getString(key))
            }.toList()
        } catch (e: Exception) {
            e.printStackTrace()
            //emptyList()
            GameBackground.entries.map {
                RemoteBackground(name = it.name, url = it.url)
            }
        }
    }
}