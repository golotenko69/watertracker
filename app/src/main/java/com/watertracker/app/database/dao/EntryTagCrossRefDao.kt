package com.watertracker.app.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EntryTagCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun link(crossRef: com.watertracker.app.database.entities.EntryTagCrossRef)

    @Query("DELETE FROM entry_tag_cross_ref WHERE entryId = :entryId")
    suspend fun clearTagsForEntry(entryId: Long)
}
