package com.watertracker.app.viewmodels

import androidx.lifecycle.*
import com.watertracker.app.database.entities.EntryWithDetails
import com.watertracker.app.database.entities.WaterEntry
import com.watertracker.app.repository.WaterRepository
import kotlinx.coroutines.launch

/**
 * ViewModel экрана "Добавить/Редактировать". entryId == -1L означает режим
 * добавления новой записи, иначе — редактирование существующей.
 */
class AddEditViewModel(
    private val repository: WaterRepository,
    private val entryId: Long
) : ViewModel() {

    val isEditMode: Boolean get() = entryId != -1L
    val drinkTypes = repository.observeDrinkTypes().asLiveData()

    private val _existingEntry = MutableLiveData<EntryWithDetails?>()
    val existingEntry: LiveData<EntryWithDetails?> = _existingEntry

    private val _saveResult = MutableLiveData<SaveResult?>()
    val saveResult: LiveData<SaveResult?> = _saveResult

    init {
        if (isEditMode) {
            viewModelScope.launch {
                _existingEntry.value = repository.getEntryDetails(entryId)
            }
        }
    }

    /** Валидация + сохранение. Возвращает false через saveResult, если поля некорректны. */
    fun save(drinkTypeId: Long?, amountText: String, note: String?) {
        if (drinkTypeId == null) {
            _saveResult.value = SaveResult.Error("Выберите тип напитка")
            return
        }
        val amount = amountText.trim().toIntOrNull()
        if (amountText.isBlank()) {
            _saveResult.value = SaveResult.Error("Укажите количество")
            return
        }
        if (amount == null || amount <= 0) {
            _saveResult.value = SaveResult.Error("Введите корректное число")
            return
        }

        viewModelScope.launch {
            if (isEditMode) {
                val current = _existingEntry.value?.entry
                if (current != null) {
                    repository.updateEntry(
                        current.copy(drinkTypeId = drinkTypeId, amountMl = amount, note = note)
                    )
                }
            } else {
                repository.addEntry(
                    WaterEntry(
                        drinkTypeId = drinkTypeId,
                        amountMl = amount,
                        note = note,
                        timestampMillis = System.currentTimeMillis()
                    )
                )
            }
            _saveResult.value = SaveResult.Success
        }
    }

    fun consumeSaveResult() { _saveResult.value = null }

    sealed class SaveResult {
        object Success : SaveResult()
        data class Error(val message: String) : SaveResult()
    }

    class Factory(
        private val repository: WaterRepository,
        private val entryId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddEditViewModel(repository, entryId) as T
    }
}
