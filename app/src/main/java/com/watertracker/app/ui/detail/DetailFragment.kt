package com.watertracker.app.ui.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.watertracker.app.R
import com.watertracker.app.WaterTrackerApp
import com.watertracker.app.databinding.FragmentDetailBinding
import com.watertracker.app.utils.DateUtils
import com.watertracker.app.utils.DialogUtils
import com.watertracker.app.utils.IconMapper
import com.watertracker.app.utils.PreferencesManager
import com.watertracker.app.utils.UnitConverter
import com.watertracker.app.viewmodels.DetailViewModel
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Экран "Детальный просмотр" (пункт 2.1 ТЗ, экран №3):
 * все поля записи + кнопки "Редактировать"/"Удалить" (полный CRUD — Delete).
 */
class DetailFragment : Fragment(R.layout.fragment_detail) {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val entryId: Long by lazy { arguments?.getLong("entryId", -1L) ?: -1L }
    private lateinit var prefs: PreferencesManager

    private val viewModel: DetailViewModel by viewModels {
        val app = requireActivity().application as WaterTrackerApp
        DetailViewModel.Factory(app.repository, entryId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailBinding.bind(view)
        prefs = PreferencesManager(requireContext())

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                R.id.action_detail_to_addEdit,
                Bundle().apply { putLong("entryId", entryId) }
            )
        }

        binding.btnDelete.setOnClickListener {
            DialogUtils.confirmDelete(requireContext()) { viewModel.delete() }
        }

        viewModel.entry.observe(viewLifecycleOwner) { details ->
            details ?: return@observe
            bind(details)
        }

        viewModel.deleted.observe(viewLifecycleOwner) { wasDeleted ->
            if (wasDeleted) findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load() // подхватить изменения после редактирования
    }

    private fun bind(details: com.watertracker.app.database.entities.EntryWithDetails) {
        val unit = prefs.unit
        val goalMl = prefs.dailyGoalMl

        binding.ivIcon.setImageResource(IconMapper.forKey(details.drinkType.iconKey))
        binding.tvAmount.text = UnitConverter.formatAmount(details.entry.amountMl, unit)
        binding.tvTypeName.text = details.drinkType.name

        val percent = if (goalMl > 0)
            min(100, ((details.entry.amountMl.toDouble() / goalMl) * 100).roundToInt())
        else 0
        binding.progressOfGoal.progress = percent
        binding.tvPercentOfGoal.text = "$percent% от дневной цели"

        binding.rowAmountMl.setValue("${details.entry.amountMl} мл")
        binding.rowOriginalUnit.setValue(UnitConverter.formatAmount(details.entry.amountMl, unit))
        binding.rowDate.setValue(DateUtils.formatDateHuman(details.entry.timestampMillis))
        binding.rowTime.setValue(DateUtils.formatTime(details.entry.timestampMillis))
        binding.rowNote.setValue(details.entry.note?.takeIf { it.isNotBlank() } ?: "Нет заметки")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
