package com.watertracker.app.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.chip.Chip
import com.watertracker.app.R
import com.watertracker.app.WaterTrackerApp
import com.watertracker.app.databinding.FragmentStatsBinding
import com.watertracker.app.utils.DateUtils
import com.watertracker.app.utils.PreferencesManager
import com.watertracker.app.utils.UnitConverter
import com.watertracker.app.viewmodels.StatsUiState
import com.watertracker.app.viewmodels.StatsViewModel

/**
 * Экран "Статистика/Аналитика" (пункт 2.1 ТЗ, экран №4).
 * Столбчатая диаграмма (MPAndroidChart) ежедневного потребления за
 * выбранный период + 4 числовых показателя, на основе реальных данных.
 */
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels {
        val app = requireActivity().application as WaterTrackerApp
        StatsViewModel.Factory(app.repository, PreferencesManager(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatsBinding.bind(view)

        setupChartStyle()
        setupPeriodChips()
        observe()
    }

    private fun setupPeriodChips() {
        val periods = listOf("7 дн." to 7, "14 дн." to 14, "30 дн." to 30)
        binding.chipGroupPeriod.removeAllViews()
        periods.forEachIndexed { index, (label, days) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setOnClickListener { viewModel.load(days) }
            }
            binding.chipGroupPeriod.addView(chip)
        }
    }

    private fun setupChartStyle() {
        with(binding.barChart) {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setFitBars(true)
            setScaleEnabled(false)
            setTouchEnabled(true)
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            xAxis.granularity = 1f
            setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun observe() {
        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
    }

    private fun render(state: StatsUiState) {
        val unit = viewModel.currentUnit()

        binding.cardAvg.tvStatValue.text = UnitConverter.formatAmount(state.averageMl, unit)
        binding.cardAvg.tvStatLabel.text = getString(R.string.stats_avg)
        binding.cardAvg.ivStatIcon.setImageResource(R.drawable.ic_water_drop)

        binding.cardDaysGoal.tvStatValue.text = "${state.daysGoalMet} / ${state.periodDays}"
        binding.cardDaysGoal.tvStatLabel.text = getString(R.string.stats_days_goal)
        binding.cardDaysGoal.ivStatIcon.setImageResource(R.drawable.ic_check_circle)

        binding.cardTotal.tvStatValue.text = UnitConverter.formatAmount(state.totalMl, unit)
        binding.cardTotal.tvStatLabel.text = getString(R.string.stats_total)
        binding.cardTotal.ivStatIcon.setImageResource(R.drawable.ic_bar_chart)

        binding.cardBestDay.tvStatValue.text = UnitConverter.formatAmount(state.bestDayMl, unit)
        binding.cardBestDay.tvStatLabel.text = getString(R.string.stats_best_day)
        binding.cardBestDay.ivStatIcon.setImageResource(R.drawable.ic_star)

        renderChart(state)
    }

    private fun renderChart(state: StatsUiState) {
        if (state.dailyTotals.isEmpty()) {
            binding.barChart.visibility = View.GONE
            binding.chartEmptyState.visibility = View.VISIBLE
            return
        }
        binding.barChart.visibility = View.VISIBLE
        binding.chartEmptyState.visibility = View.GONE

        val entries = state.dailyTotals.mapIndexed { index, daily ->
            BarEntry(index.toFloat(), daily.total.toFloat())
        }
        val labels = state.dailyTotals.map {
            DateUtils.formatDayShort(it.dayBucket * 86_400_000L)
        }

        val accent = ContextCompat.getColor(requireContext(), R.color.accent_blue)
        val dataSet = BarDataSet(entries, "Потребление").apply {
            color = accent
            setDrawValues(false)
            highLightColor = accent
        }

        binding.barChart.xAxis.valueFormatter =
            com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
        binding.barChart.data = BarData(dataSet).apply { barWidth = 0.6f }

        // Пунктирная линия дневной цели
        binding.barChart.axisLeft.removeAllLimitLines()
        val goalLine = LimitLine(state.goalMl.toFloat(), "Цель: ${UnitConverter.formatAmount(state.goalMl, viewModel.currentUnit())}").apply {
            lineColor = Color.parseColor("#4E5A68")
            lineWidth = 1.2f
            enableDashedLine(10f, 6f, 0f)
            textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            textSize = 10f
        }
        binding.barChart.axisLeft.addLimitLine(goalLine)
        binding.barChart.animateY(500)
        binding.barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
