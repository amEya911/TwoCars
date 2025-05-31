package eu.tutorials.twocars.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

object Car {
    fun DrawScope.drawCar(
        carX: Float,
        carRotation: Float,
        carBitmap: ImageBitmap,
        carY: Float,
        carWidth: Float,
        carHeight: Float,
        carColor: Color
    ): Rect {
        val x = carX - carWidth / 2
        val rect = Rect(Offset(x, carY), Size(carWidth, carHeight))
        drawIntoCanvas {
            it.save()
            it.rotate(carRotation, carX, carY + carHeight / 2)
            it.drawImageRect(
                carBitmap,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(carBitmap.width, carBitmap.height),
                dstOffset = IntOffset(x.toInt(), carY.toInt()),
                dstSize = IntSize(carWidth.toInt(), carHeight.toInt()),
                paint = Paint().apply { colorFilter = ColorFilter.tint(carColor) }
            )
            it.restore()
        }
        return rect
    }
}
