package se.simmarken.domain.export

import se.simmarken.data.export.BackupDto
import se.simmarken.data.export.BadgeProgressBackupDto
import se.simmarken.data.export.KidBackupDto
import se.simmarken.data.export.RequirementProgressBackupDto
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

data class NewKidPreview(
    val stableId: String,
    val name: String,
    val accepted: Boolean = false,
)

data class ImportPreview(
    val updateCount: Int,
    val newKids: List<NewKidPreview>,
    val skippedRowCount: Int,
    val mergePayload: MergePayload,
)

data class MergePayload(
    val requirementUpserts: List<RequirementProgressEntity>,
    val badgeUpserts: List<BadgeProgressEntity>,
    val newKidBackups: List<KidBackupDto>,
)

interface CatalogCodeResolver {
    suspend fun resolveRequirement(
        catalogCode: String,
        badgeCode: String,
        requirementCode: String,
    ): Long?

    suspend fun resolveBadge(
        catalogCode: String,
        badgeCode: String,
    ): Long?
}

object MergePlanner {
    suspend fun plan(
        backup: BackupDto,
        localKids: List<KidEntity>,
        localRequirementProgress: List<RequirementProgressEntity>,
        localBadgeProgress: List<BadgeProgressEntity>,
        catalogResolver: CatalogCodeResolver,
    ): ImportPreview {
        val localKidsByStableId = localKids.associateBy { it.stableId }
        val requirementUpserts = mutableListOf<RequirementProgressEntity>()
        val badgeUpserts = mutableListOf<BadgeProgressEntity>()
        var skippedRowCount = 0
        val newKidBackups = mutableListOf<KidBackupDto>()

        for (kidBackup in backup.kids) {
            val localKid = localKidsByStableId[kidBackup.stableId]
            if (localKid == null) {
                newKidBackups += kidBackup
                continue
            }

            for (remote in kidBackup.requirementProgress) {
                val requirementId = catalogResolver.resolveRequirement(
                    remote.catalogCode,
                    remote.badgeCode,
                    remote.requirementCode,
                )
                if (requirementId == null) {
                    skippedRowCount++
                    continue
                }
                val local = localRequirementProgress.find {
                    it.kidId == localKid.id && it.requirementId == requirementId
                }
                if (shouldApplyRemote(remote.updatedAtEpochMillis, local?.updatedAtEpochMillis ?: 0L)) {
                    requirementUpserts += RequirementProgressEntity(
                        kidId = localKid.id,
                        requirementId = requirementId,
                        isAchieved = remote.isAchieved,
                        achievedAtEpochMillis = remote.achievedAtEpochMillis,
                        updatedAtEpochMillis = remote.updatedAtEpochMillis,
                    )
                }
            }

            for (remote in kidBackup.badgeProgress) {
                val badgeId = catalogResolver.resolveBadge(remote.catalogCode, remote.badgeCode)
                if (badgeId == null) {
                    skippedRowCount++
                    continue
                }
                val local = localBadgeProgress.find {
                    it.kidId == localKid.id && it.badgeId == badgeId
                }
                if (shouldApplyRemote(remote.updatedAtEpochMillis, local?.updatedAtEpochMillis ?: 0L)) {
                    badgeUpserts += BadgeProgressEntity(
                        kidId = localKid.id,
                        badgeId = badgeId,
                        isGotten = remote.isGotten,
                        achievedAtEpochMillis = remote.achievedAtEpochMillis,
                        gottenAtEpochMillis = remote.gottenAtEpochMillis,
                        updatedAtEpochMillis = remote.updatedAtEpochMillis,
                    )
                }
            }
        }

        val newKids = newKidBackups.map { NewKidPreview(stableId = it.stableId, name = it.name) }
        return ImportPreview(
            updateCount = requirementUpserts.size + badgeUpserts.size,
            newKids = newKids,
            skippedRowCount = skippedRowCount,
            mergePayload = MergePayload(
                requirementUpserts = requirementUpserts,
                badgeUpserts = badgeUpserts,
                newKidBackups = newKidBackups,
            ),
        )
    }

    private fun shouldApplyRemote(remoteUpdatedAt: Long, localUpdatedAt: Long): Boolean =
        remoteUpdatedAt > localUpdatedAt
}
