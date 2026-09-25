package com.watertracker.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Основная таблица "Тег" (например: тренировка, жара, болезнь).
 * Связь М-М с WaterEntry через промежуточную таблицу EntryTagCrossRef.
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
