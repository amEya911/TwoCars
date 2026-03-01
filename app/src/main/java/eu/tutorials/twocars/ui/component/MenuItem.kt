package eu.tutorials.twocars.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import eu.tutorials.twocars.navigation.AppScreen

@Composable
fun MenuItem(
    modifier: Modifier = Modifier,
    navController: NavController,
    backgroundUrl: String,
    name: String,
    originalName: String
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 30.dp),
        shape = RoundedCornerShape(
            bottomStart = 30.dp,
            topEnd = 30.dp,
            topStart = 10.dp,
            bottomEnd = 10.dp
        ),
        modifier = modifier
            .size(width = 300.dp, height = 450.dp)
            .clickable {
                navController.navigate("${AppScreen.GameScreen.route}/$originalName")
            }
            .border(
                BorderStroke(2.dp, Color.Black),
                RoundedCornerShape(
                    bottomStart = 30.dp,
                    topEnd = 30.dp,
                    topStart = 10.dp,
                    bottomEnd = 10.dp
                )
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
                onError = {
                    println("Image failed: $backgroundUrl")
                },
                onSuccess = {
                    println("Image loaded: $backgroundUrl")
                }
            )
            Text(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = name,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color.White
            )
        }
    }
}
