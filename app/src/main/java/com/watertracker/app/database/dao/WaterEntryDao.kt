package com.watertracker.app.database.dao

import androidx.room.*
import com.watertracker.app.database.entities.EntryWithDetails
import com.watertracker.app.database.entities.WaterEntry
import kotlinx.coroutines.flow.Flow

/**
 * DAO для главной сущности приложения — записи о потреблении жидкости.
 * Содержит полный CRUD, а также поиск/фильтрацию/сортировку и агрегатные
 * запросы для экрана статистики.
 */
@Dao
interface WaterEntryDao {

    // ---------- CRUD ----------

    @Insert
    suspend fun insert(entry: WaterEntry): Long

    @Update
    suspend fun update(entry: WaterEntry)

    @Delete
    suspend fun delete(entry: WaterEntry)

    @Query("SELECT * FROM water_entries WHERE id = :id")
    suspend fun getById(id: Long): WaterEntry?

    @Transaction
    @Query("SELECT * FROM water_entries WHERE id = :id")
    suspend fun getWithDetailsById(id: Long): EntryWithDetails?

    // ---------- Список с поиском / фильтрами / сортировкой ----------
    // query: поиск по заметке ('' = без ограничения)
    // drinkTypeFilter: -1 = все типы напитка
    // dateFromMillis / dateToMillis: -1 = без ограничения по дате
    // sortOrder: 0 = сначала новые, 1 = сначала старые, 2 = по количеству (убыв.)
    @Transaction
    @Query(
        """
        SELECT * FROM water_entries
        WHERE (:query = '' OR note LIKE '%' || :query || '%')
          AND (:drinkTypeFilter = -1 OR drinkTypeId = :drinkTypeFilter)
          AND (:dateFromMillis = -1 OR timestampMillis >= :dateFromMillis)
          AND (:dateToMillis = -1 OR timestampMillis <= :dateToMillis)
        ORDER BY
          CASE WHEN :sortOrder = 0 THEN timestampMillis END DESC,
          CASE WHEN :sortOrder = 1 THEN timestampMillis END ASC,
          CASE WHEN :sortOrder = 2 THEN amountMl END DESC
        """
    )
    fun observeFiltered(
        query: String,
        drinkTypeFilter: Long,
        dateFromMillis: Long,
        dateToMillis: Long,
        sortOrder: Int
    ): Flow<List<EntryWithDetails>>

    @Transaction
    @Query("SELECT * FROM water_entries WHERE timestampMillis >= :startOfDay ORDER BY timestampMillis DESC")
    fun observeToday(startOfDay: Long): Flow<List<EntryWithDetails>>

    // ---------- Статистика ----------

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE timestampMillis >= :startOfDay")
    fun observeTodayTotal(startOfDay: Long): Flow<Int>

    @Query(
        """
        SELECT (timestampMillis / 86400000) AS dayBucket, SUM(amountMl) AS total
        FROM water_entries
        WHERE timestampMillis >= :sinceMillis
        GROUP BY dayBucket
        ORDER BY dayBucket ASC
        """
    )
    suspend fun getDailyTotalsSince(sinceMillis: Long): List<DailyTotal>

    @Query("SELECT COALESCE(AVG(daily), 0) FROM (SELECT SUM(amountMl) AS daily FROM water_entries WHERE timestampMillis >= :sinceMillis GROUP BY timestampMillis / 86400000)")
    suspend fun getAverageDaily(sinceMillis: Long): Double

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE timestampMillis >= :sinceMillis")
    suspend fun getTotalSince(sinceMillis: Long): Int

    @Query("SELECT COALESCE(MAX(daily), 0) FROM (SELECT SUM(amountMl) AS daily FROM water_entries WHERE timestampMillis >= :sinceMillis GROUP BY timestampMillis / 86400000)")
    suspend fun getBestDaySince(sinceMillis: Long): Int
}

data class DailyTotal(
    val dayBucket: Long,
    val total: Int
)
