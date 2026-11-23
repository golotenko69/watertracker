package com.watertracker.app.widget

import android.content.Context
import com.watertracker.app.repository.WaterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Обновляет размещённые виджеты при изменении дневника в приложении. */
object WidgetCacheUpdater {
    fun start(context: Context, repository: WaterRepository, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            repository.observeTodayTotalMl().collectLatest {
                WaterWidgetProvider.refreshAllWidgets(context)
            }
        }
    }
}
