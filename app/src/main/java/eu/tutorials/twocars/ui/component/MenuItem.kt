package eu.tutorials.twocars.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        val width: Float
        val height: Float

        with(LocalDensity.current) {
            width = 300.dp.toPx()
            height = 450.dp.toPx()
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = "$backgroundUrl/${width.toInt()}x${height.toInt()}",
                contentDescription = "menu background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
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
