package com.watertracker.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.watertracker.app.database.entities.EntryWithDetails
import com.watertracker.app.databinding.ItemWaterEntryBinding
import com.watertracker.app.utils.DateUtils
import com.watertracker.app.utils.IconMapper
import com.watertracker.app.utils.UnitConverter

/**
 * Адаптер списка записей на главном экране.
 * unitProvider вызывается на каждый bind, чтобы всегда показывать
 * актуальную единицу измерения из настроек без пересоздания адаптера.
 */
class EntryAdapter(
    private val unitProvider: () -> String,
    private val onClick: (EntryWithDetails) -> Unit
) : ListAdapter<EntryWithDetails, EntryAdapter.EntryViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemWaterEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EntryViewHolder(private val binding: ItemWaterEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EntryWithDetails) {
            binding.tvTypeName.text = item.drinkType.name
            binding.tvNote.text = item.entry.note?.takeIf { it.isNotBlank() } ?: "Без заметки"
            binding.tvAmount.text = UnitConverter.formatAmount(item.entry.amountMl, unitProvider())
            binding.tvTime.text = DateUtils.formatTime(item.entry.timestampMillis)
            binding.ivIcon.setImageResource(IconMapper.forKey(item.drinkType.iconKey))
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<EntryWithDetails>() {
            override fun areItemsTheSame(oldItem: EntryWithDetails, newItem: EntryWithDetails) =
                oldItem.entry.id == newItem.entry.id

            override fun areContentsTheSame(oldItem: EntryWithDetails, newItem: EntryWithDetails) =
                oldItem == newItem
        }
    }
}
