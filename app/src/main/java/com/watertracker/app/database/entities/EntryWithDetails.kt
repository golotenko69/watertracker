package com.watertracker.app.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Композитный объект (не отдельная таблица): запись + связанный тип напитка
 * + список тегов. Используется, чтобы не гонять несколько запросов из UI.
 */
data class EntryWithDetails(
    @Embedded val entry: WaterEntry,

    @Relation(
        parentColumn = "drinkTypeId",
        entityColumn = "id"
    )
    val drinkType: DrinkType,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EntryTagCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)
