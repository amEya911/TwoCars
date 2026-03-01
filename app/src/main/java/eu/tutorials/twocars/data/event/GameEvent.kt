package eu.tutorials.twocars.data.event

import eu.tutorials.twocars.data.model.GameMode
import eu.tutorials.twocars.data.model.PowerUpType

sealed class GameEvent {
    data object OnSpawnShape : GameEvent()
    data class OnUpdateShapePosition(val id: String, val offset: Float) : GameEvent()
    data class OnRemoveShape(val id: String) : GameEvent()
    data class OnSwitchLane(val carNumber: Int) : GameEvent()
    data class OnGameOver(val shapeId: String) : GameEvent()
    data class OnMarkCirclePassed(val id: String) : GameEvent()
    data object OnResetGame : GameEvent()
    data class OnCollectPowerUp(val id: String, val powerUpType: PowerUpType) : GameEvent()
    data class OnNearMiss(val shapeId: String, val x: Float, val y: Float) : GameEvent()
    data class OnSetGameMode(val mode: GameMode) : GameEvent()
    data object OnTimerTick : GameEvent()
    data object OnUpdateEffects : GameEvent()
}
