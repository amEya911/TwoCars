package eu.tutorials.twocars.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.data.state.MenuState
import eu.tutorials.twocars.ui.component.MenuItem

@Composable
fun GameMenu(
    menuState: MenuState,
    modifier: Modifier = Modifier,
    navController: NavController,
    onGameModeSelected: (GameMode) -> Unit = {}
) {
    val backgrounds = menuState.backgrounds

    Log.d("GameMenu", "backgrounds: $backgrounds")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCC0000),
                        Color(0xFFFF1E00),
                        Color(0xFFCC0000)
                    )
                )
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Collect & Dodge",
            fontSize = 32.sp,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Cumulative score display
        Text(
            text = "⭐ Total Score: ${menuState.cumulativeScore}",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Game mode selector
        GameModeSelector(
            selectedMode = menuState.selectedGameMode,
            onModeSelected = onGameModeSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            contentPadding = PaddingValues(horizontal = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(backgrounds.sortedBy { getUnlockThreshold(it.name.lowercase()) }) { background ->
                val isUnlocked = background.name.lowercase() in menuState.unlockedTeams ||
                        background.name in menuState.unlockedTeams
                val unlockThreshold = getUnlockThreshold(background.name.lowercase())

                MenuItem(
                    navController = navController,
                    backgroundUrl = background.url,
                    name = background.displayName,
                    originalName = background.name,
                    isLocked = !isUnlocked,
                    unlockRequirement = if (!isUnlocked) "Score $unlockThreshold total" else null,
                    gameMode = menuState.selectedGameMode
                )
            }
        }
    }
}

@Composable
fun GameModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GameMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            Text(
                text = mode.displayName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

private fun getUnlockThreshold(teamId: String): Long {
    // Based on 2025 F1 Constructors' Championship standings
    // Bottom teams = easy, top teams = hardest
    return when (teamId) {
        "cadillac" -> 0L          // New team (11th) — free starter
        "audi" -> 100L            // Was Kick Sauber (10th)
        "williams" -> 250L        // 9th
        "racing_bulls" -> 500L    // 8th
        "haas" -> 800L            // 7th
        "alpine" -> 1200L         // 6th
        "aston_martin" -> 1800L   // 5th
        "mercedes" -> 2500L       // 4th
        "red_bull" -> 3500L       // 3rd
        "ferrari" -> 5000L        // 2nd
        "mclaren" -> 7500L        // 1st (Champions)
        else -> 0L
    }
}
