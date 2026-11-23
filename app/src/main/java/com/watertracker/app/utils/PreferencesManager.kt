package com.watertracker.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Обёртка над SharedPreferences для экрана "Настройки":
 * дневная норма, единицы измерения, сортировка списка, напоминания.
 */
class PreferencesManager(context: Context, profileId: String = checkNotNull(
    (context.applicationContext as com.watertracker.app.WaterTrackerApp).session
).account.id) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            if (profileId == "original") "water_tracker_prefs" else "water_tracker_prefs_$profileId",
            Context.MODE_PRIVATE
        )

    var dailyGoalMl: Int
        get() = prefs.getInt(KEY_DAILY_GOAL, Constants.DEFAULT_DAILY_GOAL_ML)
        set(value) = prefs.edit { putInt(KEY_DAILY_GOAL, value) }

    var unit: String
        get() = prefs.getString(KEY_UNIT, Constants.UNIT_ML) ?: Constants.UNIT_ML
        set(value) = prefs.edit { putString(KEY_UNIT, value) }

    var sortOrder: Int
        get() = prefs.getInt(KEY_SORT_ORDER, Constants.SORT_NEWEST)
        set(value) = prefs.edit { putInt(KEY_SORT_ORDER, value) }

    var remindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDERS, false)
        set(value) = prefs.edit { putBoolean(KEY_REMINDERS, value) }

    fun observe(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs.registerOnSharedPreferenceChangeListener(listener)

    fun removeObserver(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs.unregisterOnSharedPreferenceChangeListener(listener)

    companion object {
        private const val KEY_DAILY_GOAL = "daily_goal_ml"
        private const val KEY_UNIT = "unit"
        private const val KEY_SORT_ORDER = "sort_order"
        private const val KEY_REMINDERS = "reminders_enabled"
    }
}
