package eu.tutorials.twocars.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.twocars.data.repository.TeamRepository
import eu.tutorials.twocars.data.state.MenuState
import eu.tutorials.twocars.util.FirebaseUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    remoteConfig: FirebaseRemoteConfig,
    private val teamRepository: TeamRepository
): ViewModel() {

    private val _menuState = MutableStateFlow(MenuState())
    val menuState: StateFlow<MenuState> = _menuState

    init {
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
    }
}