package eu.tutorials.twocars.data.model

/**
 * Stable internal identifiers for all 2026 F1 teams.
 * The [id] is used as the internal key throughout the app (navigation, themes, Remote Config).
 * The [defaultDisplayName] is the fallback display name when Remote Config has no override.
 */
enum class F1Team(val id: String, val defaultDisplayName: String) {
    MCLAREN("mclaren", "McLaren"),
    RED_BULL("red_bull", "Oracle Red Bull Racing"),
    MERCEDES("mercedes", "Mercedes-AMG Petronas"),
    FERRARI("ferrari", "Scuderia Ferrari"),
    ASTON_MARTIN("aston_martin", "Aston Martin Aramco"),
    ALPINE("alpine", "BWT Alpine"),
    WILLIAMS("williams", "Williams Racing"),
    HAAS("haas", "MoneyGram Haas F1 Team"),
    RACING_BULLS("racing_bulls", "Racing Bulls"),
    AUDI("audi", "Audi F1 Team"),
    CADILLAC("cadillac", "Cadillac F1 Team");

    companion object {
        fun fromId(id: String): F1Team? = entries.find { it.id == id }
    }
}
