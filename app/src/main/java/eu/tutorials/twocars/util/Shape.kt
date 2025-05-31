package eu.tutorials.twocars.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import eu.tutorials.twocars.data.event.GameEvent
import eu.tutorials.twocars.data.state.GameState
import eu.tutorials.twocars.data.state.Shape
import eu.tutorials.twocars.data.state.ShapeType
import eu.tutorials.twocars.ui.viewmodel.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

            when (shape.type) {
                ShapeType.DODGE -> drawSquareShape(
                    shapeX, shapeY, scaleFactor, shape, gameState, car1Rect, car2Rect, viewModel
                )

                ShapeType.COLLECT -> drawCircleShape(
                    shapeX, shapeY, scaleFactor, shape, gameState, car1Rect, car2Rect, coroutineScope, viewModel
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
        viewModel: GameViewModel
    ) {
        val size = 80f * scaleFactor
        val rect = Rect(Offset(shapeX - size / 2, shapeY - size / 2), Size(size, size))
        drawRect(Color.Cyan, rect.topLeft, rect.size)

        if (!gameState.isGameOver &&
            ((shape.lane in 0..1 && car1Rect.overlaps(rect)) ||
                    (shape.lane in 2..3 && car2Rect.overlaps(rect)))
        ) {
            viewModel.onEvent(GameEvent.OnGameOver(shape.id))
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
        coroutineScope: CoroutineScope,
        viewModel: GameViewModel
    ) {
        val radius = 40f * scaleFactor
        val center = Offset(shapeX, shapeY)
        drawCircle(Color.Red, radius, center)

        if (!gameState.isGameOver && !shape.passed) {
            val checkCollision = { carRect: Rect ->
                val closest = Offset(
                    center.x.coerceIn(carRect.left, carRect.right),
                    center.y.coerceIn(carRect.top, carRect.bottom)
                )
                closest.minus(center).getDistance() < radius
            }

            when {
                shape.lane in 0..1 && checkCollision(car1Rect) -> coroutineScope.launch {
                    viewModel.onEvent(GameEvent.OnMarkCirclePassed(shape.id))
                    viewModel.onEvent(GameEvent.OnRemoveShape(shape.id))
                }

                shape.lane in 2..3 && checkCollision(car2Rect) -> coroutineScope.launch {
                    viewModel.onEvent(GameEvent.OnMarkCirclePassed(shape.id))
                    viewModel.onEvent(GameEvent.OnRemoveShape(shape.id))
                }
            }
        }
    }
}