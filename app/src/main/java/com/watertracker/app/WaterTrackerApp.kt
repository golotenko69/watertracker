package com.watertracker.app

import android.app.Application
import com.watertracker.app.database.AppDatabase
import com.watertracker.app.repository.WaterRepository

/**
 * Точка входа приложения. Здесь лениво создаются единственные на всё
 * приложение экземпляры базы данных и репозитория (простой service locator,
 * чтобы не тянуть в учебный проект отдельный DI-фреймворк).
 */
class WaterTrackerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: WaterRepository by lazy {
        WaterRepository(
            entryDao = database.waterEntryDao(),
            drinkTypeDao = database.drinkTypeDao(),
            tagDao = database.tagDao(),
            crossRefDao = database.entryTagCrossRefDao()
        )
    }
}
