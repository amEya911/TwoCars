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
import androidx.hilt.navigation.compose.hiltViewModel
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.tutorials.twocars.ui.component.LaunchCarAnimation
import eu.tutorials.twocars.util.Car
import eu.tutorials.twocars.util.Shape.drawShapes
import eu.tutorials.twocars.ui.component.GameOverLayout

@Composable
fun Game(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel(),
    navController: NavController,
    gameId: String?
) {
    val gameState = viewModel.gameState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    val carBitmap = ImageBitmap.imageResource(R.drawable.formula_1)
    val car1X = remember { Animatable(0f) }
    val car2X = remember { Animatable(0f) }
    val car1Rotation = remember { Animatable(0f) }
    val car2Rotation = remember { Animatable(0f) }
    val carColor = MaterialTheme.colorScheme.primary


    var canvasWidth by remember { mutableStateOf(0f) }
    val paddingBottom = with(LocalDensity.current) { 64.dp.toPx() }

    val pulsateScale by rememberInfiniteTransition(label = "pulsate").animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse),
        label = "pulsateScale"
    )

    val laneCenters = List(4) { i -> canvasWidth * ((2 * i + 1) / 8f) }
    val laneColor = MaterialTheme.colorScheme.tertiary
    val car1TargetX = laneCenters.getOrElse(gameState.car1Lane) { 0f }
    val car2TargetX = laneCenters.getOrElse(gameState.car2Lane) { 0f }

    Box(modifier = modifier.fillMaxSize()) {
        LaunchCarAnimation(car1X, car1Rotation, gameState.car1Lane, car1TargetX, canvasWidth)
        LaunchCarAnimation(car2X, car2Rotation, gameState.car2Lane, car2TargetX, canvasWidth)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondary)
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

            for (i in 1 until 4) {
                drawLine(
                    laneColor,
                    Offset(i * laneWidth, 0f),
                    Offset(i * laneWidth, canvasHeight),
                    4f
                )
            }

            val scale = 0.5f
            val carWidth = carBitmap.width * scale
            val carHeight = carBitmap.height * scale
            //val carY = canvasHeight - carHeight - paddingBottom
            val bottomOffsetFraction = 0.1f
            val carY = canvasHeight * (1f - bottomOffsetFraction) - carHeight

            val car1Rect = with(Car) {
                drawCar(
                    car1X.value,
                    car1Rotation.value,
                    carBitmap,
                    carY,
                    carWidth,
                    carHeight,
                    carColor
                )
            }
            val car2Rect = with(Car) {
                drawCar(
                    car2X.value,
                    car2Rotation.value,
                    carBitmap,
                    carY,
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
        }

        ScoreHeader(score = gameState.score)

        if (gameState.isGameOver) {
            GameOverLayout(modifier, gameState, viewModel, navController)
        }
    }
}

@Composable
fun BoxScope.ScoreHeader(score: Long) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "Score: $score",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp)
        )
    }
}