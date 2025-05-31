package eu.tutorials.twocars.data.state

import java.util.UUID

data class GameState(
    val shapes: List<Shape> = emptyList(),
    val car1Lane: Int = 1, // between 0 and 1
    val car2Lane: Int = 2, // between 2 and 3
    val isGameOver: Boolean = false,
    val gameOverShapeId: String? = null,
    val score: Long = 0,
    val highScore: Long = 0,
    val separationTime: Long
)

data class Shape(
    val id: String = UUID.randomUUID().toString(),
    val type: ShapeType,
    val lane: Int, // 0 for left, 1 for right
    val yOffset: Float = 0f,
    val passed: Boolean = false,
    val spawnTimeMillis: Long
)

enum class ShapeType {
    COLLECT, DODGE
}

