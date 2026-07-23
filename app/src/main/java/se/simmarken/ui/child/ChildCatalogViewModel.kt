package se.simmarken.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.simmarken.data.local.entity.BadgeEntity
import kotlinx.coroutines.flow.update
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.domain.BadgeCatalogMapper
import se.simmarken.domain.model.CategorySection
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.repository.ProgressRepository

data class ChildCatalogUiState(
    val sections: List<CategorySection> = emptyList(),
    val catalogs: List<se.simmarken.data.local.entity.CatalogEntity> = emptyList(),
    val kidMissing: Boolean = false,
)

class ChildCatalogViewModel(
    private val kidRepository: KidRepository,
    private val catalogRepository: CatalogRepository,
    private val progressRepository: ProgressRepository,
    private val kidId: Long,
) : ViewModel() {
    private val selectedCatalogId = MutableStateFlow<Long?>(null)

    val childName = kidRepository.observeById(kidId)
        .map { kid -> kid?.name ?: "" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "",
        )

    private val categorySectionsFlow = selectedCatalogId.flatMapLatest { catalogId ->
        if (catalogId == null) {
            flowOf(emptyList())
        } else {
            catalogRepository.observeCategories(catalogId).flatMapLatest { categories ->
                if (categories.isEmpty()) {
                    flowOf(emptyList<Pair<CategoryEntity, List<BadgeEntity>>>())
                } else {
                    val categoryBadgeFlows = categories.map { category ->
                        catalogRepository.observeBadges(category.id).map { badges ->
                            category to badges
                        }
                    }
                    combine(categoryBadgeFlows) { pairs -> pairs.toList() }
                }
            }.flatMapLatest { categoryBadges ->
                combine(
                    flowOf(categoryBadges),
                    progressRepository.observeRequirementProgress(kidId),
                    progressRepository.observeBadgeProgress(kidId),
                    catalogRepository.observeAllRequirements(),
                ) { pairs, reqProgress, badgeProgress, allRequirements ->
                    val requirementProgressById = reqProgress.associate { it.requirementId to it.isAchieved }
                    val gottenByBadgeId = badgeProgress.associate { it.badgeId to it.isGotten }
                    pairs
                        .sortedBy { it.first.sortOrder }
                        .map { (category, badges) ->
                            BadgeCatalogMapper.toCategorySection(
                                category = category,
                                badges = badges.sortedBy { it.sortOrder },
                                allRequirements = allRequirements,
                                requirementProgressById = requirementProgressById,
                                gottenByBadgeId = gottenByBadgeId,
                            )
                        }
                }
            }
        }
    }

    val uiState = combine(
        categorySectionsFlow,
        catalogRepository.observeCatalogs(),
        kidRepository.observeById(kidId).map { it == null },
        selectedCatalogId,
    ) { sections, catalogs, kidMissing, selectedId ->
        if (selectedId == null && catalogs.isNotEmpty()) {
            val defaultCatalog = catalogs.find { it.code == "simidrott" } ?: catalogs.first()
            selectedCatalogId.update { defaultCatalog.id }
        }
        ChildCatalogUiState(
            sections = sections,
            catalogs = catalogs,
            kidMissing = kidMissing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChildCatalogUiState(),
    )

    fun selectCatalog(catalogId: Long) {
        selectedCatalogId.value = catalogId
    }

    fun selectCatalogByTabIndex(index: Int) {
        val catalogs = uiState.value.catalogs.sortedBy { it.sortOrder }
        val code = when (index) {
            0 -> "simidrott"
            1 -> "sls"
            else -> return
        }
        catalogs.find { it.code == code }?.let { selectCatalog(it.id) }
    }
}
