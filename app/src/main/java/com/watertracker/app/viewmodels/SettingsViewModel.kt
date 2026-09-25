package com.watertracker.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.watertracker.app.utils.Constants
import com.watertracker.app.utils.PreferencesManager

/**
 * ViewModel экрана "Настройки". Тонкая обёртка над PreferencesManager:
 * хранит и меняет дневную норму, единицы измерения, сортировку и
 * состояние напоминаний (раздел 2.1 ТЗ, экран №5).
 */
class SettingsViewModel(private val prefs: PreferencesManager) : ViewModel() {

    fun getDailyGoal(): Int = prefs.dailyGoalMl
    fun setDailyGoal(value: Int) { prefs.dailyGoalMl = value }

    fun getUnit(): String = prefs.unit
    fun setUnit(unit: String) { prefs.unit = unit }

    fun getSortOrder(): Int = prefs.sortOrder
    fun setSortOrder(order: Int) { prefs.sortOrder = order }

    fun getRemindersEnabled(): Boolean = prefs.remindersEnabled
    fun setRemindersEnabled(enabled: Boolean) { prefs.remindersEnabled = enabled }

    class Factory(private val prefs: PreferencesManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(prefs) as T
    }
}
