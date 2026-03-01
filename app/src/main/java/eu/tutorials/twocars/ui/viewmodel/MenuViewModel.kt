package eu.tutorials.twocars.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.twocars.data.datasource.local.DataStore
import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.data.repository.TeamRepository
import eu.tutorials.twocars.data.state.MenuState
import eu.tutorials.twocars.util.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    remoteConfig: FirebaseRemoteConfig,
    private val teamRepository: TeamRepository,
    private val dataStore: DataStore
): ViewModel() {

    private val _menuState = MutableStateFlow(MenuState())
    val menuState: StateFlow<MenuState> = _menuState

    // null = loading, true = show onboarding, false = skip it
    private val _showOnboarding = MutableStateFlow<Boolean?>(null)
    val showOnboarding: StateFlow<Boolean?> = _showOnboarding

    init {
        // Check onboarding flag first
        viewModelScope.launch {
            val seen = dataStore.getBoolean(DataStore.ONBOARDING_SEEN_KEY, false).first()
            _showOnboarding.value = !seen
        }
        viewModelScope.launch {
            remoteConfig.fetchAndActivate().addOnCompleteListener {
                teamRepository.refresh()
                val backgrounds =
                    FirebaseUtils.getBackgroundImages(remoteConfig).map { bg ->
                        bg.copy(displayName = teamRepository.getDisplayName(bg.name))
                    }

                _menuState.value = _menuState.value.copy(
                    backgrounds = backgrounds
                )
            }
        }

        // Load progression data
        viewModelScope.launch {
            val unlockedTeams = dataStore.getStringSet(
                DataStore.UNLOCKED_TEAMS_KEY,
                setOf("cadillac")
            ).first()

            val earnedAchievements = dataStore.getStringSet(
                DataStore.EARNED_ACHIEVEMENTS_KEY
            ).first()

            val cumulativeScore = dataStore.getData(
                DataStore.CUMULATIVE_SCORE_KEY, 0L
            ).first()

            _menuState.value = _menuState.value.copy(
                unlockedTeams = unlockedTeams,
                earnedAchievements = earnedAchievements,
                cumulativeScore = cumulativeScore
            )
        }
    }

    fun setGameMode(mode: GameMode) {
        _menuState.value = _menuState.value.copy(selectedGameMode = mode)
    }

    fun refreshUnlocks() {
        viewModelScope.launch {
            val unlockedTeams = dataStore.getStringSet(
                DataStore.UNLOCKED_TEAMS_KEY,
                setOf("cadillac")
            ).first()

            val earnedAchievements = dataStore.getStringSet(
                DataStore.EARNED_ACHIEVEMENTS_KEY
            ).first()

            val cumulativeScore = dataStore.getData(
                DataStore.CUMULATIVE_SCORE_KEY, 0L
            ).first()

            _menuState.value = _menuState.value.copy(
                unlockedTeams = unlockedTeams,
                earnedAchievements = earnedAchievements,
                cumulativeScore = cumulativeScore
            )
        }
    }

    fun markOnboardingSeen() {
        viewModelScope.launch {
            dataStore.saveBoolean(DataStore.ONBOARDING_SEEN_KEY, true)
            _showOnboarding.value = false
        }
    }
}