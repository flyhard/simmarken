package se.simmarken.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementProgressEntity
import se.simmarken.domain.KidProgressSummaryCalculator
import se.simmarken.domain.model.KidProgressSummary
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.repository.ProgressRepository

sealed interface KidSheetState {
    data object Hidden : KidSheetState
    data class Add(val sessionId: Long) : KidSheetState
    data class Edit(val kidId: Long, val sessionId: Long) : KidSheetState
}

data class HomeUiState(
    val kids: List<KidEntity> = emptyList(),
    val summariesByKidId: Map<Long, KidProgressSummary> = emptyMap(),
    val sheetState: KidSheetState = KidSheetState.Hidden,
    val deleteTarget: KidEntity? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val kidRepository: KidRepository,
    private val catalogRepository: CatalogRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {
    private val sheetState = MutableStateFlow<KidSheetState>(KidSheetState.Hidden)
    private val deleteTarget = MutableStateFlow<KidEntity?>(null)
    private var nextSheetSessionId = 0L

    private val summariesByKidId = kidRepository.observeAll().flatMapLatest { kids ->
        if (kids.isEmpty()) {
            flowOf(emptyMap())
        } else {
            combine(
                catalogRepository.observeAllBadges(),
                catalogRepository.observeAllCategories(),
                catalogRepository.observeAllRequirements(),
                combine(
                    kids.map { kid ->
                        combine(
                            progressRepository.observeRequirementProgress(kid.id),
                            progressRepository.observeBadgeProgress(kid.id),
                        ) { reqProgress, badgeProgress ->
                            kid.id to (reqProgress to badgeProgress)
                        }
                    },
                ) { pairs -> pairs.toMap() },
            ) { badges, categories, requirements, progressByKid ->
                val categoriesById = categories.associateBy { it.id }
                kids.associate { kid ->
                    val (reqProgress, badgeProgress) = progressByKid[kid.id]
                        ?: (emptyList<RequirementProgressEntity>() to emptyList<BadgeProgressEntity>())
                    val requirementProgressById =
                        reqProgress.associate { it.requirementId to it.isAchieved }
                    val gottenByBadgeId = badgeProgress.associate { it.badgeId to it.isGotten }
                    val summary = KidProgressSummaryCalculator.compute(
                        badges = badges,
                        categoriesById = categoriesById,
                        allRequirements = requirements,
                        requirementProgressById = requirementProgressById,
                        gottenByBadgeId = gottenByBadgeId,
                    )
                    kid.id to summary.copy(kidId = kid.id)
                }
            }
        }
    }

    val uiState = combine(
        kidRepository.observeAll(),
        summariesByKidId,
        sheetState,
        deleteTarget,
    ) { kids, summaries, sheet, delete ->
        HomeUiState(
            kids = kids,
            summariesByKidId = summaries,
            sheetState = sheet,
            deleteTarget = delete,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun openAddSheet() {
        deleteTarget.value = null
        sheetState.value = KidSheetState.Add(sessionId = nextSheetSessionId++)
    }

    fun openEditSheet(kidId: Long) {
        deleteTarget.value = null
        sheetState.value = KidSheetState.Edit(kidId = kidId, sessionId = nextSheetSessionId++)
    }

    fun closeSheet() {
        sheetState.value = KidSheetState.Hidden
    }

    fun requestDelete(kid: KidEntity) {
        sheetState.value = KidSheetState.Hidden
        deleteTarget.value = kid
    }

    fun dismissDelete() {
        deleteTarget.value = null
    }

    fun confirmDelete() {
        val kid = deleteTarget.value ?: return
        deleteTarget.value = null
        viewModelScope.launch(Dispatchers.IO) {
            kidRepository.delete(kid.id)
        }
    }
}
