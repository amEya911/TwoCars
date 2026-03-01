package eu.tutorials.twocars.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)
//
//@Composable
//fun TwoCarsTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}

@Composable
fun AppTheme(
    colorScheme: ColorScheme = mercedes,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, true)
//            window.statusBarColor = colorScheme.primary.toArgb()
//            window.navigationBarColor = colorScheme.primary.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

val mcLaren = darkColorScheme(
    primary = Color(0xFFFF8000), // Papaya Orange
    secondary = Color(0xFF141414), // Carbon Black
    tertiary = Color(0xFF00C4B3) // Blue accent
)

val redBull = darkColorScheme(
    primary = Color(0xFF1E41FF), // Racing Blue
    secondary = Color(0xFFDC0000), // Yellow
    tertiary = Color(0xFFFFD700) // Red Bull Red
)

val mercedes = darkColorScheme(
    primary = Color(0xFF00E5C0), // Petronas Green
    secondary = Color(0xFF2B2B2B), // Graphite
    tertiary = Color(0xFFFFFFFF) // Silver/White
)

val ferrari = darkColorScheme(
    primary = Color(0xFFDC0000), // Ferrari Red
    secondary = Color(0xFF000000), // Black
    tertiary = Color(0xFFFFD700) // Yellow
)

val astonMartin = darkColorScheme(
    primary = Color(0xFF006F62), // British Racing Green
    secondary = Color(0xFF000000), // Black
    tertiary = Color(0xFFB6FF00) // Lime accents
)

val alpine = darkColorScheme(
    primary = Color(0xFF0090FF), // Alpine Blue
    secondary = Color(0xFFFFFFFF), // White
    tertiary = Color(0xFFFF4C4C) // Red accent
)

val williams = darkColorScheme(
    primary = Color(0xFF00A3E0), // Williams Blue
    secondary = Color(0xFF002244), // Navy
    tertiary = Color(0xFFFFFFFF) // White
)

val haas = darkColorScheme(
    primary = Color(0xFFFF1E00), // Haas Red
    secondary = Color(0xFF2E2E2E), // Dark Grey
    tertiary = Color(0xFFFFFFFF) // White
)

val racingBulls = darkColorScheme(
    primary = Color(0xFF6D28D9), // Purple/Indigo
    secondary = Color(0xFF141414), // Deep Black
    tertiary = Color(0xFFFFFFFF) // White
)

val audi = darkColorScheme(
    primary = Color(0xFFBB0A1E), // Audi Red
    secondary = Color(0xFF000000), // Black
    tertiary = Color(0xFFD9D9D9) // Silver
)

val cadillac = darkColorScheme(
    primary = Color(0xFF1A1A2E), // Dark Navy
    secondary = Color(0xFFC8A951), // Gold
    tertiary = Color(0xFFFFFFFF) // White
)

enum class GameBackground(val url: String) {
    MCLAREN("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/mcLaren1.png?alt=media&token=4c837568-c157-4bdd-a669-67e805deddd6"),
    RED_BULL("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/redbul1.jpg?alt=media&token=77e710ba-bf96-4fbd-b9e0-48ae8c013761"),
    MERCEDES("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/mercedes1.jpg?alt=media&token=8a927f21-27f5-4578-887c-de4c64524e99"),
    FERRARI("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/ferrari1.jpg?alt=media&token=ae093a04-6d1e-4bf9-9d93-60dc38521da9"),
    ASTON_MARTIN("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/astonMartin1.jpg?alt=media&token=b6c332e7-0514-4a2e-96e8-90d107b11a7f"),
    ALPINE("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/alpine1.jpg?alt=media&token=c1b9d7cc-b45a-4fc7-9ce8-a4f926e67a16"),
    WILLIAMS("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/williams1.jpg?alt=media&token=6716eaba-8b5c-4e66-bf86-a46250ba21e1"),
    HAAS("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/haas1.jpg?alt=media&token=b567d33e-6499-4fe0-ad52-1204523ecb61"),
    RACING_BULLS("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/visaCashAppRB1.jpg?alt=media&token=37ac03d5-83fd-473c-973e-41eb72799559"),
    AUDI("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/alphaRomeo1.jpg?alt=media&token=d36ebca7-b516-4e72-8f79-b09dfd6eb40b"),
    CADILLAC("https://firebasestorage.googleapis.com/v0/b/collectdodge.firebasestorage.app/o/alphaRomeo1.jpg?alt=media&token=d36ebca7-b516-4e72-8f79-b09dfd6eb40b")
}

fun getGameTheme(gameId: String?): ColorScheme {
    return when (gameId) {
        GameBackground.MCLAREN.name -> mcLaren
        GameBackground.RED_BULL.name -> redBull
        GameBackground.MERCEDES.name -> mercedes
        GameBackground.FERRARI.name -> ferrari
        GameBackground.ASTON_MARTIN.name -> astonMartin
        GameBackground.ALPINE.name -> alpine
        GameBackground.WILLIAMS.name -> williams
        GameBackground.HAAS.name -> haas
        GameBackground.RACING_BULLS.name -> racingBulls
        GameBackground.AUDI.name -> audi
        GameBackground.CADILLAC.name -> cadillac
        else -> mercedes
    }
}
