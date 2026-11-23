package com.watertracker.app.repository

import com.watertracker.app.database.dao.DailyTotal
import com.watertracker.app.database.dao.DrinkTypeDao
import com.watertracker.app.database.dao.EntryTagCrossRefDao
import com.watertracker.app.database.dao.TagDao
import com.watertracker.app.database.dao.WaterEntryDao
import com.watertracker.app.database.entities.DrinkType
import com.watertracker.app.database.entities.EntryTagCrossRef
import com.watertracker.app.database.entities.EntryWithDetails
import com.watertracker.app.database.entities.Tag
import com.watertracker.app.database.entities.WaterEntry
import com.watertracker.app.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Единая точка доступа к данным (паттерн Repository), полностью скрывающая
 * DAO от ViewModel'ей. Все операции записи выполняются в Dispatchers.IO,
 * т.е. вне главного потока — как того требует раздел 2.5 ТЗ.
 */
class WaterRepository(
    private val entryDao: WaterEntryDao,
    private val drinkTypeDao: DrinkTypeDao,
    private val tagDao: TagDao,
    private val crossRefDao: EntryTagCrossRefDao
) {

    // ---------- Типы напитков ----------
    fun observeDrinkTypes(): Flow<List<DrinkType>> = drinkTypeDao.observeAll()

    suspend fun addDrinkType(type: DrinkType): Long = withContext(Dispatchers.IO) {
        drinkTypeDao.insert(type)
    }

    suspend fun deleteDrinkType(type: DrinkType) = withContext(Dispatchers.IO) {
        drinkTypeDao.delete(type) // каскадно удалит все связанные записи
    }

    // ---------- Записи (CRUD) ----------

    suspend fun addEntry(entry: WaterEntry, tags: List<Tag> = emptyList()): Long =
        withContext(Dispatchers.IO) {
            val id = entryDao.insert(entry)
            linkTags(id, tags)
            id
        }

    suspend fun updateEntry(entry: WaterEntry) =
        withContext(Dispatchers.IO) {
            check(entryDao.update(entry) == 1) { "Запись недоступна" }
        }

    suspend fun deleteEntry(entry: WaterEntry) = withContext(Dispatchers.IO) {
        entryDao.delete(entry)
    }

    suspend fun getEntryDetails(id: Long): EntryWithDetails? = withContext(Dispatchers.IO) {
        entryDao.getWithDetailsById(id)
    }

    private suspend fun linkTags(entryId: Long, tags: List<Tag>) {
        tags.forEach { tag ->
            val existing = tagDao.findByName(tag.name)
            val tagId = existing?.id ?: tagDao.insert(tag)
            crossRefDao.link(EntryTagCrossRef(entryId, tagId))
        }
    }

    // ---------- Список: поиск / фильтр / сортировка ----------

    fun observeFilteredEntries(
        query: String,
        drinkTypeFilter: Long,
        dateFromMillis: Long,
        dateToMillis: Long,
        sortOrder: Int
    ): Flow<List<EntryWithDetails>> = entryDao.observeFiltered(
        query, drinkTypeFilter, dateFromMillis, dateToMillis, sortOrder
    )

    fun observeTodayEntries(): Flow<List<EntryWithDetails>> =
        entryDao.observeToday(DateUtils.startOfTodayMillis())

    fun observeTodayTotalMl(): Flow<Int> =
        entryDao.observeTodayTotal(DateUtils.startOfTodayMillis())

    // ---------- Статистика ----------

    suspend fun getDailyTotals(periodDays: Int): List<DailyTotal> = withContext(Dispatchers.IO) {
        entryDao.getDailyTotalsSince(DateUtils.daysAgoMillis(periodDays - 1))
    }

    suspend fun getAverageDaily(periodDays: Int): Double = withContext(Dispatchers.IO) {
        entryDao.getAverageDaily(DateUtils.daysAgoMillis(periodDays - 1))
    }

    suspend fun getTotal(periodDays: Int): Int = withContext(Dispatchers.IO) {
        entryDao.getTotalSince(DateUtils.daysAgoMillis(periodDays - 1))
    }

    suspend fun getBestDay(periodDays: Int): Int = withContext(Dispatchers.IO) {
        entryDao.getBestDaySince(DateUtils.daysAgoMillis(periodDays - 1))
    }
}
