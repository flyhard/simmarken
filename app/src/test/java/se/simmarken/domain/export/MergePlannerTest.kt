package se.simmarken.domain.export

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import se.simmarken.data.export.BackupDto
import se.simmarken.data.export.BadgeProgressBackupDto
import se.simmarken.data.export.KidBackupDto
import se.simmarken.data.export.RequirementProgressBackupDto
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

class MergePlannerTest {
    private val resolver = object : CatalogCodeResolver {
        override suspend fun resolveRequirement(
            catalogCode: String,
            badgeCode: String,
            requirementCode: String,
        ): Long? = when {
            catalogCode == "simidrott" && badgeCode == "simmare" && requirementCode == "req1" -> 40L
            else -> null
        }

        override suspend fun resolveBadge(catalogCode: String, badgeCode: String): Long? =
            if (catalogCode == "simidrott" && badgeCode == "simmare") 30L else null
    }

    @Test
    fun merge_newerWins() = runTest {
        val localKid = KidEntity(
            id = 1L,
            stableId = "kid-1",
            name = "Local",
            avatarColorArgb = 1,
            createdAtEpochMillis = 1L,
            sortOrder = 0,
        )
        val backup = backupForKid(
            stableId = "kid-1",
            name = "Remote",
            requirementUpdatedAt = 2_000L,
            requirementAchieved = true,
        )
        val preview = MergePlanner.plan(
            backup = backup,
            localKids = listOf(localKid),
            localRequirementProgress = listOf(
                RequirementProgressEntity(
                    kidId = 1L,
                    requirementId = 40L,
                    isAchieved = false,
                    achievedAtEpochMillis = null,
                    updatedAtEpochMillis = 1_000L,
                ),
            ),
            localBadgeProgress = emptyList(),
            catalogResolver = resolver,
        )

        assertEquals(1, preview.mergePayload.requirementUpserts.size)
        assertTrue(preview.mergePayload.requirementUpserts.first().isAchieved)
    }

    @Test
    fun merge_keepsLocalKidProfile() = runTest {
        val localKid = KidEntity(
            id = 1L,
            stableId = "kid-1",
            name = "Local Name",
            avatarColorArgb = 0xFF0000FF.toInt(),
            createdAtEpochMillis = 1L,
            sortOrder = 0,
        )
        val backup = backupForKid(
            stableId = "kid-1",
            name = "Remote Name",
            requirementUpdatedAt = 500L,
            requirementAchieved = true,
        )
        val preview = MergePlanner.plan(
            backup = backup,
            localKids = listOf(localKid),
            localRequirementProgress = emptyList(),
            localBadgeProgress = emptyList(),
            catalogResolver = resolver,
        )

        assertTrue(preview.newKids.isEmpty())
        assertTrue(preview.mergePayload.newKidBackups.isEmpty())
        assertTrue(preview.mergePayload.requirementUpserts.isNotEmpty())
    }

    @Test
    fun merge_newKidsUnchecked() = runTest {
        val backup = backupForKid(
            stableId = "new-kid",
            name = "New Kid",
            requirementUpdatedAt = 1_000L,
            requirementAchieved = true,
        )
        val preview = MergePlanner.plan(
            backup = backup,
            localKids = emptyList(),
            localRequirementProgress = emptyList(),
            localBadgeProgress = emptyList(),
            catalogResolver = resolver,
        )

        assertEquals(1, preview.newKids.size)
        assertFalse(preview.newKids.first().accepted)
        assertEquals(0, preview.updateCount)
    }

    @Test
    fun merge_skipsUnknownCodes() = runTest {
        val localKid = KidEntity(
            id = 1L,
            stableId = "kid-1",
            name = "Ella",
            avatarColorArgb = 1,
            createdAtEpochMillis = 1L,
            sortOrder = 0,
        )
        val backup = BackupDto(
            exportedAtEpochMillis = 1L,
            kids = listOf(
                KidBackupDto(
                    stableId = "kid-1",
                    name = "Ella",
                    avatarColorArgb = 1,
                    createdAtEpochMillis = 1L,
                    sortOrder = 0,
                    requirementProgress = listOf(
                        RequirementProgressBackupDto(
                            catalogCode = "unknown",
                            badgeCode = "missing",
                            requirementCode = "nope",
                            isAchieved = true,
                            achievedAtEpochMillis = 1L,
                            updatedAtEpochMillis = 1L,
                        ),
                    ),
                    badgeProgress = emptyList(),
                ),
            ),
        )
        val preview = MergePlanner.plan(
            backup = backup,
            localKids = listOf(localKid),
            localRequirementProgress = emptyList(),
            localBadgeProgress = emptyList(),
            catalogResolver = resolver,
        )

        assertEquals(1, preview.skippedRowCount)
        assertEquals(0, preview.updateCount)
    }

    private fun backupForKid(
        stableId: String,
        name: String,
        requirementUpdatedAt: Long,
        requirementAchieved: Boolean,
    ): BackupDto = BackupDto(
        exportedAtEpochMillis = 1L,
        kids = listOf(
            KidBackupDto(
                stableId = stableId,
                name = name,
                avatarColorArgb = 1,
                createdAtEpochMillis = 1L,
                sortOrder = 0,
                requirementProgress = listOf(
                    RequirementProgressBackupDto(
                        catalogCode = "simidrott",
                        badgeCode = "simmare",
                        requirementCode = "req1",
                        isAchieved = requirementAchieved,
                        achievedAtEpochMillis = requirementUpdatedAt,
                        updatedAtEpochMillis = requirementUpdatedAt,
                    ),
                ),
                badgeProgress = listOf(
                    BadgeProgressBackupDto(
                        catalogCode = "simidrott",
                        badgeCode = "simmare",
                        isGotten = false,
                        achievedAtEpochMillis = null,
                        gottenAtEpochMillis = null,
                        updatedAtEpochMillis = 500L,
                    ),
                ),
            ),
        ),
    )
}
