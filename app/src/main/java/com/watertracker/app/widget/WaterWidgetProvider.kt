package com.watertracker.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.watertracker.app.R
import com.watertracker.app.WaterTrackerApp
import com.watertracker.app.database.entities.WaterEntry
import com.watertracker.app.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * "Изюминка" проекта (раздел 2.2 ТЗ): виджет на рабочий стол.
 * Показывает прогресс дня и позволяет добавить воду одним тапом с домашнего
 * экрана, без открытия приложения. Актуальные данные виджет берёт из
 * лёгкого кэша в SharedPreferences, который обновляет WidgetCacheUpdater
 * (см. WaterTrackerApp) при каждом изменении записей в БД.
 */
class WaterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        widgetIds.forEach { id -> updateWidget(context, manager, id) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_QUICK_ADD) {
            val amount = intent.getIntExtra(EXTRA_AMOUNT, 0)
            if (amount > 0) addWaterAndRefresh(context, amount)
        }
    }

    private fun addWaterAndRefresh(context: Context, amountMl: Int) {
        val app = context.applicationContext as WaterTrackerApp
        CoroutineScope(Dispatchers.IO).launch {
            val waterType = app.repository.observeDrinkTypes().first()
                .firstOrNull { it.iconKey == "water" } ?: return@launch
            app.repository.addEntry(
                WaterEntry(
                    drinkTypeId = waterType.id,
                    amountMl = amountMl,
                    timestampMillis = System.currentTimeMillis()
                )
            )
            refreshAllWidgets(context)
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val prefs = PreferencesManager(context)
        val views = RemoteViews(context.packageName, R.layout.water_widget)

        val cache = context.getSharedPreferences(WidgetCacheUpdater.PREFS_NAME, Context.MODE_PRIVATE)
        val cachedTotal = cache.getInt(WidgetCacheUpdater.KEY_TODAY_TOTAL_ML, 0)
        val goal = prefs.dailyGoalMl
        val percent = if (goal > 0) min(100, ((cachedTotal.toDouble() / goal) * 100).roundToInt()) else 0

        views.setTextViewText(R.id.widget_total, "$cachedTotal / $goal мл")
        views.setTextViewText(R.id.widget_percent, "$percent%")

        views.setOnClickPendingIntent(R.id.widget_btn_150, quickAddPendingIntent(context, widgetId, 150))
        views.setOnClickPendingIntent(R.id.widget_btn_250, quickAddPendingIntent(context, widgetId, 250))
        views.setOnClickPendingIntent(R.id.widget_btn_350, quickAddPendingIntent(context, widgetId, 350))

        manager.updateAppWidget(widgetId, views)
    }

    private fun quickAddPendingIntent(context: Context, widgetId: Int, amount: Int): PendingIntent {
        val intent = Intent(context, WaterWidgetProvider::class.java).apply {
            action = ACTION_QUICK_ADD
            putExtra(EXTRA_AMOUNT, amount)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, widgetId * 10 + amount, intent, flags)
    }

    companion object {
        const val ACTION_QUICK_ADD = "com.watertracker.app.ACTION_QUICK_ADD"
        private const val EXTRA_AMOUNT = "extra_amount"

        /** Вызывается после любого изменения записей, чтобы обновить все виджеты на экране. */
        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WaterWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val intent = Intent(context, WaterWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
