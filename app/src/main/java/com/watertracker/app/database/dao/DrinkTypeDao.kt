package com.watertracker.app.database.dao

import androidx.room.*
import com.watertracker.app.database.entities.DrinkType
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkTypeDao {

    @Query("SELECT * FROM drink_types ORDER BY id ASC")
    fun observeAll(): Flow<List<DrinkType>>

    @Query("SELECT * FROM drink_types WHERE id = :id")
    suspend fun getById(id: Long): DrinkType?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(types: List<DrinkType>)

    @Insert
    suspend fun insert(type: DrinkType): Long

    @Update
    suspend fun update(type: DrinkType)

    // Удаление DrinkType каскадно удалит все связанные WaterEntry (FK CASCADE)
    @Delete
    suspend fun delete(type: DrinkType)

    @Query("SELECT COUNT(*) FROM drink_types")
    suspend fun count(): Int
}
