package eu.tutorials.twocars.data.state

import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.data.model.NearMissEvent
import eu.tutorials.twocars.data.model.Particle
import eu.tutorials.twocars.data.model.PowerUpType
import eu.tutorials.twocars.data.model.RunStats
import eu.tutorials.twocars.data.model.ScorePopup
import java.util.UUID

data class GameState(
    val shapes: List<Shape> = emptyList(),
    val car1Lane: Int = 1,
    val car2Lane: Int = 2,
    val isGameOver: Boolean = false,
    val gameOverShapeId: String? = null,
    val score: Long = 0,
    val highScore: Long = 0,
    val separationTime: Long,

    // Combo system
    val comboCount: Int = 0,
    val comboMultiplier: Int = 1,
    val lastCollectTimeMs: Long = 0L,
    val comboWindowMs: Long = 2000L,

    // Power-ups
    val activePowerUp: PowerUpType? = null,
    val powerUpExpiryTime: Long = 0L,
    val shieldActive: Boolean = false,

    // Visual effects
    val particles: List<Particle> = emptyList(),
    val scorePopups: List<ScorePopup> = emptyList(),
    val nearMissEvents: List<NearMissEvent> = emptyList(),
    val screenShakeActive: Boolean = false,
    val screenShakeStartTime: Long = 0L,

    // Game mode & timer
    val gameMode: GameMode = GameMode.ENDLESS,
    val remainingTimeMs: Long = 90_000L, // 90 seconds for timed mode
    val isVictory: Boolean = false,

    // Run stats
    val runStats: RunStats = RunStats(),

    // Next particle/popup ID
    val nextEffectId: Int = 0
)

data class Shape(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType,
    val lane: Int,
    val yOffset: Float = 0f,
    val passed: Boolean = false,
    val spawnTimeMillis: Long,
    val nearMissChecked: Boolean = false
)

enum class ShapeType {
    COLLECT, DODGE, POWER_UP
}
