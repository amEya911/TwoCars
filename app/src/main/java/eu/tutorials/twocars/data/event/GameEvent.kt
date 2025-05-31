package eu.tutorials.twocars.data.event

sealed class GameEvent {
    data object OnSpawnShape : GameEvent()
    data class OnUpdateShapePosition(val id: String, val offset: Float) : GameEvent()
    data class OnRemoveShape(val id: String) : GameEvent()
    data class OnSwitchLane(val carNumber: Int) : GameEvent() // carNumber: 1 or 2
    data class OnGameOver(val shapeId: String) : GameEvent()
    data class OnMarkCirclePassed(val id: String) : GameEvent()
    data object OnResetGame: GameEvent()
}
