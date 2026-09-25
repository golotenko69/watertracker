package com.watertracker.app.ui.addedit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.watertracker.app.R
import com.watertracker.app.WaterTrackerApp
import com.watertracker.app.databinding.FragmentAddEditBinding
import com.watertracker.app.viewmodels.AddEditViewModel

/**
 * Экран "Добавление/Редактирование" (пункт 2.1 ТЗ, экран №2).
 * В зависимости от аргумента entryId работает и как форма добавления,
 * и как форма редактирования существующей записи (пункт 2.3 CRUD).
 */
class AddEditFragment : Fragment(R.layout.fragment_add_edit) {

    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!

    private val entryId: Long by lazy { arguments?.getLong("entryId", -1L) ?: -1L }

    private lateinit var typeAdapter: DrinkTypeAdapter

    private val viewModel: AddEditViewModel by viewModels {
        val app = requireActivity().application as WaterTrackerApp
        AddEditViewModel.Factory(app.repository, entryId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditBinding.bind(view)

        binding.tvTitle.text = if (viewModel.isEditMode)
            getString(R.string.add_edit_title_edit) else getString(R.string.add_edit_title_add)
        binding.btnSave.text = if (viewModel.isEditMode)
            getString(R.string.save) else getString(R.string.add)

        setupTypeGrid()
        setupAmountChips()
        setupNoteCounter()
        setupButtons()
        observeViewModel()
    }

    private fun setupTypeGrid() {
        typeAdapter = DrinkTypeAdapter(onSelected = { clearAmountError() })
        binding.rvDrinkTypes.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvDrinkTypes.adapter = typeAdapter
    }

    private fun setupAmountChips() {
        val amounts = listOf(150, 200, 250, 300, 350, 500)
        binding.chipGroupAmounts.removeAllViews()
        amounts.forEach { amount ->
            val chip = Chip(requireContext()).apply {
                text = amount.toString()
                isCheckable = true
                setTextColor(resources.getColor(R.color.text_primary, null))
                setOnClickListener {
                    binding.etAmount.setText(amount.toString())
                    binding.etAmount.setSelection(binding.etAmount.text.length)
                }
            }
            binding.chipGroupAmounts.addView(chip)
        }

        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearAmountError()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupNoteCounter() {
        binding.etNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvNoteCounter.text = "${s?.length ?: 0}/200"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }
        binding.btnSave.setOnClickListener {
            val typeId = typeAdapter.selectedType()?.id
            viewModel.save(
                drinkTypeId = typeId,
                amountText = binding.etAmount.text.toString(),
                note = binding.etNote.text.toString().takeIf { it.isNotBlank() }
            )
        }
    }

    private fun observeViewModel() {
        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.progressSaving.visibility = if (saving) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !saving
            binding.btnCancel.isEnabled = !saving
        }

        viewModel.drinkTypes.observe(viewLifecycleOwner) { types ->
            val preselect = viewModel.existingEntry.value?.drinkType?.id
            typeAdapter.submitList(types, preselect)
        }

        viewModel.existingEntry.observe(viewLifecycleOwner) { details ->
            details ?: return@observe
            typeAdapter.submitList(viewModel.drinkTypes.value ?: emptyList(), details.drinkType.id)
            binding.etAmount.setText(details.entry.amountMl.toString())
            binding.etNote.setText(details.entry.note)
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddEditViewModel.SaveResult.Success -> {
                    findNavController().popBackStack()
                }
                is AddEditViewModel.SaveResult.Error -> {
                    showFieldError(result.message)
                }
                null -> Unit
            }
            viewModel.consumeSaveResult()
        }
    }

    /** Раздел 2.4 ТЗ: подсветка поля красным + всплывающее сообщение (Snackbar). */
    private fun showFieldError(message: String) {
        binding.tvAmountError.text = message
        binding.tvAmountError.visibility = View.VISIBLE
        binding.etAmount.setBackgroundResource(R.drawable.bg_input_field_error)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun clearAmountError() {
        binding.tvAmountError.visibility = View.GONE
        binding.etAmount.setBackgroundResource(R.drawable.bg_input_field)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
