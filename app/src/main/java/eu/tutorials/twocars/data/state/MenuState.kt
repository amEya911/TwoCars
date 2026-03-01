package eu.tutorials.twocars.data.state

import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.data.model.RemoteBackground

data class MenuState(
    val backgrounds: List<RemoteBackground> = emptyList(),
    val unlockedTeams: Set<String> = setOf("cadillac"), // cadillac unlocked by default
    val earnedAchievements: Set<String> = emptySet(),
    val cumulativeScore: Long = 0L,
    val selectedGameMode: GameMode = GameMode.ENDLESS
)