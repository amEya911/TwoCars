package eu.tutorials.twocars.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.tutorials.twocars.data.event.GameEvent
import eu.tutorials.twocars.data.state.GameState
import eu.tutorials.twocars.ui.viewmodel.GameViewModel

@Composable
fun GameOverLayout(
    modifier: Modifier = Modifier,
    gameState: GameState,
    viewModel: GameViewModel,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Victory or Game Over title
        Text(
            text = if (gameState.isVictory) "🏆 VICTORY!" else "GAME OVER",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (gameState.isVictory) Color(0xFFFFD700) else Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Score display
        ScoreDisplay(gameState)

        Spacer(modifier = Modifier.height(16.dp))

        // Star rating
        StarRating(stars = gameState.runStats.starRating)

        Spacer(modifier = Modifier.height(16.dp))

        // Run stats
        RunStatsDisplay(gameState)

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            RoundIconButton(
                icon = Icons.Default.Refresh,
                description = "Restart",
                onClick = { viewModel.onEvent(GameEvent.OnResetGame) },
                modifier = Modifier.size(64.dp)
            )

            RoundIconButton(
                icon = Icons.Default.Home,
                description = "Home",
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StarRating(stars: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(3) { index ->
            Text(
                text = if (index < stars) "⭐" else "☆",
                fontSize = 36.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun RunStatsDisplay(gameState: GameState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatRow("🎯 Circles Collected", "${gameState.runStats.circlesCollected}")
            StatRow("🔥 Longest Combo", "${gameState.runStats.longestCombo}x")
            StatRow("😰 Near Misses", "${gameState.runStats.nearMissCount}")
            StatRow("⭐ Power-Ups Used", "${gameState.runStats.powerUpsCollected}")
            StatRow("⏱️ Survival Time", formatTime(gameState.runStats.survivalTimeMs))
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(ms: Long): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) "${minutes}m ${secs}s" else "${secs}s"
}

@Composable
fun ScoreDisplay(gameState: GameState) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .height(56.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ScoreText("SCORE", gameState.score)
        ScoreText("BEST", gameState.highScore)
    }
}

@Composable
fun ScoreText(label: String, value: Long) {
    Text(
        text = "$label  $value",
        color = MaterialTheme.colorScheme.tertiary,
        fontSize = 20.sp,
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}