package com.watertracker.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.watertracker.app.database.dao.DrinkTypeDao
import com.watertracker.app.database.dao.EntryTagCrossRefDao
import com.watertracker.app.database.dao.TagDao
import com.watertracker.app.database.dao.WaterEntryDao
import com.watertracker.app.database.entities.DrinkType
import com.watertracker.app.database.entities.EntryTagCrossRef
import com.watertracker.app.database.entities.Tag
import com.watertracker.app.database.entities.WaterEntry

/**
 * 4 таблицы: DrinkType, WaterEntry, Tag (основные) + EntryTagCrossRef (junction).
 */
@Database(
    entities = [DrinkType::class, WaterEntry::class, Tag::class, EntryTagCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun drinkTypeDao(): DrinkTypeDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun tagDao(): TagDao
    abstract fun entryTagCrossRefDao(): EntryTagCrossRefDao

    companion object {
        fun open(context: Context, profileId: String): AppDatabase {
            val file = if (profileId == "original") "water_tracker.db" else "water_tracker_$profileId.db"
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, file)
                .setAutoCloseTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addCallback(PrepopulateCallback())
                .build()
        }
    }

    /** Заполняет справочник типов напитков при первом создании БД. */
    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val drinks = listOf("Вода" to "water", "Сок" to "juice", "Чай" to "tea",
                "Кофе" to "coffee", "Молоко" to "milk", "Спортивный" to "sport",
                "Газировка" to "soda", "Другое" to "other")
            for ((name, icon) in drinks) {
                db.execSQL("INSERT INTO drink_types (name, iconKey, isCustom) VALUES (?, ?, 0)", arrayOf(name, icon))
            }
        }
    }
}
