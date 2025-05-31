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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
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
            .background(Color.Black.copy(alpha = 0.5f))
            .blur(16.dp)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScoreDisplay(gameState)
        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxSize()) {
            RoundIconButton(
                icon = Icons.Default.Refresh,
                description = "Restart",
                onClick = { viewModel.onEvent(GameEvent.OnResetGame) },
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )

            RoundIconButton(
                icon = Icons.Default.Home,
                description = "Home",
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun ScoreDisplay(gameState: GameState) {
    Row(
        modifier = Modifier
            .padding(top = 16.dp)
            .height(64.dp)
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
        fontSize = 24.sp,
        letterSpacing = 2.sp
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