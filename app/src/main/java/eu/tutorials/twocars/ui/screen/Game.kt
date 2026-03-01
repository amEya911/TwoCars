package eu.tutorials.twocars.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import eu.tutorials.twocars.R
import eu.tutorials.twocars.data.event.GameEvent
import eu.tutorials.twocars.ui.viewmodel.GameViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.data.model.PowerUpType
import eu.tutorials.twocars.data.state.GameState
import eu.tutorials.twocars.ui.component.LaunchCarAnimation
import eu.tutorials.twocars.util.Car
import eu.tutorials.twocars.util.Shape.drawShapes
import eu.tutorials.twocars.ui.component.GameOverLayout
import kotlin.random.Random

@Composable
fun Game(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    gameState: GameState,
    navController: NavController,
    gameId: String?
) {
    val coroutineScope = rememberCoroutineScope()

    val carBitmap = ImageBitmap.imageResource(R.drawable.formula_1)
    val car1X = remember { Animatable(0f) }
    val car2X = remember { Animatable(0f) }
    val car1Rotation = remember { Animatable(0f) }
    val car2Rotation = remember { Animatable(0f) }
    val carColor = MaterialTheme.colorScheme.primary

    var canvasWidth by remember { mutableStateOf(0f) }

    val pulsateScale by rememberInfiniteTransition(label = "pulsate").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse),
        label = "pulsateScale"
    )

    val laneCenters = List(4) { i -> canvasWidth * ((2 * i + 1) / 8f) }
    val laneColor = MaterialTheme.colorScheme.tertiary
    val bgColorPrimary = MaterialTheme.colorScheme.secondary
    val bgColorDark = Color(
        (bgColorPrimary.red * 0.7f),
        (bgColorPrimary.green * 0.7f),
        (bgColorPrimary.blue * 0.7f)
    )
    val car1TargetX = laneCenters.getOrElse(gameState.car1Lane) { 0f }
    val car2TargetX = laneCenters.getOrElse(gameState.car2Lane) { 0f }

    // Screen shake offset
    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    LaunchedEffect(gameState.screenShakeActive) {
        if (gameState.screenShakeActive) {
            repeat(6) {
                shakeOffsetX.animateTo(Random.nextFloat() * 16f - 8f, tween(50))
                shakeOffsetY.animateTo(Random.nextFloat() * 16f - 8f, tween(50))
            }
            shakeOffsetX.animateTo(0f, tween(50))
            shakeOffsetY.animateTo(0f, tween(50))
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.fillMaxSize()) {
        LaunchCarAnimation(car1X, car1Rotation, gameState.car1Lane, car1TargetX, canvasWidth)
        LaunchCarAnimation(car2X, car2Rotation, gameState.car2Lane, car2TargetX, canvasWidth)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bgColorDark, bgColorPrimary, bgColorDark)
                    )
                )
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val touches = mutableListOf<PointerInputChange>()
                        while (true) {
                            val event = awaitPointerEvent()
                            val downPointers = event.changes.filter { it.changedToDown() }
                            if (downPointers.isNotEmpty()) {
                                touches.addAll(downPointers)
                            }
                            if (event.changes.all { it.changedToUp() || it.isConsumed }) break
                        }

                        val tappedLanes = touches.map { it.position.x / (size.width / 4) }

                        if (tappedLanes.any { it < 2 }) {
                            viewModel.onEvent(GameEvent.OnSwitchLane(1))
                        }
                        if (tappedLanes.any { it >= 2 }) {
                            viewModel.onEvent(GameEvent.OnSwitchLane(2))
                        }
                    }
                }
        ) {
            canvasWidth = size.width
            val canvasHeight = size.height
            val laneWidth = canvasWidth / 4f

            // Apply screen shake
            val offsetX = shakeOffsetX.value
            val offsetY = shakeOffsetY.value

            // Draw lane dividers with glow
            for (i in 1 until 4) {
                val lineX = i * laneWidth + offsetX
                // Glow
                drawLine(
                    laneColor.copy(alpha = 0.15f),
                    Offset(lineX, 0f),
                    Offset(lineX, canvasHeight),
                    strokeWidth = 12f
                )
                // Main line
                drawLine(
                    laneColor.copy(alpha = 0.6f),
                    Offset(lineX, 0f),
                    Offset(lineX, canvasHeight),
                    strokeWidth = 2f
                )
            }

            val scale = 0.5f
            val carWidth = carBitmap.width * scale
            val carHeight = carBitmap.height * scale
            val bottomOffsetFraction = 0.1f
            val carY = canvasHeight * (1f - bottomOffsetFraction) - carHeight

            val car1Rect = with(Car) {
                drawCar(
                    car1X.value + offsetX,
                    car1Rotation.value,
                    carBitmap,
                    carY + offsetY,
                    carWidth,
                    carHeight,
                    carColor
                )
            }
            val car2Rect = with(Car) {
                drawCar(
                    car2X.value + offsetX,
                    car2Rotation.value,
                    carBitmap,
                    carY + offsetY,
                    carWidth,
                    carHeight,
                    carColor
                )
            }

            drawShapes(
                shapes = gameState.shapes,
                laneCenters = laneCenters,
                canvasHeight = canvasHeight,
                pulsateScale = pulsateScale,
                gameState = gameState,
                car1Rect = car1Rect,
                car2Rect = car2Rect,
                coroutineScope = coroutineScope,
                viewModel = viewModel
            )

            // Draw particles
            gameState.particles.forEach { particle ->
                if (particle.isAlive) {
                    drawCircle(
                        color = particle.color.copy(alpha = particle.currentAlpha),
                        radius = particle.size,
                        center = Offset(
                            particle.position.x + canvasWidth / 2,
                            particle.position.y * canvasHeight
                        )
                    )
                }
            }

            // Draw score popups
            gameState.scorePopups.forEach { popup ->
                if (popup.isAlive) {
                    val yOffset = popup.position.y * canvasHeight - (popup.progress * 80f)
                    val alpha = 1f - popup.progress
                    val textResult = textMeasurer.measure(
                        popup.text,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = popup.color.copy(alpha = alpha)
                        )
                    )
                    drawText(
                        textResult,
                        topLeft = Offset(
                            canvasWidth / 2 - textResult.size.width / 2,
                            yOffset
                        )
                    )
                }
            }

            // Draw near-miss events
            gameState.nearMissEvents.forEach { event ->
                if (event.isAlive) {
                    val alpha = 1f - event.progress
                    val yOff = event.position.y - (event.progress * 60f)
                    val textResult = textMeasurer.measure(
                        "CLOSE!",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800).copy(alpha = alpha)
                        )
                    )
                    drawText(
                        textResult,
                        topLeft = Offset(
                            event.position.x - textResult.size.width / 2,
                            yOff
                        )
                    )
                }
            }
        }

        // HUD overlay
        GameHUD(gameState = gameState)

        if (gameState.isGameOver || gameState.isVictory) {
            GameOverLayout(modifier, gameState, viewModel, navController)
        }
    }
}

@Composable
fun BoxScope.GameHUD(gameState: GameState) {
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // Score
        Text(
            text = "Score: ${gameState.score}",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp)
        )

        // Combo indicator
        if (gameState.comboCount >= 3) {
            Text(
                text = "🔥 ${gameState.comboMultiplier}x COMBO (${gameState.comboCount})",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Timer for timed mode
        if (gameState.gameMode == GameMode.TIMED_CHALLENGE) {
            val seconds = gameState.remainingTimeMs / 1000
            val timerColor = if (seconds <= 10) Color.Red else Color.White
            Text(
                text = "⏱️ ${seconds}s",
                color = timerColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Active power-up indicator (top left)
    if (gameState.activePowerUp != null) {
        val remainingMs = gameState.powerUpExpiryTime - System.currentTimeMillis()
        val displayTime = if (gameState.activePowerUp == PowerUpType.SHIELD) {
            "Active"
        } else {
            "${(remainingMs / 1000).coerceAtLeast(0)}s"
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(gameState.activePowerUp.color).copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⭐",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${gameState.activePowerUp.displayName} $displayTime",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Shield indicator
    if (gameState.shieldActive && gameState.activePowerUp != PowerUpType.SHIELD) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 56.dp)
                .statusBarsPadding()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF4FC3F7).copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛡️ Shield Active",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}