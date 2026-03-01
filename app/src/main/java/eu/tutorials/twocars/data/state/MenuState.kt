package eu.tutorials.twocars.data.state

import eu.tutorials.twocars.data.model.RemoteBackground

data class MenuState (
    val backgrounds: List<RemoteBackground> = emptyList()
)