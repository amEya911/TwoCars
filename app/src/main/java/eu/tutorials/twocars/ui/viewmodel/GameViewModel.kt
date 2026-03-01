package eu.tutorials.twocars.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.twocars.data.datasource.local.DataStore
import eu.tutorials.twocars.data.event.GameEvent
import eu.tutorials.twocars.data.state.GameState
import eu.tutorials.twocars.data.state.Shape
import eu.tutorials.twocars.data.state.ShapeType
import eu.tutorials.twocars.util.FirebaseUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class GameViewModel @Inject constructor(
    private val dataStore: DataStore,
    remoteConfig: FirebaseRemoteConfig
) : ViewModel() {

    private val separationTime1 =
        FirebaseUtils.getSeparationTimeConfig(remoteConfig).separationTime

    private val _gameState = MutableStateFlow(GameState(separationTime = separationTime1))
    val gameState: StateFlow<GameState> = _gameState

    init {
        viewModelScope.launch {
            dataStore.getData(DataStore.HIGH_SCORE_KEY, 0L).collect { highScore ->
                _gameState.value = _gameState.value.copy(highScore = highScore)
            }
        }
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                if (!_gameState.value.isGameOver) {
                    onEvent(GameEvent.OnSpawnShape)
                }
                delay(Random.nextLong(100, separationTime1))
            }
        }
    }

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.OnSpawnShape -> {
                val currentTime = System.currentTimeMillis()

                val blockedLanes = _gameState.value.shapes
                    .filter { currentTime - it.spawnTimeMillis <= _gameState.value.separationTime }
                    .flatMap {
                        when (it.lane) {
                            0, 1 -> listOf(0, 1)
                            2, 3 -> listOf(2, 3)
                            else -> emptyList()
                        }
                    }.toSet()

                val availableLanes = (0..3).filterNot { it in blockedLanes }

                if (availableLanes.isNotEmpty()) {
                    val chosenLane = availableLanes.random()
                    val shape = Shape(
                        type = if (Random.nextBoolean()) ShapeType.COLLECT else ShapeType.DODGE,
                        lane = chosenLane,
                        spawnTimeMillis = currentTime
                    )
                    _gameState.value = _gameState.value.copy(
                        shapes = _gameState.value.shapes + shape
                    )
                    animateShapeDown(shape)
                }
            }

            is GameEvent.OnUpdateShapePosition -> {
                _gameState.value = _gameState.value.copy(
                    shapes = _gameState.value.shapes.map {
                        if (it.id == event.id) it.copy(yOffset = event.offset) else it
                    }
                )
            }

            is GameEvent.OnRemoveShape -> {
                _gameState.value = _gameState.value.copy(
                    shapes = _gameState.value.shapes.filterNot { it.id == event.id }
                )
            }

            is GameEvent.OnSwitchLane -> {
                if (_gameState.value.isGameOver) return

                _gameState.value = _gameState.value.copy(
                    car1Lane = if (event.carNumber == 1) {
                        if (_gameState.value.car1Lane == 0) 1 else 0
                    } else _gameState.value.car1Lane,
                    car2Lane = if (event.carNumber == 2) {
                        if (_gameState.value.car2Lane == 2) 3 else 2
                    } else _gameState.value.car2Lane
                )
            }

            is GameEvent.OnGameOver -> {
                val currentScore = _gameState.value.score

                viewModelScope.launch {
                    val savedHighScore = dataStore.getData(DataStore.HIGH_SCORE_KEY, 0L).first()
                    if (currentScore > savedHighScore) {
                        dataStore.saveData(DataStore.HIGH_SCORE_KEY, currentScore)
                        _gameState.value = _gameState.value.copy(highScore = currentScore)
                    }
                }

                _gameState.value = _gameState.value.copy(
                    isGameOver = true,
                    gameOverShapeId = event.shapeId
                )
            }

            is GameEvent.OnMarkCirclePassed -> {
                val newScore = _gameState.value.score + 1
                val newSeparationTime = when {
                    newScore >= 200 -> 400L
                    newScore >= 100 -> 500L
                    else -> _gameState.value.separationTime
                }

                _gameState.value = _gameState.value.copy(
                    shapes = _gameState.value.shapes.map {
                        if (it.id == event.id) it.copy(passed = true) else it
                    },
                    score = newScore,
                    separationTime = newSeparationTime
                )
            }

            GameEvent.OnResetGame -> {
                _gameState.value = GameState(highScore = _gameState.value.highScore, separationTime = separationTime1)
            }
        }
    }

    private fun animateShapeDown(shape: Shape) {
        viewModelScope.launch {
            var previousTime = System.currentTimeMillis()
            var currentProgress = 0f
            var isMissChecked = false

            while (currentProgress < 1f && !_gameState.value.isGameOver) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - previousTime).toFloat()

                val duration = _gameState.value.separationTime * 4
                currentProgress += deltaTime / duration
                currentProgress = currentProgress.coerceIn(0f, 1f)

                onEvent(
                    GameEvent.OnUpdateShapePosition(
                        id = shape.id,
                        offset = currentProgress
                    )
                )

                if (currentProgress >= 0.925f && !isMissChecked) {
                    isMissChecked = true
                    val currentShape = _gameState.value.shapes.find { it.id == shape.id }
                    if (currentShape != null && currentShape.type == ShapeType.COLLECT && !currentShape.passed) {
                        onEvent(GameEvent.OnGameOver(shape.id))
                        break
                    }
                }

                previousTime = currentTime
                delay(16)
            }

            if (currentProgress >= 1f) {
                val finalShape = _gameState.value.shapes.find { it.id == shape.id }
                if (finalShape != null) {
                    if (finalShape.type == ShapeType.COLLECT && !finalShape.passed) {
                        onEvent(GameEvent.OnGameOver(shape.id))
                    }
                    onEvent(GameEvent.OnRemoveShape(shape.id))
                }
            }
        }
    }
}

