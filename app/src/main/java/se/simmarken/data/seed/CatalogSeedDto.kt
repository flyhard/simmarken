package se.simmarken.data.seed

import kotlinx.serialization.Serializable
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity

@Serializable
data class CatalogSeedDto(
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val catalogVersion: String,
    val sortOrder: Int = 0,
    val categories: List<CategorySeedDto>,
)

@Serializable
data class CategorySeedDto(
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val sortOrder: Int,
    val badges: List<BadgeSeedDto>,
)

@Serializable
data class BadgeSeedDto(
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val sortOrder: Int,
    val imageAssetPath: String? = null,
    val requirements: List<RequirementSeedDto>,
)

@Serializable
data class RequirementSeedDto(
    val code: String,
    val textSv: String,
    val textEn: String,
    val sortOrder: Int,
)

fun CatalogSeedDto.toEntity(existingId: Long = 0): CatalogEntity = CatalogEntity(
    id = existingId,
    code = code,
    nameSv = nameSv,
    nameEn = nameEn,
    catalogVersion = catalogVersion,
    sortOrder = sortOrder,
)

fun CategorySeedDto.toEntity(catalogId: Long, existingId: Long = 0): CategoryEntity = CategoryEntity(
    id = existingId,
    catalogId = catalogId,
    code = code,
    nameSv = nameSv,
    nameEn = nameEn,
    sortOrder = sortOrder,
)

fun BadgeSeedDto.toEntity(categoryId: Long, existingId: Long = 0): BadgeEntity = BadgeEntity(
    id = existingId,
    categoryId = categoryId,
    code = code,
    nameSv = nameSv,
    nameEn = nameEn,
    imageAssetPath = imageAssetPath,
    sortOrder = sortOrder,
)

fun RequirementSeedDto.toEntity(badgeId: Long, existingId: Long = 0): RequirementEntity = RequirementEntity(
    id = existingId,
    badgeId = badgeId,
    code = code,
    textSv = textSv,
    textEn = textEn,
    sortOrder = sortOrder,
)
