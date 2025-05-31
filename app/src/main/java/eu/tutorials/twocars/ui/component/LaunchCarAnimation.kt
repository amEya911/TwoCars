package eu.tutorials.twocars.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

@Composable
fun LaunchCarAnimation(
    carX: Animatable<Float, *>,
    carRotation: Animatable<Float, *>,
    lane: Int,
    targetX: Float,
    canvasWidth: Float,
) {
    LaunchedEffect(targetX, canvasWidth) {
        if (carX.value != targetX) {
            carRotation.snapTo(0f)

            launch {
                carRotation.animateTo(if (lane % 2 == 0) -37f else 37f, tween(200))
            }

            carX.animateTo(targetX, tween(200))

            launch {
                carRotation.animateTo(0f, tween(100))
            }
        } else {
            carX.snapTo(targetX)
            carRotation.snapTo(0f)
        }
    }
}
