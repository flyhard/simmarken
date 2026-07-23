package se.simmarken.domain.model

data class BadgeCellUiModel(
    val id: Long,
    val nameSv: String,
    val imageAssetPath: String?,
    val categoryCode: String,
    val visualState: BadgeVisualState,
    val progressFraction: Float,
    val achievedCount: Int,
    val totalRequirements: Int,
)

data class CategorySection(
    val categoryId: Long,
    val categoryNameSv: String,
    val categoryCode: String,
    val sortOrder: Int,
    val badges: List<BadgeCellUiModel>,
)
