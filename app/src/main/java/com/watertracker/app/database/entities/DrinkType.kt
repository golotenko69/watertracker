package com.watertracker.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Основная таблица "Тип напитка" (выступает как категория).
 * Связь 1-М: один DrinkType -> много WaterEntry.
 */
@Entity(tableName = "drink_types")
data class DrinkType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconKey: String,      // ключ иконки: water, juice, tea, coffee, milk, sport, soda, other
    val isCustom: Boolean = false
)
