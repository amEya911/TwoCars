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

    // Timing state tracking
    private var timeSinceLastSpawnMs = 0f
    private var timeSinceLastTimerTickMs = 0f

    init {
        viewModelScope.launch {
            dataStore.getData(DataStore.HIGH_SCORE_KEY, 0L).collect { highScore ->
                _gameState.value = _gameState.value.copy(highScore = highScore)
            }
        }
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

    // --- Core Game Loop ---
    fun update(deltaTimeMs: Float) {
        val state = _gameState.value
        if (state.isGameOver || state.isVictory) return

        var nextState = state
        val currentTime = System.currentTimeMillis()

        // 1. Handle Spawning
        timeSinceLastSpawnMs += deltaTimeMs
        val currentSep = getCurrentSeparationTime()
        // We add some variation to spawn timing to make it feel natural
        if (timeSinceLastSpawnMs >= currentSep + Random.nextLong(100)) {
            nextState = handleSpawnShape(nextState, currentTime, currentSep)
            timeSinceLastSpawnMs = 0f
        }

        // 2. Handle Movement Logic
        // duration indicates how long a shape takes to reach the bottom (y=1f)
        val duration = currentSep * 4f 
        val moveDelta = deltaTimeMs / duration
        var isGameOverTriggered = false
        var gameOverShapeId: String? = null

        val updatedShapes = nextState.shapes.mapNotNull { shape ->
            val newY = shape.yOffset + moveDelta

            // Pass check at 0.925
            val hasPassed = newY >= 0.925f
            val justPassed = hasPassed && !shape.nearMissChecked // use this flag internally for checking

            if (justPassed && shape.type == ShapeType.COLLECT && !shape.passed) {
                // If magnet is active, auto-collect
                val isMagnetActive = state.activePowerUp == PowerUpType.MAGNET &&
                        currentTime < state.powerUpExpiryTime
                
                if (isMagnetActive) {
                    handleMarkCirclePassed(GameEvent.OnMarkCirclePassed(shape.id))
                    // State will be updated via flow, next iteration will handle it cleanly
                    // but for this list mapping, we drop the shape since it's collected.
                    return@mapNotNull null
                } else {
                    // Missed collectable
                    isGameOverTriggered = true
                    gameOverShapeId = shape.id
                }
            }

            if (newY >= 1f) {
                // Shape fell off screen
                if (shape.type == ShapeType.COLLECT && !shape.passed && !isGameOverTriggered) {
                     val isMagnetActive = state.activePowerUp == PowerUpType.MAGNET &&
                        currentTime < state.powerUpExpiryTime
                     if (!isMagnetActive) {
                        isGameOverTriggered = true
                        gameOverShapeId = shape.id
                     }
                }
                null // remove from list
            } else {
                shape.copy(
                    yOffset = newY,
                    nearMissChecked = hasPassed
                )
            }
        }

        // 3. Effects & Timers
        val aliveParticles = nextState.particles.filter { it.isAlive }.map {
            val elapsed = currentTime - it.createdAt
            it.copy(
                position = Offset(
                    it.position.x + it.velocity.x * (deltaTimeMs / 16f) * 0.05f,
                    it.position.y + it.velocity.y * (deltaTimeMs / 16f) * 0.05f
                )
            )
        }

        val alivePopups = nextState.scorePopups.filter { it.isAlive }
        val aliveNearMiss = nextState.nearMissEvents.filter { it.isAlive }

        val shakeActive = nextState.screenShakeActive &&
                currentTime - nextState.screenShakeStartTime < 300L

        val powerUpExpired = nextState.activePowerUp != null &&
                nextState.activePowerUp != PowerUpType.SHIELD &&
                currentTime >= nextState.powerUpExpiryTime

        val comboExpired = nextState.comboCount > 0 &&
                currentTime - nextState.lastCollectTimeMs > nextState.comboWindowMs

        nextState = nextState.copy(
            shapes = updatedShapes,
            particles = aliveParticles,
            scorePopups = alivePopups,
            nearMissEvents = aliveNearMiss,
            screenShakeActive = shakeActive,
            activePowerUp = if (powerUpExpired) null else nextState.activePowerUp,
            powerUpExpiryTime = if (powerUpExpired) 0L else nextState.powerUpExpiryTime,
            comboCount = if (comboExpired) 0 else nextState.comboCount,
            comboMultiplier = if (comboExpired) 1 else nextState.comboMultiplier
        )
        
        // 4. Game clock for Timed Mode
        if (nextState.gameMode == GameMode.TIMED_CHALLENGE) {
            timeSinceLastTimerTickMs += deltaTimeMs
            if (timeSinceLastTimerTickMs >= 1000f) {
                timeSinceLastTimerTickMs -= 1000f
                val newTime = nextState.remainingTimeMs - 1000L
                if (newTime <= 0L) {
                    handleVictory(nextState)
                    return // State is updated inside handleVictory
                } else {
                    nextState = nextState.copy(remainingTimeMs = newTime)
                }
            }
        }

        _gameState.value = nextState

        if (isGameOverTriggered && gameOverShapeId != null) {
             onEvent(GameEvent.OnGameOver(gameOverShapeId!!))
        }
    }


    fun onEvent(event: GameEvent) {
        // Certain events can still be handled identically
        when (event) {
            // Deprecated events no longer used natively by UI, replaced by update loop:
            is GameEvent.OnSpawnShape -> {}
            is GameEvent.OnUpdateShapePosition -> {}
            is GameEvent.OnUpdateEffects -> {}
            is GameEvent.OnTimerTick -> {}
            
            is GameEvent.OnRemoveShape -> handleRemoveShape(event)
            is GameEvent.OnSwitchLane -> handleSwitchLane(event)
            is GameEvent.OnGameOver -> handleGameOver(event)
            is GameEvent.OnMarkCirclePassed -> handleMarkCirclePassed(event)
            is GameEvent.OnResetGame -> handleResetGame()
            is GameEvent.OnCollectPowerUp -> handleCollectPowerUp(event)
            is GameEvent.OnNearMiss -> handleNearMiss(event)
            is GameEvent.OnSetGameMode -> setGameMode(event.mode)
        }
    }

    private fun handleSpawnShape(state: GameState, currentTime: Long, currentSep: Long): GameState {
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
            return state.copy(
                shapes = state.shapes + shape
            )
        }
        return state
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

        if (state.shieldActive) {
            _gameState.value = state.copy(
                shieldActive = false,
                activePowerUp = null,
                shapes = state.shapes.filterNot { it.id == event.shapeId }
            )
            spawnParticles(0f, 0f, Color(0xFF4FC3F7), 8)
            return
        }

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

        processEndGameStats(state, isVictory = false, gameOverShapeId = event.shapeId)
    }

    private fun handleVictory(state: GameState) {
        processEndGameStats(state, isVictory = true, gameOverShapeId = null)
    }

    private fun processEndGameStats(state: GameState, isVictory: Boolean, gameOverShapeId: String?) {
        val currentScore = state.score
        val survivalTime = System.currentTimeMillis() - state.runStats.startTimeMs
        val updatedStats = state.runStats.copy(survivalTimeMs = survivalTime)

        viewModelScope.launch {
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
            isGameOver = !isVictory,
            isVictory = isVictory,
            gameOverShapeId = gameOverShapeId,
            remainingTimeMs = if (isVictory) 0L else state.remainingTimeMs,
            screenShakeActive = !isVictory,
            screenShakeStartTime = System.currentTimeMillis(),
            runStats = updatedStats
        )
    }

    private fun handleMarkCirclePassed(event: GameEvent.OnMarkCirclePassed) {
        val state = _gameState.value
        val currentTime = System.currentTimeMillis()

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

        val newParticles = if (shape != null) {
            state.particles + generateParticles(
                newId + 1, 0f, shape.yOffset, Color.Red, 6
            )
        } else state.particles

        _gameState.value = state.copy(
            shapes = state.shapes.filterNot { it.id == event.id },
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

    private fun handleResetGame() {
        timeSinceLastSpawnMs = 0f
        timeSinceLastTimerTickMs = 0f
        _gameState.value = GameState(
            highScore = _gameState.value.highScore,
            separationTime = baseSeparationTime,
            gameMode = _gameState.value.gameMode,
            remainingTimeMs = if (_gameState.value.gameMode == GameMode.TIMED_CHALLENGE) 90_000L else 0L
        )
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

        val teamUnlocks = mapOf(
            "cadillac" to 0L,         
            "audi" to 100L,           
            "williams" to 250L,       
            "racing_bulls" to 500L,   
            "haas" to 800L,           
            "alpine" to 1200L,        
            "aston_martin" to 1800L,  
            "mercedes" to 2500L,      
            "red_bull" to 3500L,      
            "ferrari" to 5000L,       
            "mclaren" to 7500L        
        )

        teamUnlocks.forEach { (teamId, threshold) ->
            if (cumulativeScore >= threshold) {
                dataStore.addToStringSet(DataStore.UNLOCKED_TEAMS_KEY, teamId)
            }
        }
    }
}
