package eu.tutorials.twocars.ui.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.tutorials.twocars.data.datasource.local.DataStore
import eu.tutorials.twocars.data.event.GameEvent
import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.data.model.NearMissEvent
import eu.tutorials.twocars.data.model.Particle
import eu.tutorials.twocars.data.model.PowerUpType
import eu.tutorials.twocars.data.model.RunStats
import eu.tutorials.twocars.data.model.ScorePopup
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

    private val baseSeparationTime =
        FirebaseUtils.getSeparationTimeConfig(remoteConfig).separationTime

    private val _gameState = MutableStateFlow(GameState(separationTime = baseSeparationTime))
    val gameState: StateFlow<GameState> = _gameState

    private var currentTeamId: String? = null
    private var gameModeInitialized = false
    private var timerStarted = false

    init {
        viewModelScope.launch {
            dataStore.getData(DataStore.HIGH_SCORE_KEY, 0L).collect { highScore ->
                _gameState.value = _gameState.value.copy(highScore = highScore)
            }
        }
        startGameLoop()
        startEffectsLoop()
    }

    fun setTeamId(teamId: String?) {
        if (teamId == currentTeamId) return
        currentTeamId = teamId
        if (teamId != null) {
            viewModelScope.launch {
                val teamHighScore = dataStore.getData(DataStore.highScoreKeyForTeam(teamId), 0L).first()
                _gameState.value = _gameState.value.copy(highScore = teamHighScore)
            }
        }
    }

    fun setGameMode(mode: GameMode) {
        if (gameModeInitialized) return
        gameModeInitialized = true
        _gameState.value = _gameState.value.copy(
            gameMode = mode,
            remainingTimeMs = if (mode == GameMode.TIMED_CHALLENGE) 90_000L else 0L
        )
        if (mode == GameMode.TIMED_CHALLENGE) {
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerStarted) return
        timerStarted = true
        viewModelScope.launch {
            while (!_gameState.value.isGameOver && !_gameState.value.isVictory) {
                delay(1000L)
                if (_gameState.value.gameMode == GameMode.TIMED_CHALLENGE) {
                    onEvent(GameEvent.OnTimerTick)
                }
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                if (!_gameState.value.isGameOver && !_gameState.value.isVictory) {
                    onEvent(GameEvent.OnSpawnShape)
                }
                val currentSep = getCurrentSeparationTime()
                delay(Random.nextLong(100, currentSep))
            }
        }
    }

    private fun startEffectsLoop() {
        viewModelScope.launch {
            while (true) {
                delay(50L)
                onEvent(GameEvent.OnUpdateEffects)
            }
        }
    }

    private fun getCurrentSeparationTime(): Long {
        val state = _gameState.value
        val score = state.score
        val base = baseSeparationTime

        val scaledSep = when {
            score >= 500 -> 250L
            score >= 300 -> 300L
            score >= 200 -> 350L
            score >= 150 -> 400L
            score >= 100 -> 450L
            score >= 50 -> 550L
            else -> base
        }

        // Apply slow-mo power-up
        return if (state.activePowerUp == PowerUpType.SLOW_MO &&
            System.currentTimeMillis() < state.powerUpExpiryTime
        ) {
            (scaledSep * 1.5).toLong()
        } else {
            scaledSep
        }
    }

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.OnSpawnShape -> handleSpawnShape()
            is GameEvent.OnUpdateShapePosition -> handleUpdatePosition(event)
            is GameEvent.OnRemoveShape -> handleRemoveShape(event)
            is GameEvent.OnSwitchLane -> handleSwitchLane(event)
            is GameEvent.OnGameOver -> handleGameOver(event)
            is GameEvent.OnMarkCirclePassed -> handleMarkCirclePassed(event)
            is GameEvent.OnResetGame -> handleResetGame()
            is GameEvent.OnCollectPowerUp -> handleCollectPowerUp(event)
            is GameEvent.OnNearMiss -> handleNearMiss(event)
            is GameEvent.OnSetGameMode -> setGameMode(event.mode)
            is GameEvent.OnTimerTick -> handleTimerTick()
            is GameEvent.OnUpdateEffects -> handleUpdateEffects()
        }
    }

    private fun handleSpawnShape() {
        val currentTime = System.currentTimeMillis()
        val state = _gameState.value
        val currentSep = getCurrentSeparationTime()

        val blockedLanes = state.shapes
            .filter { currentTime - it.spawnTimeMillis <= currentSep }
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

            // Determine shape type: 8% power-up, then increasing dodge ratio
            val rand = Random.nextFloat()
            val dodgeRatio = when {
                state.score >= 300 -> 0.65f
                state.score >= 200 -> 0.55f
                state.score >= 100 -> 0.50f
                else -> 0.45f
            }

            val type = when {
                rand < 0.08f -> ShapeType.POWER_UP
                rand < 0.08f + dodgeRatio -> ShapeType.DODGE
                else -> ShapeType.COLLECT
            }

            val shape = Shape(
                type = type,
                lane = chosenLane,
                spawnTimeMillis = currentTime
            )
            _gameState.value = state.copy(
                shapes = state.shapes + shape
            )
            animateShapeDown(shape)
        }
    }

    private fun handleUpdatePosition(event: GameEvent.OnUpdateShapePosition) {
        _gameState.value = _gameState.value.copy(
            shapes = _gameState.value.shapes.map {
                if (it.id == event.id) it.copy(yOffset = event.offset) else it
            }
        )
    }

    private fun handleRemoveShape(event: GameEvent.OnRemoveShape) {
        _gameState.value = _gameState.value.copy(
            shapes = _gameState.value.shapes.filterNot { it.id == event.id }
        )
    }

    private fun handleSwitchLane(event: GameEvent.OnSwitchLane) {
        val state = _gameState.value
        if (state.isGameOver || state.isVictory) return

        _gameState.value = state.copy(
            car1Lane = if (event.carNumber == 1) {
                if (state.car1Lane == 0) 1 else 0
            } else state.car1Lane,
            car2Lane = if (event.carNumber == 2) {
                if (state.car2Lane == 2) 3 else 2
            } else state.car2Lane
        )
    }

    private fun handleGameOver(event: GameEvent.OnGameOver) {
        val state = _gameState.value

        // Shield power-up: absorb hit instead of game over
        if (state.shieldActive) {
            _gameState.value = state.copy(
                shieldActive = false,
                activePowerUp = null,
                shapes = state.shapes.filterNot { it.id == event.shapeId }
            )
            // Spawn shield-break particles
            spawnParticles(0f, 0f, Color(0xFF4FC3F7), 8)
            return
        }

        // Ghost power-up: squares pass through
        if (state.activePowerUp == PowerUpType.GHOST &&
            System.currentTimeMillis() < state.powerUpExpiryTime
        ) {
            val shape = state.shapes.find { it.id == event.shapeId }
            if (shape?.type == ShapeType.DODGE) {
                _gameState.value = state.copy(
                    shapes = state.shapes.filterNot { it.id == event.shapeId }
                )
                return
            }
        }

        val currentScore = state.score
        val survivalTime = System.currentTimeMillis() - state.runStats.startTimeMs
        val updatedStats = state.runStats.copy(survivalTimeMs = survivalTime)

        viewModelScope.launch {
            // Save global high score
            val savedHighScore = dataStore.getData(DataStore.HIGH_SCORE_KEY, 0L).first()
            if (currentScore > savedHighScore) {
                dataStore.saveData(DataStore.HIGH_SCORE_KEY, currentScore)
            }

            // Save per-team high score
            currentTeamId?.let { teamId ->
                val teamHighScore = dataStore.getData(DataStore.highScoreKeyForTeam(teamId), 0L).first()
                if (currentScore > teamHighScore) {
                    dataStore.saveData(DataStore.highScoreKeyForTeam(teamId), currentScore)
                }
            }

            // Add to cumulative score
            dataStore.addCumulativeScore(currentScore)

            // Check and award achievements
            checkAndAwardAchievements(updatedStats, currentScore)

            // Check team unlocks
            checkTeamUnlocks()
        }

        _gameState.value = state.copy(
            isGameOver = true,
            gameOverShapeId = event.shapeId,
            screenShakeActive = true,
            screenShakeStartTime = System.currentTimeMillis(),
            runStats = updatedStats
        )
    }

    private fun handleMarkCirclePassed(event: GameEvent.OnMarkCirclePassed) {
        val state = _gameState.value
        val currentTime = System.currentTimeMillis()

        // Check combo
        val timeSinceLastCollect = currentTime - state.lastCollectTimeMs
        val newComboCount = if (timeSinceLastCollect <= state.comboWindowMs) {
            state.comboCount + 1
        } else {
            1
        }

        val newMultiplier = when {
            newComboCount >= 10 -> 5
            newComboCount >= 6 -> 3
            newComboCount >= 3 -> 2
            else -> 1
        }

        // Apply double points power-up
        val pointMultiplier = if (state.activePowerUp == PowerUpType.DOUBLE_POINTS &&
            currentTime < state.powerUpExpiryTime
        ) {
            newMultiplier * 2
        } else {
            newMultiplier
        }

        val pointsEarned = 1L * pointMultiplier
        val newScore = state.score + pointsEarned

        val newSeparationTime = getCurrentSeparationTime()
        val longestCombo = maxOf(state.runStats.longestCombo, newComboCount)

        // Spawn score popup
        val shape = state.shapes.find { it.id == event.id }
        val newId = state.nextEffectId

        val newPopups = if (shape != null) {
            val popupText = if (pointMultiplier > 1) "+$pointsEarned (${pointMultiplier}x)" else "+1"
            state.scorePopups + ScorePopup(
                id = newId,
                text = popupText,
                position = Offset(0f, shape.yOffset),
                color = if (pointMultiplier > 1) Color(0xFFFFD700) else Color.White
            )
        } else state.scorePopups

        // Spawn collect particles
        val newParticles = if (shape != null) {
            state.particles + generateParticles(
                newId + 1, 0f, shape.yOffset, Color.Red, 6
            )
        } else state.particles

        _gameState.value = state.copy(
            shapes = state.shapes.map {
                if (it.id == event.id) it.copy(passed = true) else it
            },
            score = newScore,
            separationTime = newSeparationTime,
            comboCount = newComboCount,
            comboMultiplier = newMultiplier,
            lastCollectTimeMs = currentTime,
            scorePopups = newPopups,
            particles = newParticles,
            nextEffectId = newId + 10,
            runStats = state.runStats.copy(
                circlesCollected = state.runStats.circlesCollected + 1,
                longestCombo = longestCombo
            )
        )
    }

    private fun handleCollectPowerUp(event: GameEvent.OnCollectPowerUp) {
        val state = _gameState.value
        val currentTime = System.currentTimeMillis()

        val isShield = event.powerUpType == PowerUpType.SHIELD
        val expiryTime = if (isShield) 0L else currentTime + event.powerUpType.durationMs

        // Spawn power-up particles
        val shape = state.shapes.find { it.id == event.id }
        val newId = state.nextEffectId
        val newParticles = if (shape != null) {
            state.particles + generateParticles(
                newId, 0f, shape.yOffset, Color(event.powerUpType.color), 10
            )
        } else state.particles

        val newPopups = state.scorePopups + ScorePopup(
            id = newId + 10,
            text = event.powerUpType.displayName,
            position = Offset(0f, shape?.yOffset ?: 0.5f),
            color = Color(event.powerUpType.color)
        )

        _gameState.value = state.copy(
            activePowerUp = event.powerUpType,
            powerUpExpiryTime = expiryTime,
            shieldActive = if (isShield) true else state.shieldActive,
            shapes = state.shapes.filterNot { it.id == event.id },
            particles = newParticles,
            scorePopups = newPopups,
            nextEffectId = newId + 20,
            runStats = state.runStats.copy(
                powerUpsCollected = state.runStats.powerUpsCollected + 1
            )
        )
    }

    private fun handleNearMiss(event: GameEvent.OnNearMiss) {
        val state = _gameState.value
        val newId = state.nextEffectId

        // Award 2 bonus points for near misses
        val bonusPoints = 2L * state.comboMultiplier

        _gameState.value = state.copy(
            score = state.score + bonusPoints,
            nearMissEvents = state.nearMissEvents + NearMissEvent(
                id = newId,
                position = Offset(event.x, event.y)
            ),
            scorePopups = state.scorePopups + ScorePopup(
                id = newId + 1,
                text = "CLOSE! +$bonusPoints",
                position = Offset(event.x, event.y),
                color = Color(0xFFFF9800)
            ),
            nextEffectId = newId + 2,
            runStats = state.runStats.copy(
                nearMissCount = state.runStats.nearMissCount + 1
            )
        )
    }

    private fun handleTimerTick() {
        val state = _gameState.value
        if (state.gameMode != GameMode.TIMED_CHALLENGE) return

        val newTime = state.remainingTimeMs - 1000L
        if (newTime <= 0L) {
            // Victory!
            val survivalTime = System.currentTimeMillis() - state.runStats.startTimeMs
            val updatedStats = state.runStats.copy(survivalTimeMs = survivalTime)

            viewModelScope.launch {
                val currentScore = state.score
                val savedHighScore = dataStore.getData(DataStore.HIGH_SCORE_KEY, 0L).first()
                if (currentScore > savedHighScore) {
                    dataStore.saveData(DataStore.HIGH_SCORE_KEY, currentScore)
                }
                currentTeamId?.let { teamId ->
                    val teamHighScore = dataStore.getData(DataStore.highScoreKeyForTeam(teamId), 0L).first()
                    if (currentScore > teamHighScore) {
                        dataStore.saveData(DataStore.highScoreKeyForTeam(teamId), currentScore)
                    }
                }
                dataStore.addCumulativeScore(currentScore)
                checkAndAwardAchievements(updatedStats, currentScore)
                checkTeamUnlocks()
            }

            _gameState.value = state.copy(
                isVictory = true,
                remainingTimeMs = 0L,
                runStats = updatedStats
            )
        } else {
            _gameState.value = state.copy(remainingTimeMs = newTime)
        }
    }

    private fun handleUpdateEffects() {
        val state = _gameState.value

        // Clean up expired effects
        val aliveParticles = state.particles.filter { it.isAlive }
        val alivePopups = state.scorePopups.filter { it.isAlive }
        val aliveNearMiss = state.nearMissEvents.filter { it.isAlive }

        // Update particle positions
        val movedParticles = aliveParticles.map {
            val elapsed = System.currentTimeMillis() - it.createdAt
            val t = elapsed / it.lifetimeMs.toFloat()
            it.copy(
                position = Offset(
                    it.position.x + it.velocity.x * 0.05f,
                    it.position.y + it.velocity.y * 0.05f
                )
            )
        }

        // Check if screen shake should end (300ms duration)
        val shakeActive = state.screenShakeActive &&
                System.currentTimeMillis() - state.screenShakeStartTime < 300L

        // Expire power-up
        val currentTime = System.currentTimeMillis()
        val powerUpExpired = state.activePowerUp != null &&
                state.activePowerUp != PowerUpType.SHIELD &&
                currentTime >= state.powerUpExpiryTime

        // Check combo expiry
        val comboExpired = state.comboCount > 0 &&
                currentTime - state.lastCollectTimeMs > state.comboWindowMs

        _gameState.value = state.copy(
            particles = movedParticles,
            scorePopups = alivePopups,
            nearMissEvents = aliveNearMiss,
            screenShakeActive = shakeActive,
            activePowerUp = if (powerUpExpired) null else state.activePowerUp,
            powerUpExpiryTime = if (powerUpExpired) 0L else state.powerUpExpiryTime,
            comboCount = if (comboExpired) 0 else state.comboCount,
            comboMultiplier = if (comboExpired) 1 else state.comboMultiplier
        )
    }

    private fun handleResetGame() {
        _gameState.value = GameState(
            highScore = _gameState.value.highScore,
            separationTime = baseSeparationTime,
            gameMode = _gameState.value.gameMode,
            remainingTimeMs = if (_gameState.value.gameMode == GameMode.TIMED_CHALLENGE) 90_000L else 0L
        )
        if (_gameState.value.gameMode == GameMode.TIMED_CHALLENGE) {
            timerStarted = false
            startTimer()
        }
    }

    private fun animateShapeDown(shape: Shape) {
        viewModelScope.launch {
            var previousTime = System.currentTimeMillis()
            var currentProgress = 0f
            var isMissChecked = false

            while (currentProgress < 1f && !_gameState.value.isGameOver && !_gameState.value.isVictory) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - previousTime).toFloat()

                val duration = getCurrentSeparationTime() * 4
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
                        // If magnet is active, don't penalize for missing — just let it pass
                        val isMagnetActive = _gameState.value.activePowerUp == PowerUpType.MAGNET &&
                                System.currentTimeMillis() < _gameState.value.powerUpExpiryTime
                        if (!isMagnetActive) {
                            onEvent(GameEvent.OnGameOver(shape.id))
                            break
                        }
                    }
                }

                previousTime = currentTime
                delay(16)
            }

            if (currentProgress >= 1f) {
                val finalShape = _gameState.value.shapes.find { it.id == shape.id }
                if (finalShape != null) {
                    if (finalShape.type == ShapeType.COLLECT && !finalShape.passed) {
                        val isMagnetActive = _gameState.value.activePowerUp == PowerUpType.MAGNET &&
                                System.currentTimeMillis() < _gameState.value.powerUpExpiryTime
                        if (!isMagnetActive) {
                            onEvent(GameEvent.OnGameOver(shape.id))
                        }
                    }
                    onEvent(GameEvent.OnRemoveShape(shape.id))
                }
            }
        }
    }

    // --- Helper functions ---

    private fun spawnParticles(x: Float, y: Float, color: Color, count: Int) {
        val state = _gameState.value
        val newId = state.nextEffectId
        val particles = generateParticles(newId, x, y, color, count)
        _gameState.value = state.copy(
            particles = state.particles + particles,
            nextEffectId = newId + count
        )
    }

    private fun generateParticles(startId: Int, x: Float, y: Float, color: Color, count: Int): List<Particle> {
        return (0 until count).map { i ->
            Particle(
                id = startId + i,
                position = Offset(x, y),
                velocity = Offset(
                    Random.nextFloat() * 10f - 5f,
                    Random.nextFloat() * -8f - 2f
                ),
                color = color,
                size = Random.nextFloat() * 6f + 3f,
                lifetimeMs = Random.nextLong(400, 800)
            )
        }
    }

    private suspend fun checkAndAwardAchievements(stats: RunStats, score: Long) {
        val cumulativeScore = dataStore.getData(DataStore.CUMULATIVE_SCORE_KEY, 0L).first()
        val earned = dataStore.getStringSet(DataStore.EARNED_ACHIEVEMENTS_KEY).first()

        val newAchievements = eu.tutorials.twocars.data.model.Achievements.ALL.filter { achievement ->
            achievement.id !in earned && when (val condition = achievement.condition) {
                is eu.tutorials.twocars.data.model.AchievementCondition.ScoreThreshold ->
                    score >= condition.score
                is eu.tutorials.twocars.data.model.AchievementCondition.ComboThreshold ->
                    stats.longestCombo >= condition.combo
                is eu.tutorials.twocars.data.model.AchievementCondition.NearMissCount ->
                    stats.nearMissCount >= condition.count
                is eu.tutorials.twocars.data.model.AchievementCondition.SurviveSeconds ->
                    stats.survivalTimeMs >= condition.seconds * 1000L
                is eu.tutorials.twocars.data.model.AchievementCondition.CollectPowerUps ->
                    stats.powerUpsCollected >= condition.count
                is eu.tutorials.twocars.data.model.AchievementCondition.TotalScore ->
                    cumulativeScore >= condition.cumulativeScore
            }
        }

        newAchievements.forEach { achievement ->
            dataStore.addToStringSet(DataStore.EARNED_ACHIEVEMENTS_KEY, achievement.id)
        }
    }

    private suspend fun checkTeamUnlocks() {
        val cumulativeScore = dataStore.getData(DataStore.CUMULATIVE_SCORE_KEY, 0L).first()

        // Team unlock thresholds based on 2025 Constructors' Championship standings
        // Bottom teams (new/lower standings) = easier to unlock
        // Top teams (championship leaders) = hardest to unlock
        val teamUnlocks = mapOf(
            "cadillac" to 0L,         // New team (11th) — free starter
            "audi" to 100L,           // Was Kick Sauber (10th in 2025)
            "williams" to 250L,       // 9th in 2025
            "racing_bulls" to 500L,   // 8th in 2025
            "haas" to 800L,           // 7th in 2025
            "alpine" to 1200L,        // 6th in 2025
            "aston_martin" to 1800L,  // 5th in 2025
            "mercedes" to 2500L,      // 4th in 2025
            "red_bull" to 3500L,      // 3rd in 2025
            "ferrari" to 5000L,       // 2nd in 2025
            "mclaren" to 7500L        // 1st in 2025 (Champions)
        )

        teamUnlocks.forEach { (teamId, threshold) ->
            if (cumulativeScore >= threshold) {
                dataStore.addToStringSet(DataStore.UNLOCKED_TEAMS_KEY, teamId)
            }
        }
    }
}
