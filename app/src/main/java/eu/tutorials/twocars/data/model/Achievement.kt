package eu.tutorials.twocars.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val condition: AchievementCondition
)

sealed class AchievementCondition {
    data class ScoreThreshold(val score: Long) : AchievementCondition()
    data class ComboThreshold(val combo: Int) : AchievementCondition()
    data class NearMissCount(val count: Int) : AchievementCondition()
    data class SurviveSeconds(val seconds: Int) : AchievementCondition()
    data class CollectPowerUps(val count: Int) : AchievementCondition()
    data class TotalScore(val cumulativeScore: Long) : AchievementCondition()
}

object Achievements {
    val ALL = listOf(
        Achievement("first_lap", "First Lap", "Score 10 points", "🏁", AchievementCondition.ScoreThreshold(10)),
        Achievement("getting_started", "Getting Started", "Score 25 points", "🚦", AchievementCondition.ScoreThreshold(25)),
        Achievement("half_century", "Half Century", "Score 50 points", "🏅", AchievementCondition.ScoreThreshold(50)),
        Achievement("century", "Century", "Score 100 points", "💯", AchievementCondition.ScoreThreshold(100)),
        Achievement("speed_demon", "Speed Demon", "Score 200 points", "👹", AchievementCondition.ScoreThreshold(200)),
        Achievement("legend", "Legend", "Score 500 points", "🏆", AchievementCondition.ScoreThreshold(500)),
        Achievement("combo_starter", "Combo Starter", "Get a 3x combo", "🔥", AchievementCondition.ComboThreshold(3)),
        Achievement("combo_master", "Combo Master", "Get a 6x combo", "⚡", AchievementCondition.ComboThreshold(6)),
        Achievement("combo_king", "Combo King", "Get a 10x combo", "👑", AchievementCondition.ComboThreshold(10)),
        Achievement("close_call", "Close Call", "Get 5 near misses in one run", "😰", AchievementCondition.NearMissCount(5)),
        Achievement("daredevil", "Daredevil", "Get 20 near misses in one run", "🤪", AchievementCondition.NearMissCount(20)),
        Achievement("survivor", "Survivor", "Survive for 60 seconds", "⏱️", AchievementCondition.SurviveSeconds(60)),
        Achievement("endurance", "Endurance", "Survive for 120 seconds", "💪", AchievementCondition.SurviveSeconds(120)),
        Achievement("powered_up", "Powered Up", "Collect 3 power-ups in one run", "⭐", AchievementCondition.CollectPowerUps(3)),
        Achievement("collector", "Collector", "Reach 1000 total cumulative score", "🎯", AchievementCondition.TotalScore(1000)),
        Achievement("grinder", "Grinder", "Reach 5000 total cumulative score", "💎", AchievementCondition.TotalScore(5000)),
    )

    fun getById(id: String): Achievement? = ALL.find { it.id == id }
}
