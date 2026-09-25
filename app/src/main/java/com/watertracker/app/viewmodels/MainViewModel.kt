package com.watertracker.app.viewmodels

import androidx.lifecycle.*
import com.watertracker.app.database.entities.EntryWithDetails
import com.watertracker.app.database.entities.WaterEntry
import com.watertracker.app.repository.WaterRepository
import com.watertracker.app.utils.Constants
import com.watertracker.app.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel главного экрана. Комбинирует строку поиска, фильтр по типу
 * напитка и сортировку (из настроек) в единый реактивный список Flow ->
 * LiveData, который наблюдает MainFragment.
 */
class MainViewModel(
    private val repository: WaterRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val drinkTypeFilter = MutableStateFlow(-1L)
    private val dateFromFilter = MutableStateFlow(-1L)
    private val dateToFilter = MutableStateFlow(-1L)
    private val sortOrderFlow = MutableStateFlow(prefs.sortOrder)

    val entries: LiveData<List<EntryWithDetails>> = combine(
        searchQuery, drinkTypeFilter, dateFromFilter, dateToFilter, sortOrderFlow
    ) { query, typeFilter, from, to, sort ->
        FilterState(query, typeFilter, from, to, sort)
    }.flatMapLatest { state ->
        repository.observeFilteredEntries(
            state.query, state.typeFilter, state.from, state.to, state.sort
        )
    }.asLiveData()

    val todayTotalMl: LiveData<Int> = repository.observeTodayTotalMl().asLiveData()
    val drinkTypes = repository.observeDrinkTypes().asLiveData()

    fun setSearchQuery(query: String) { searchQuery.value = query }
    fun setDrinkTypeFilter(typeId: Long) { drinkTypeFilter.value = typeId }
    fun setDateRangeFilter(fromMillis: Long, toMillis: Long) {
        dateFromFilter.value = fromMillis
        dateToFilter.value = toMillis
    }
    fun refreshSortOrder() { sortOrderFlow.value = prefs.sortOrder }

    fun quickAddWater(amountMl: Int, waterTypeId: Long) {
        viewModelScope.launch {
            repository.addEntry(
                WaterEntry(
                    drinkTypeId = waterTypeId,
                    amountMl = amountMl,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun currentDailyGoalMl(): Int = prefs.dailyGoalMl
    fun currentUnit(): String = prefs.unit

    private data class FilterState(
        val query: String,
        val typeFilter: Long,
        val from: Long,
        val to: Long,
        val sort: Int
    )

    class Factory(
        private val repository: WaterRepository,
        private val prefs: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(repository, prefs) as T
    }
}
