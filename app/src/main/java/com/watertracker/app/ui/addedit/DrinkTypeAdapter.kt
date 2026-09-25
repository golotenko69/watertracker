package com.watertracker.app.ui.addedit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.watertracker.app.database.entities.DrinkType
import com.watertracker.app.databinding.ItemDrinkTypeBinding
import com.watertracker.app.utils.IconMapper

/** Сетка выбора типа напитка на экране "Добавить/Редактировать". */
class DrinkTypeAdapter(
    private val onSelected: (DrinkType) -> Unit
) : RecyclerView.Adapter<DrinkTypeAdapter.ViewHolder>() {

    private var items: List<DrinkType> = emptyList()
    private var selectedId: Long? = null

    fun submitList(list: List<DrinkType>, preselectId: Long? = null) {
        items = list
        if (preselectId != null) selectedId = preselectId
        else if (selectedId == null) selectedId = list.firstOrNull()?.id
        notifyDataSetChanged()
    }

    fun selectedType(): DrinkType? = items.firstOrNull { it.id == selectedId }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDrinkTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(private val binding: ItemDrinkTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DrinkType) {
            binding.tvName.text = item.name
            binding.ivIcon.setImageResource(IconMapper.forKey(item.iconKey))
            val isSelected = item.id == selectedId
            binding.container.setBackgroundResource(
                if (isSelected) com.watertracker.app.R.drawable.bg_chip_selected
                else com.watertracker.app.R.drawable.bg_chip_unselected
            )
            binding.root.setOnClickListener {
                selectedId = item.id
                notifyDataSetChanged()
                onSelected(item)
            }
        }
    }
}
