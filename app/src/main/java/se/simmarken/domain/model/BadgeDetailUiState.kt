package se.simmarken.domain.model

data class BadgeDetailUiState(
    val nameSv: String = "",
    val imageAssetPath: String? = null,
    val categoryCode: String = "",
    val visualState: BadgeVisualState = BadgeVisualState.LOCKED,
    val progressFraction: Float = 0f,
    val achievedCount: Int = 0,
    val totalRequirements: Int = 0,
    val badgeMissing: Boolean = false,
    val isLoading: Boolean = true,
)
