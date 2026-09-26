package com.watertracker.app

import android.app.Application
import com.watertracker.app.database.AppDatabase
import com.watertracker.app.repository.WaterRepository
import com.watertracker.app.widget.WidgetCacheUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Точка входа приложения. Здесь лениво создаются единственные на всё
 * приложение экземпляры базы данных и репозитория (простой service locator,
 * чтобы не тянуть в учебный проект отдельный DI-фреймворк).
 */
class WaterTrackerApp : Application() {

    /** Живёт всё время работы процесса приложения — для фоновых наблюдателей вроде виджета. */
    private val appScope = CoroutineScope(SupervisorJob())

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: WaterRepository by lazy {
        WaterRepository(
            entryDao = database.waterEntryDao(),
            drinkTypeDao = database.drinkTypeDao(),
            tagDao = database.tagDao(),
            crossRefDao = database.entryTagCrossRefDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // "Изюминка": держим виджет на рабочем столе синхронизированным с БД
        WidgetCacheUpdater.start(this, repository, appScope)
    }
}
