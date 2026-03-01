package eu.tutorials.twocars.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.tutorials.twocars.data.state.GameState
import eu.tutorials.twocars.data.state.MenuState
import eu.tutorials.twocars.ui.component.MenuItem

@Composable
fun GameMenu(
    menuState: MenuState,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val backgroundColor = Color(0xFFFF1E00)
    val backgrounds = menuState.backgrounds

    Log.d("GameMenu", "backgrounds: $backgrounds")

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
                    name = background.displayName,
                    originalName = background.name
                )
            }
        }
    }
}
