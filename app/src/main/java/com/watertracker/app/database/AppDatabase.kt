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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "water_tracker.db"
                )
                    .addCallback(PrepopulateCallback(context))
                    .build().also { INSTANCE = it }
            }
    }

    /** Заполняет справочник типов напитков при первом создании БД. */
    private class PrepopulateCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(context).drinkTypeDao()
                dao.insertAll(
                    listOf(
                        DrinkType(name = "Вода", iconKey = "water"),
                        DrinkType(name = "Сок", iconKey = "juice"),
                        DrinkType(name = "Чай", iconKey = "tea"),
                        DrinkType(name = "Кофе", iconKey = "coffee"),
                        DrinkType(name = "Молоко", iconKey = "milk"),
                        DrinkType(name = "Спортивный", iconKey = "sport"),
                        DrinkType(name = "Газировка", iconKey = "soda"),
                        DrinkType(name = "Другое", iconKey = "other")
                    )
                )
            }
        }
    }
}
