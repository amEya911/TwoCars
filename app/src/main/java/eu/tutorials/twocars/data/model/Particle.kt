package eu.tutorials.twocars.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Particle(
    val id: Int,
    val position: Offset,
    val velocity: Offset,
    val color: Color,
    val alpha: Float = 1f,
    val size: Float = 6f,
    val lifetimeMs: Long = 600L,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAlive: Boolean
        get() = System.currentTimeMillis() - createdAt < lifetimeMs

    val currentAlpha: Float
        get() {
            val elapsed = System.currentTimeMillis() - createdAt
            return (alpha * (1f - elapsed.toFloat() / lifetimeMs)).coerceIn(0f, 1f)
        }
}

data class ScorePopup(
    val id: Int,
    val text: String,
    val position: Offset,
    val color: Color = Color.White,
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 800L
) {
    val isAlive: Boolean
        get() = System.currentTimeMillis() - createdAt < durationMs

    val progress: Float
        get() = ((System.currentTimeMillis() - createdAt).toFloat() / durationMs).coerceIn(0f, 1f)
}

data class NearMissEvent(
    val id: Int,
    val position: Offset,
    val createdAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 600L
) {
    val isAlive: Boolean
        get() = System.currentTimeMillis() - createdAt < durationMs

    val progress: Float
        get() = ((System.currentTimeMillis() - createdAt).toFloat() / durationMs).coerceIn(0f, 1f)
}

data class RunStats(
    val circlesCollected: Long = 0,
    val longestCombo: Int = 0,
    val nearMissCount: Int = 0,
    val powerUpsCollected: Int = 0,
    val survivalTimeMs: Long = 0,
    val startTimeMs: Long = System.currentTimeMillis()
) {
    val starRating: Int
        get() = when {
            circlesCollected >= 150 -> 3
            circlesCollected >= 75 -> 2
            circlesCollected >= 25 -> 1
            else -> 0
        }
}
