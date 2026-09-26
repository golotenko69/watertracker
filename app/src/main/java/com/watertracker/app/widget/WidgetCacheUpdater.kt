package com.watertracker.app.widget

import android.content.Context
import com.watertracker.app.repository.WaterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Виджет (RemoteViews) не может напрямую подписаться на Flow/LiveData из Room,
 * поэтому приложение в фоне следит за суммой потребления за сегодня и
 * складывает актуальное значение в лёгкий SharedPreferences-кэш, а затем
 * просит [WaterWidgetProvider] перерисовать все размещённые виджеты.
 */
object WidgetCacheUpdater {
    const val PREFS_NAME = "widget_cache"
    const val KEY_TODAY_TOTAL_ML = "today_total_ml"

    fun start(context: Context, repository: WaterRepository, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            repository.observeTodayTotalMl().collectLatest { totalMl ->
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(KEY_TODAY_TOTAL_ML, totalMl)
                    .apply()
                WaterWidgetProvider.refreshAllWidgets(context)
            }
        }
    }
}
