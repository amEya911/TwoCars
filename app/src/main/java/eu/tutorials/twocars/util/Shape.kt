package eu.tutorials.twocars.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import eu.tutorials.twocars.data.event.GameEvent
import eu.tutorials.twocars.data.model.PowerUpType
import eu.tutorials.twocars.data.state.GameState
import eu.tutorials.twocars.data.state.Shape
import eu.tutorials.twocars.data.state.ShapeType
import eu.tutorials.twocars.ui.viewmodel.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

object Shape {

    fun DrawScope.drawShapes(
        shapes: List<Shape>,
        laneCenters: List<Float>,
        canvasHeight: Float,
        pulsateScale: Float,
        gameState: GameState,
        car1Rect: Rect,
        car2Rect: Rect,
        coroutineScope: CoroutineScope,
        viewModel: GameViewModel
    ) {
        shapes.forEach { shape ->
            val shapeX = laneCenters.getOrNull(shape.lane) ?: return@forEach
            val shapeY = shape.yOffset * canvasHeight
            val isGameOverShape = shape.id == gameState.gameOverShapeId
            val scaleFactor = if (isGameOverShape) pulsateScale else 1f

            // Ghost mode: make dodge shapes semi-transparent
            val isGhostActive = gameState.activePowerUp == PowerUpType.GHOST &&
                    System.currentTimeMillis() < gameState.powerUpExpiryTime

            when (shape.type) {
                ShapeType.DODGE -> drawSquareShape(
                    shapeX, shapeY, scaleFactor, shape, gameState,
                    car1Rect, car2Rect, viewModel, isGhostActive
                )
                ShapeType.COLLECT -> drawCircleShape(
                    shapeX, shapeY, scaleFactor, shape, gameState,
                    car1Rect, car2Rect, viewModel
                )
                ShapeType.POWER_UP -> drawPowerUpShape(
                    shapeX, shapeY, scaleFactor, shape, gameState,
                    car1Rect, car2Rect, viewModel
                )
            }
        }
    }

    private fun DrawScope.drawSquareShape(
        shapeX: Float,
        shapeY: Float,
        scaleFactor: Float,
        shape: Shape,
        gameState: GameState,
        car1Rect: Rect,
        car2Rect: Rect,
        viewModel: GameViewModel,
        isGhostActive: Boolean
    ) {
        val shapeSize = 80f * scaleFactor
        val rect = Rect(Offset(shapeX - shapeSize / 2, shapeY - shapeSize / 2), Size(shapeSize, shapeSize))
        val alpha = if (isGhostActive) 0.3f else 1f
        drawRect(Color.Cyan.copy(alpha = alpha), rect.topLeft, rect.size)

        if (!gameState.isGameOver && !gameState.isVictory) {
            val car1Hit = shape.lane in 0..1 && car1Rect.overlaps(rect)
            val car2Hit = shape.lane in 2..3 && car2Rect.overlaps(rect)

            if (car1Hit || car2Hit) {
                viewModel.onEvent(GameEvent.OnGameOver(shape.id))
            }

            // Near-miss detection: if square is close but not hitting
            if (!shape.nearMissChecked && !car1Hit && !car2Hit) {
                val nearMissThreshold = 25f
                val carRect = if (shape.lane in 0..1) car1Rect else car2Rect

                val closestX = shapeX.coerceIn(carRect.left, carRect.right)
                val closestY = shapeY.coerceIn(carRect.top, carRect.bottom)
                val dist = Offset(closestX - shapeX, closestY - shapeY).getDistance()

                if (dist < nearMissThreshold + shapeSize / 2 && dist > 0 && shape.yOffset > 0.6f) {
                    viewModel.onEvent(GameEvent.OnNearMiss(shape.id, shapeX, shapeY))
                    // Mark as checked to avoid duplicate near misses by piggybacking on yOffset event for now
                    // In a purer architecture this would set a flag without updating full offset, 
                    // but reusing this event works perfectly fine.
                    viewModel.onEvent(GameEvent.OnUpdateShapePosition(shape.id, shape.yOffset))
                }
            }
        }
    }

    private fun DrawScope.drawCircleShape(
        shapeX: Float,
        shapeY: Float,
        scaleFactor: Float,
        shape: Shape,
        gameState: GameState,
        car1Rect: Rect,
        car2Rect: Rect,
        viewModel: GameViewModel
    ) {
        val radius = 40f * scaleFactor
        val center = Offset(shapeX, shapeY)

        // Draw circle
        drawCircle(Color.Red, radius, center)

        // Magnet effect: auto-collect when close
        val magnetActive = gameState.activePowerUp == PowerUpType.MAGNET &&
                System.currentTimeMillis() < gameState.powerUpExpiryTime
        val collectRadius = if (magnetActive) radius * 3f else radius

        if (!gameState.isGameOver && !gameState.isVictory && !shape.passed) {
            val checkCollision = { carRect: Rect ->
                val closest = Offset(
                    center.x.coerceIn(carRect.left, carRect.right),
                    center.y.coerceIn(carRect.top, carRect.bottom)
                )
                closest.minus(center).getDistance() < collectRadius
            }

            if ((shape.lane in 0..1 && checkCollision(car1Rect)) ||
                (shape.lane in 2..3 && checkCollision(car2Rect))
            ) {
                viewModel.onEvent(GameEvent.OnMarkCirclePassed(shape.id))
            }
        }
    }

    private fun DrawScope.drawPowerUpShape(
        shapeX: Float,
        shapeY: Float,
        scaleFactor: Float,
        shape: Shape,
        gameState: GameState,
        car1Rect: Rect,
        car2Rect: Rect,
        viewModel: GameViewModel
    ) {
        val starSize = 35f * scaleFactor

        // Pick a random power-up type based on shape ID hash for consistency
        val powerUpTypes = PowerUpType.entries
        val powerUpType = powerUpTypes[shape.id.hashCode().let { if (it < 0) -it else it } % powerUpTypes.size]

        val color = Color(powerUpType.color)

        // Draw a star shape
        drawStar(shapeX, shapeY, starSize, color)

        // Draw glow
        drawCircle(color.copy(alpha = 0.2f), starSize * 1.8f, Offset(shapeX, shapeY))

        if (!gameState.isGameOver && !gameState.isVictory) {
            val checkCollision = { carRect: Rect ->
                val closest = Offset(
                    shapeX.coerceIn(carRect.left, carRect.right),
                    shapeY.coerceIn(carRect.top, carRect.bottom)
                )
                closest.minus(Offset(shapeX, shapeY)).getDistance() < starSize * 1.5f
            }

            if ((shape.lane in 0..1 && checkCollision(car1Rect)) ||
                (shape.lane in 2..3 && checkCollision(car2Rect))
            ) {
                viewModel.onEvent(GameEvent.OnCollectPowerUp(shape.id, powerUpType))
            }
        }
    }

    private fun DrawScope.drawStar(cx: Float, cy: Float, radius: Float, color: Color) {
        val path = Path()
        val points = 5
        val innerRadius = radius * 0.45f

        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = Math.toRadians((i * 360.0 / (points * 2)) - 90.0)
            val x = cx + (r * kotlin.math.cos(angle)).toFloat()
            val y = cy + (r * kotlin.math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(path, color)
    }
}