package eu.tutorials.twocars.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.navigation.AppScreen

@Composable
fun MenuItem(
    modifier: Modifier = Modifier,
    navController: NavController,
    backgroundUrl: String,
    name: String,
    originalName: String,
    isLocked: Boolean = false,
    unlockRequirement: String? = null,
    gameMode: GameMode = GameMode.ENDLESS
) {
    val cardShape = RoundedCornerShape(
        bottomStart = 30.dp,
        topEnd = 30.dp,
        topStart = 10.dp,
        bottomEnd = 10.dp
    )

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 30.dp),
        shape = cardShape,
        modifier = modifier
            .size(width = 300.dp, height = 450.dp)
            .clickable(enabled = !isLocked) {
                navController.navigate("${AppScreen.GameScreen.route}/$originalName/${gameMode.name}")
            }
            .border(
                BorderStroke(2.dp, if (isLocked) Color.Gray else Color.Black),
                cardShape
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = backgroundUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isLocked) 0.4f else 1f,
                onError = {
                    println("Image failed: $backgroundUrl")
                },
                onSuccess = {
                    println("Image loaded: $backgroundUrl")
                }
            )

            // Locked overlay
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = unlockRequirement ?: "Locked",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Team name at bottom
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                text = name,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}
