package se.simmarken.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import se.simmarken.data.local.dao.CatalogDao
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.data.local.entity.RequirementProgressEntity
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.repository.ProgressRepository
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

open class ExportRepository(
    private val kidRepository: KidRepository,
    private val progressRepository: ProgressRepository,
    private val catalogDao: CatalogDao,
) {
    open suspend fun buildBackup(selectedKidIds: List<Long>): BackupDto {
        val kids = kidRepository.findByIds(selectedKidIds)
            .sortedWith(compareBy<KidEntity> { it.sortOrder }.thenBy { it.name })
        if (kids.isEmpty()) {
            return BackupDto(
                exportedAtEpochMillis = System.currentTimeMillis(),
                kids = emptyList(),
            )
        }

        val kidIds = kids.map { it.id }
        val requirementProgress = progressRepository.getRequirementProgressForKids(kidIds)
        val badgeProgress = progressRepository.getBadgeProgressForKids(kidIds)

        val catalogs = catalogDao.listCatalogs().associateBy { it.id }
        val categories = catalogDao.listCategories().associateBy { it.id }
        val badges = catalogDao.listBadges().associateBy { it.id }
        val requirements = catalogDao.listRequirements().associateBy { it.id }

        return BackupDto(
            exportedAtEpochMillis = System.currentTimeMillis(),
            kids = kids.map { kid ->
                KidBackupDto(
                    stableId = kid.stableId,
                    name = kid.name,
                    avatarColorArgb = kid.avatarColorArgb,
                    createdAtEpochMillis = kid.createdAtEpochMillis,
                    sortOrder = kid.sortOrder,
                    requirementProgress = requirementProgress
                        .filter { it.kidId == kid.id }
                        .mapNotNull { row ->
                            toRequirementBackup(row, requirements, badges, categories, catalogs)
                        },
                    badgeProgress = badgeProgress
                        .filter { it.kidId == kid.id }
                        .mapNotNull { row ->
                            toBadgeBackup(row, badges, categories, catalogs)
                        },
                )
            },
        )
    }

    fun encode(dto: BackupDto): String = ExportJson.encode(dto)

    fun decode(json: String): BackupDto = ExportJson.decode(json)

    open suspend fun writeExportFile(context: Context, dto: BackupDto): File {
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val file = File(exportsDir, "simmarken-backup-$date.json")
        file.writeText(encode(dto))
        return file
    }

    open fun fileToShareUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )

    private fun toRequirementBackup(
        row: RequirementProgressEntity,
        requirements: Map<Long, RequirementEntity>,
        badges: Map<Long, BadgeEntity>,
        categories: Map<Long, CategoryEntity>,
        catalogs: Map<Long, se.simmarken.data.local.entity.CatalogEntity>,
    ): RequirementProgressBackupDto? {
        val requirement = requirements[row.requirementId] ?: return null
        val badge = badges[requirement.badgeId] ?: return null
        val category = categories[badge.categoryId] ?: return null
        val catalog = catalogs[category.catalogId] ?: return null
        return RequirementProgressBackupDto(
            catalogCode = catalog.code,
            badgeCode = badge.code,
            requirementCode = requirement.code,
            isAchieved = row.isAchieved,
            achievedAtEpochMillis = row.achievedAtEpochMillis,
            updatedAtEpochMillis = row.updatedAtEpochMillis,
        )
    }

    private fun toBadgeBackup(
        row: BadgeProgressEntity,
        badges: Map<Long, BadgeEntity>,
        categories: Map<Long, CategoryEntity>,
        catalogs: Map<Long, se.simmarken.data.local.entity.CatalogEntity>,
    ): BadgeProgressBackupDto? {
        val badge = badges[row.badgeId] ?: return null
        val category = categories[badge.categoryId] ?: return null
        val catalog = catalogs[category.catalogId] ?: return null
        return BadgeProgressBackupDto(
            catalogCode = catalog.code,
            badgeCode = badge.code,
            isGotten = row.isGotten,
            achievedAtEpochMillis = row.achievedAtEpochMillis,
            gottenAtEpochMillis = row.gottenAtEpochMillis,
            updatedAtEpochMillis = row.updatedAtEpochMillis,
        )
    }
}
