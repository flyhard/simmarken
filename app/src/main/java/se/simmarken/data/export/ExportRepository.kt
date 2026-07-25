package se.simmarken.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import se.simmarken.data.export.KidBackupDto
import se.simmarken.data.local.AppDatabase
import se.simmarken.data.local.dao.CatalogDao
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.data.local.entity.RequirementProgressEntity
import se.simmarken.domain.export.BackupValidator
import se.simmarken.domain.export.CatalogCodeResolver
import se.simmarken.domain.export.ImportPreview
import se.simmarken.domain.export.MergePlanner
import se.simmarken.domain.export.ValidationResult
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.repository.ProgressRepository
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

open class ExportRepository(
    private val kidRepository: KidRepository,
    private val progressRepository: ProgressRepository,
    private val catalogDao: CatalogDao,
    private val database: AppDatabase,
) {
    private val catalogCodeResolver = object : CatalogCodeResolver {
        override suspend fun resolveRequirement(
            catalogCode: String,
            badgeCode: String,
            requirementCode: String,
        ): Long? {
            val catalog = catalogDao.findCatalogByCode(catalogCode) ?: return null
            val badge = catalogDao.findBadgeByCatalogAndCode(catalog.id, badgeCode) ?: return null
            return catalogDao.findRequirementByBadgeAndCode(badge.id, requirementCode)?.id
        }

        override suspend fun resolveBadge(catalogCode: String, badgeCode: String): Long? {
            val catalog = catalogDao.findCatalogByCode(catalogCode) ?: return null
            return catalogDao.findBadgeByCatalogAndCode(catalog.id, badgeCode)?.id
        }
    }

    fun parseBackup(bytes: ByteArray): ValidationResult = BackupValidator.validate(bytes)

    suspend fun planImport(backup: BackupDto): ImportPreview {
        val localKids = kidRepository.findAll()
        val kidIds = localKids.map { it.id }
        val requirementProgress = if (kidIds.isEmpty()) {
            emptyList()
        } else {
            progressRepository.getRequirementProgressForKids(kidIds)
        }
        val badgeProgress = if (kidIds.isEmpty()) {
            emptyList()
        } else {
            progressRepository.getBadgeProgressForKids(kidIds)
        }
        return MergePlanner.plan(
            backup = backup,
            localKids = localKids,
            localRequirementProgress = requirementProgress,
            localBadgeProgress = badgeProgress,
            catalogResolver = catalogCodeResolver,
        )
    }

    suspend fun merge(preview: ImportPreview, acceptedNewKidStableIds: Set<String>) {
        database.withTransaction {
            val requirementProgressDao = database.requirementProgressDao()
            val badgeProgressDao = database.badgeProgressDao()
            val kidDao = database.kidDao()

            preview.mergePayload.requirementUpserts.forEach { requirementProgressDao.upsert(it) }
            preview.mergePayload.badgeUpserts.forEach { badgeProgressDao.upsert(it) }

            val acceptedNewKids = preview.mergePayload.newKidBackups
                .filter { it.stableId in acceptedNewKidStableIds }
            for (kidBackup in acceptedNewKids) {
                val kidId = kidDao.upsert(
                    KidEntity(
                        stableId = kidBackup.stableId,
                        name = kidBackup.name,
                        avatarColorArgb = kidBackup.avatarColorArgb,
                        createdAtEpochMillis = kidBackup.createdAtEpochMillis,
                        sortOrder = kidBackup.sortOrder,
                    ),
                )
                for (remote in kidBackup.requirementProgress) {
                    val requirementId = catalogCodeResolver.resolveRequirement(
                        remote.catalogCode,
                        remote.badgeCode,
                        remote.requirementCode,
                    ) ?: continue
                    requirementProgressDao.upsert(
                        RequirementProgressEntity(
                            kidId = kidId,
                            requirementId = requirementId,
                            isAchieved = remote.isAchieved,
                            achievedAtEpochMillis = remote.achievedAtEpochMillis,
                            updatedAtEpochMillis = remote.updatedAtEpochMillis,
                        ),
                    )
                }
                for (remote in kidBackup.badgeProgress) {
                    val badgeId = catalogCodeResolver.resolveBadge(remote.catalogCode, remote.badgeCode)
                        ?: continue
                    badgeProgressDao.upsert(
                        BadgeProgressEntity(
                            kidId = kidId,
                            badgeId = badgeId,
                            isGotten = remote.isGotten,
                            achievedAtEpochMillis = remote.achievedAtEpochMillis,
                            gottenAtEpochMillis = remote.gottenAtEpochMillis,
                            updatedAtEpochMillis = remote.updatedAtEpochMillis,
                        ),
                    )
                }
            }
        }
    }
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
