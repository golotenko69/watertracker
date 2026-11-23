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
 * экрана. При обновлении считывает текущий итог дня из Room.
 */
class WaterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        receiveAsync {
            val app = context.applicationContext as WaterTrackerApp
            val session = app.session
            val total = session?.repository?.observeTodayTotalMl()?.first() ?: 0
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                if (app.session === session) {
                    widgetIds.forEach { updateWidget(context, manager, it, total, session?.account?.id) }
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_QUICK_ADD) return
        val amount = intent.getIntExtra(EXTRA_AMOUNT, 0)
        if (amount !in listOf(150, 250, 350)) return
        receiveAsync {
            val app = context.applicationContext as WaterTrackerApp
            val session = app.session ?: return@receiveAsync
            if (intent.getStringExtra("profile") != session.account.id) return@receiveAsync
            val water = session.repository.observeDrinkTypes().first().firstOrNull { it.iconKey == "water" }
                ?: return@receiveAsync
            session.repository.addEntry(WaterEntry(drinkTypeId = water.id, amountMl = amount,
                timestampMillis = System.currentTimeMillis()))
            refreshAllWidgets(context)
        }
    }

    private fun receiveAsync(action: suspend () -> Unit) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                action()
            } catch (error: Exception) {
                android.util.Log.e("WaterWidget", "Ошибка обновления виджета", error)
            } finally {
                pending.finish()
            }
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int, total: Int, profileId: String?) {
        val views = RemoteViews(context.packageName, R.layout.water_widget)
        val open = Intent(context, com.watertracker.app.ui.main.MainActivity::class.java)
        views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 0, open,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
        if (profileId == null) {
            views.setTextViewText(R.id.widget_total, "Войти в дневник")
            views.setTextViewText(R.id.widget_percent, "")
            for (button in listOf(R.id.widget_btn_150, R.id.widget_btn_250, R.id.widget_btn_350)) {
                views.setViewVisibility(button, android.view.View.GONE)
            }
            manager.updateAppWidget(widgetId, views)
            return
        }
        for (button in listOf(R.id.widget_btn_150, R.id.widget_btn_250, R.id.widget_btn_350)) {
            views.setViewVisibility(button, android.view.View.VISIBLE)
        }
        val goal = PreferencesManager(context, profileId).dailyGoalMl
        val percent = if (goal > 0) min(100, ((total.toDouble() / goal) * 100).roundToInt()) else 0
        views.setTextViewText(R.id.widget_total, "$total / $goal мл")
        views.setTextViewText(R.id.widget_percent, "$percent%")

        views.setOnClickPendingIntent(R.id.widget_btn_150, quickAddPendingIntent(context, widgetId, 150, profileId))
        views.setOnClickPendingIntent(R.id.widget_btn_250, quickAddPendingIntent(context, widgetId, 250, profileId))
        views.setOnClickPendingIntent(R.id.widget_btn_350, quickAddPendingIntent(context, widgetId, 350, profileId))

        manager.updateAppWidget(widgetId, views)
    }

    private fun quickAddPendingIntent(context: Context, widgetId: Int, amount: Int, profileId: String): PendingIntent {
        val intent = Intent(context, WaterWidgetProvider::class.java).apply {
            action = ACTION_QUICK_ADD
            putExtra(EXTRA_AMOUNT, amount)
            putExtra("profile", profileId)
            data = android.net.Uri.parse("watertracker://widget/$widgetId/$profileId/$amount")
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, amount, intent, flags)
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
