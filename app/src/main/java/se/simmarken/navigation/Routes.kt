package se.simmarken.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object Settings

@Serializable
data class ChildCatalog(val kidId: Long)

@Serializable
data class BadgeDetail(val kidId: Long, val badgeId: Long)
