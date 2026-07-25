package se.simmarken.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.validation.KidNameError
import se.simmarken.domain.validation.KidNameValidation
import se.simmarken.ui.theme.KidAvatarColors

class KidFormViewModel(
    private val kidRepository: KidRepository,
    private val kidId: Long?,
) : ViewModel() {
    val isEditMode: Boolean = kidId != null

    private var userPickedColor = false
    private var existingKid: KidEntity? = null
    private var initialLoadDone = false

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _selectedColorArgb = MutableStateFlow(KidAvatarColors.defaultForName(""))
    val selectedColorArgb: StateFlow<Int> = _selectedColorArgb.asStateFlow()

    private val _nameError = MutableStateFlow<KidFormFieldError?>(null)
    val nameError: StateFlow<KidFormFieldError?> = _nameError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isReadyToSave = MutableStateFlow(!isEditMode)
    val isReadyToSave: StateFlow<Boolean> = _isReadyToSave.asStateFlow()

    private val _saveCompleted = MutableStateFlow(false)
    val saveCompleted: StateFlow<Boolean> = _saveCompleted.asStateFlow()

    init {
        if (kidId != null) {
            viewModelScope.launch {
                kidRepository.observeById(kidId).collect { kid ->
                    if (kid != null && !initialLoadDone) {
                        existingKid = kid
                        _name.value = kid.name
                        _selectedColorArgb.value = kid.avatarColorArgb
                        userPickedColor = true
                        initialLoadDone = true
                        _isReadyToSave.value = true
                    }
                }
            }
        }
    }

    fun updateName(value: String) {
        _name.value = value
        _nameError.value = null
        if (!userPickedColor) {
            _selectedColorArgb.value = KidAvatarColors.defaultForName(value)
        }
    }

    fun selectColor(argb: Int) {
        userPickedColor = true
        _selectedColorArgb.value = argb
    }

    fun save() {
        if (_isSaving.value) return
        val error = KidNameValidation.validateName(_name.value)
        if (error != null) {
            _nameError.value = when (error) {
                KidNameError.EMPTY -> KidFormFieldError.NameEmpty
                KidNameError.TOO_LONG -> KidFormFieldError.NameTooLong
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            try {
                if (isEditMode) {
                    val existing = existingKid
                    if (existing == null) {
                        _nameError.value = KidFormFieldError.LoadFailed
                        return@launch
                    }
                    kidRepository.upsert(
                        existing.copy(
                            name = _name.value.trim(),
                            avatarColorArgb = _selectedColorArgb.value,
                        ),
                    )
                } else {
                    val kids = kidRepository.observeAll().first()
                    val sortOrder = (kids.maxOfOrNull { it.sortOrder } ?: -1) + 1
                    kidRepository.upsert(
                        KidEntity(
                            stableId = UUID.randomUUID().toString(),
                            name = _name.value.trim(),
                            avatarColorArgb = _selectedColorArgb.value,
                            sortOrder = sortOrder,
                            createdAtEpochMillis = System.currentTimeMillis(),
                        ),
                    )
                }
                _saveCompleted.value = true
            } finally {
                _isSaving.value = false
            }
        }
    }
}
