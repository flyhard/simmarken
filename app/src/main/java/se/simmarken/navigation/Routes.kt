package se.simmarken.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class ChildCatalog(val kidId: Long)
