package com.watertracker.app.viewmodels

import androidx.lifecycle.*
import com.watertracker.app.database.dao.DailyTotal
import com.watertracker.app.repository.WaterRepository
import com.watertracker.app.utils.PreferencesManager
import kotlinx.coroutines.launch

data class StatsUiState(
    val dailyTotals: List<DailyTotal>,
    val averageMl: Int,
    val totalMl: Int,
    val bestDayMl: Int,
    val daysGoalMet: Int,
    val periodDays: Int,
    val goalMl: Int
)

class StatsViewModel(
    private val repository: WaterRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableLiveData<StatsUiState>()
    val uiState: LiveData<StatsUiState> = _uiState

    private var periodDays = 7

    init { load(periodDays) }

    fun load(days: Int) {
        periodDays = days
        viewModelScope.launch {
            val dailyTotals = repository.getDailyTotals(days)
            val average = repository.getAverageDaily(days).toInt()
            val total = repository.getTotal(days)
            val best = repository.getBestDay(days)
            val goal = prefs.dailyGoalMl
            val daysGoalMet = dailyTotals.count { it.total >= goal }

            _uiState.value = StatsUiState(
                dailyTotals = dailyTotals,
                averageMl = average,
                totalMl = total,
                bestDayMl = best,
                daysGoalMet = daysGoalMet,
                periodDays = days,
                goalMl = goal
            )
        }
    }

    fun currentUnit(): String = prefs.unit

    class Factory(
        private val repository: WaterRepository,
        private val prefs: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StatsViewModel(repository, prefs) as T
    }
}
