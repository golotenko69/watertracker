package com.watertracker.app.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Промежуточная (junction) таблица, реализующая связь "многие-ко-многим"
 * между WaterEntry и Tag. Это 4-я таблица базы данных, требуемая ТЗ.
 */
@Entity(
    tableName = "entry_tag_cross_ref",
    primaryKeys = ["entryId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = WaterEntry::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entryId"), Index("tagId")]
)
data class EntryTagCrossRef(
    val entryId: Long,
    val tagId: Long
)
