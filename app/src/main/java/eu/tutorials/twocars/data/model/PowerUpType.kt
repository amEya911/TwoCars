package eu.tutorials.twocars.data.model

enum class PowerUpType(val displayName: String, val durationMs: Long, val color: Long) {
    SHIELD("Shield", 0L, 0xFF4FC3F7),          // One-time use, no duration
    MAGNET("Magnet", 5000L, 0xFFFF7043),        // 5 seconds
    SLOW_MO("Slow-Mo", 4000L, 0xFF81C784),      // 4 seconds
    DOUBLE_POINTS("2x Points", 6000L, 0xFFFFD54F), // 6 seconds
    GHOST("Ghost", 3000L, 0xFFCE93D8)           // 3 seconds
}
