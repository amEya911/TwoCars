package eu.tutorials.twocars.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import eu.tutorials.twocars.ui.component.MenuItem
import eu.tutorials.twocars.util.FirebaseUtils

@Composable
fun GameMenu(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val backgroundColor = Color(0xFFFF1E00)
    val backgrounds = remember { FirebaseUtils.getBackgroundImages(Firebase.remoteConfig) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "Collect & Dodge",
            fontSize = 28.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            contentPadding = PaddingValues(horizontal = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(backgrounds) { background ->
                MenuItem(
                    navController = navController,
                    backgroundUrl = background.url,
                    name = background.name.replace("_", " ").uppercase(),
                    originalName = background.name
                )
            }
        }
    }
}
