package com.watertracker.app.ui.main

import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.watertracker.app.R
import com.watertracker.app.WaterTrackerApp
import com.watertracker.app.databinding.FragmentMainBinding
import com.watertracker.app.utils.PreferencesManager
import com.watertracker.app.utils.UnitConverter
import com.watertracker.app.viewmodels.MainViewModel
import kotlin.math.min
import kotlin.math.roundToInt

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: EntryAdapter

    private val viewModel: MainViewModel by viewModels {
        val app = requireActivity().application as WaterTrackerApp
        MainViewModel.Factory(app.repository, PreferencesManager(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)
        prefs = PreferencesManager(requireContext())

        setupList()
        setupQuickAddButtons()
        setupSearch()
        setupFilters()
        binding.progressToday.progressDrawable = GlassProgressDrawable()
        observeData()

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_main_to_addEdit)
        }
    }

    override fun onResume() {
        super.onResume()
        // единицы измерения / сортировка могли поменяться в "Настройках"
        viewModel.refreshSortOrder()
        adapter.notifyDataSetChanged()
    }

    private fun setupList() {
        adapter = EntryAdapter(
            unitProvider = { prefs.unit },
            onClick = { item ->
                findNavController().navigate(
                    R.id.action_main_to_detail,
                    Bundle().apply { putLong("entryId", item.entry.id) }
                )
            }
        )
        binding.rvEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntries.adapter = adapter
    }

    private fun setupQuickAddButtons() {
        val amounts = listOf(150, 200, 250, 300)
        binding.quickAddContainer.removeAllViews()
        amounts.forEach { amount ->
            val tv = TextView(requireContext()).apply {
                text = "+$amount мл"
                val featured = amount == 250
                setTextColor(resources.getColor(if (featured) R.color.white else R.color.text_primary, null))
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                setBackgroundResource(if (featured) R.drawable.bg_portion_primary else R.drawable.bg_chip_unselected)
                val params = android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
                val gap = (4 * resources.displayMetrics.density).roundToInt()
                params.marginStart = if (amount == amounts.first()) 0 else gap
                params.marginEnd = if (amount == amounts.last()) 0 else gap
                layoutParams = params
                minHeight = (48 * resources.displayMetrics.density).roundToInt()
                isFocusable = true
                setOnClickListener {
                    viewModel.drinkTypes.value?.firstOrNull { it.iconKey == "water" }?.let { waterType ->
                        viewModel.quickAddWater(amount, waterType.id)
                    }
                }
            }
            binding.quickAddContainer.addView(tv)
        }
    }

    /**
     * Раздел 2.3 ТЗ: минимум 2 фильтра, работающих вместе — по типу напитка
     * и по периоду (дате). Оба фильтра одновременно применяются к запросу в БД.
     */
    private fun setupFilters() {
        // Фильтр по типу напитка ("Все" + список типов из БД)
        viewModel.drinkTypes.observe(viewLifecycleOwner) { types ->
            if (binding.chipGroupTypeFilter.childCount > 0) return@observe
            val chipAll = com.google.android.material.chip.Chip(requireContext()).apply {
                text = "Все типы"
                isCheckable = true
                isChecked = true
                tag = -1L
            }
            binding.chipGroupTypeFilter.addView(chipAll)
            types.forEach { type ->
                val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                    text = type.name
                    isCheckable = true
                    tag = type.id
                }
                binding.chipGroupTypeFilter.addView(chip)
            }
            binding.chipGroupTypeFilter.setOnCheckedStateChangeListener { group, checkedIds ->
                val checkedChip = checkedIds.firstOrNull()?.let { group.findViewById<com.google.android.material.chip.Chip>(it) }
                viewModel.setDrinkTypeFilter((checkedChip?.tag as? Long) ?: -1L)
            }
        }

        // Фильтр по периоду
        val periods = listOf("Всё время" to -1, "Сегодня" to 0, "7 дней" to 7, "30 дней" to 30)
        periods.forEach { (label, days) ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = days == -1
            }
            chip.setOnClickListener {
                val from = if (days == -1) -1L else com.watertracker.app.utils.DateUtils.daysAgoMillis(days)
                viewModel.setDateRangeFilter(from, -1L)
            }
            binding.chipGroupDateFilter.addView(chip)
        }
    }

    private fun setupSearch() {
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
        }
    }

    private fun observeData() {
        viewModel.entries.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.rvEntries.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            binding.tvEntriesCount.text = list.size.toString()
        }

        viewModel.todayTotalMl.observe(viewLifecycleOwner) { totalMl ->
            val unit = prefs.unit
            val goalMl = prefs.dailyGoalMl
            val percent = if (goalMl > 0) min(100, ((totalMl.toDouble() / goalMl) * 100).roundToInt()) else 0
            val remainingMl = (goalMl - totalMl).coerceAtLeast(0)

            binding.tvTodayAmount.text = UnitConverter.formatAmount(totalMl, unit)
            binding.tvGoalAmount.text = getString(R.string.goal_readout, UnitConverter.formatAmount(goalMl, unit))
            binding.tvPercent.text = "$percent%"
            binding.progressToday.setProgress(percent, true)
            binding.tvRemaining.text = when {
                totalMl == 0 -> getString(R.string.buddy_start)
                remainingMl == 0 -> getString(R.string.buddy_done)
                else -> getString(R.string.buddy_remaining, UnitConverter.formatAmount(remainingMl, unit))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
