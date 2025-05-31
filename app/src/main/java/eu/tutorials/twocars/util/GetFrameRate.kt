package eu.tutorials.twocars.util

import android.app.Activity

fun Activity.getFrameDelay(): Long {
    val refreshRate = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        display?.refreshRate ?: 60f
    } else {
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay?.refreshRate ?: 60f
    }

    return when {
        refreshRate >= 119f -> 8L
        refreshRate >= 89f -> 11L
        else -> 16L
    }
}
