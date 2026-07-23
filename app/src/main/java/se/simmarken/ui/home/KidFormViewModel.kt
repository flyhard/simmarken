package se.simmarken.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.validation.KidNameValidation
import se.simmarken.ui.theme.KidAvatarColors

class KidFormViewModel(
    private val kidRepository: KidRepository,
    @Suppress("UnusedPrivateProperty") private val kidId: Long?,
) : ViewModel() {
    private var userPickedColor = false

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _selectedColorArgb = MutableStateFlow(KidAvatarColors.defaultForName(""))
    val selectedColorArgb: StateFlow<Int> = _selectedColorArgb.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveCompleted = MutableStateFlow(false)
    val saveCompleted: StateFlow<Boolean> = _saveCompleted.asStateFlow()

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
        val error = KidNameValidation.validateName(_name.value)
        if (error != null) {
            _nameError.value = error
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            try {
                val kids = kidRepository.observeAll().first()
                val sortOrder = (kids.maxOfOrNull { it.sortOrder } ?: -1) + 1
                kidRepository.upsert(
                    KidEntity(
                        name = _name.value.trim(),
                        avatarColorArgb = _selectedColorArgb.value,
                        sortOrder = sortOrder,
                        createdAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
                _saveCompleted.value = true
            } finally {
                _isSaving.value = false
            }
        }
    }
}
