package com.watertracker.app.viewmodels

import androidx.lifecycle.*
import com.watertracker.app.database.entities.EntryWithDetails
import com.watertracker.app.repository.WaterRepository
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: WaterRepository,
    private val entryId: Long
) : ViewModel() {

    private val _entry = MutableLiveData<EntryWithDetails?>()
    val entry: LiveData<EntryWithDetails?> = _entry

    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> = _deleted

    init { load() }

    fun load() {
        viewModelScope.launch {
            _entry.value = repository.getEntryDetails(entryId)
        }
    }

    fun delete() {
        viewModelScope.launch {
            _entry.value?.entry?.let { repository.deleteEntry(it) }
            _deleted.value = true
        }
    }

    class Factory(
        private val repository: WaterRepository,
        private val entryId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetailViewModel(repository, entryId) as T
    }
}
