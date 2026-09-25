package com.watertracker.app.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Основная таблица "Запись о потреблении жидкости".
 *
 * Связь 1-М: drinkTypeId -> DrinkType.id. При удалении DrinkType все его
 * записи удаляются каскадно (onDelete = CASCADE), как того требует ТЗ.
 */
@Entity(
    tableName = "water_entries",
    foreignKeys = [
        ForeignKey(
            entity = DrinkType::class,
            parentColumns = ["id"],
            childColumns = ["drinkTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("drinkTypeId")]
)
data class WaterEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val drinkTypeId: Long,
    val amountMl: Int,
    val note: String? = null,
    val timestampMillis: Long
)
