package com.watertracker.app.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.watertracker.app.R
import com.watertracker.app.databinding.FragmentSettingsBinding
import com.watertracker.app.utils.Constants
import com.watertracker.app.utils.PreferencesManager
import com.watertracker.app.utils.ReminderScheduler
import com.watertracker.app.viewmodels.SettingsViewModel

/**
 * Экран "Настройки" (пункт 2.1 ТЗ, экран №5): минимум 3 настройки —
 * здесь их 4 (дневная норма, единицы измерения, сортировка, напоминания).
 * Все изменения сразу сохраняются в SharedPreferences через ViewModel.
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(PreferencesManager(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupDailyGoal()
        setupUnits()
        setupSort()
        setupReminders()
    }

    private fun setupDailyGoal() {
        val current = viewModel.getDailyGoal()
        binding.sliderDailyGoal.value = current.toFloat()
        binding.tvDailyGoalValue.text = "$current мл"

        binding.sliderDailyGoal.addOnChangeListener { _, value, fromUser ->
            binding.tvDailyGoalValue.text = "${value.toInt()} мл"
            if (fromUser) viewModel.setDailyGoal(value.toInt())
        }
    }

    private fun setupUnits() {
        val checkedId = when (viewModel.getUnit()) {
            Constants.UNIT_ML -> R.id.btn_unit_ml
            Constants.UNIT_OZ -> R.id.btn_unit_oz
            else -> R.id.btn_unit_cups
        }
        binding.toggleUnits.check(checkedId)

        binding.toggleUnits.addOnButtonCheckedListener { _, checkedButtonId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val unit = when (checkedButtonId) {
                R.id.btn_unit_ml -> Constants.UNIT_ML
                R.id.btn_unit_oz -> Constants.UNIT_OZ
                else -> Constants.UNIT_CUPS
            }
            viewModel.setUnit(unit)
        }
    }

    private fun setupSort() {
        fun refresh() {
            val order = viewModel.getSortOrder()
            binding.rowSortNewest.setSelected(order == Constants.SORT_NEWEST)
            binding.rowSortOldest.setSelected(order == Constants.SORT_OLDEST)
            binding.rowSortAmount.setSelected(order == Constants.SORT_AMOUNT)
        }
        refresh()
        binding.rowSortNewest.setOnClickListener { viewModel.setSortOrder(Constants.SORT_NEWEST); refresh() }
        binding.rowSortOldest.setOnClickListener { viewModel.setSortOrder(Constants.SORT_OLDEST); refresh() }
        binding.rowSortAmount.setOnClickListener { viewModel.setSortOrder(Constants.SORT_AMOUNT); refresh() }
    }

    private fun setupReminders() {
        binding.switchReminders.isChecked = viewModel.getRemindersEnabled()
        binding.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setRemindersEnabled(isChecked)
            if (isChecked) {
                ReminderScheduler.schedule(requireContext())
            } else {
                ReminderScheduler.cancel(requireContext())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
