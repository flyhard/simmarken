package se.simmarken.data.export

import kotlinx.serialization.Serializable

/**
 * Version 1 backup JSON schema for phone migration (D-08).
 *
 * Locked field semantics — forward-compatible readers required once parents exchange files:
 * - [exportVersion] must be `1` for this schema generation.
 * - Kids are keyed by [KidBackupDto.stableId] (UUID string), never Room auto-increment ids.
 * - Progress rows use catalog [RequirementProgressBackupDto.catalogCode] +
 *   [RequirementProgressBackupDto.badgeCode] + [RequirementProgressBackupDto.requirementCode]
 *   (or badge-level codes for [BadgeProgressBackupDto]) — not Room Long foreign keys.
 * - [RequirementProgressBackupDto.updatedAtEpochMillis] and [BadgeProgressBackupDto.updatedAtEpochMillis]
 *   drive D-04 newer-wins merge per row.
 * - Payload excludes catalog seed data (D-05) and language preference (D-21).
 */
@Serializable
data class BackupDto(
    val exportVersion: Int = 1,
    val exportedAtEpochMillis: Long,
    val kids: List<KidBackupDto>,
)

@Serializable
data class KidBackupDto(
    val stableId: String,
    val name: String,
    val avatarColorArgb: Int,
    val createdAtEpochMillis: Long,
    val sortOrder: Int,
    val requirementProgress: List<RequirementProgressBackupDto>,
    val badgeProgress: List<BadgeProgressBackupDto>,
)

@Serializable
data class RequirementProgressBackupDto(
    val catalogCode: String,
    val badgeCode: String,
    val requirementCode: String,
    val isAchieved: Boolean,
    val achievedAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long,
)

@Serializable
data class BadgeProgressBackupDto(
    val catalogCode: String,
    val badgeCode: String,
    val isGotten: Boolean,
    val achievedAtEpochMillis: Long?,
    val gottenAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long,
)
