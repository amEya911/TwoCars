package eu.tutorials.twocars.data.model

data class RemoteBackground(
    val name: String,
    val url: String,
    val displayName: String = name.replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
)
