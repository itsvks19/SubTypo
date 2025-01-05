/*
 * This file is part of SubTypo.
 *
 * SubTypo is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SubTypo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with SubTypo.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.teixeira0x.subtypo.ui.textedit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getFormattedTime
import com.teixeira0x.subtypo.core.subtitle.util.TimeUtils.getMilliseconds
import com.teixeira0x.subtypo.ui.common.Constants
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.databinding.FragmentSheetCueEditBinding
import com.teixeira0x.subtypo.ui.common.dialog.showConfirmDialog
import com.teixeira0x.subtypo.ui.common.fragment.BaseBottomSheetFragment
import com.teixeira0x.subtypo.ui.common.fragment.ProgressDialogFragment
import com.teixeira0x.subtypo.ui.common.util.showToastShort
import com.teixeira0x.subtypo.ui.common.validate.ValidationResult
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditIntent
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditViewEvent
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditViewState
import com.teixeira0x.subtypo.ui.textedit.util.CueFieldType
import com.teixeira0x.subtypo.ui.textedit.util.createTimeChip
import com.teixeira0x.subtypo.ui.textedit.util.decreaseTime
import com.teixeira0x.subtypo.ui.textedit.util.increaseTime
import com.teixeira0x.subtypo.ui.textedit.viewmodel.CueEditViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CueEditSheetFragment : BaseBottomSheetFragment() {

  companion object {
    @JvmStatic
    fun newInstance(
      videoPosition: Long = 0L,
      subtitleId: Long,
      cueIndex: Int = -1,
    ): CueEditSheetFragment {
      return CueEditSheetFragment().apply {
        arguments =
          Bundle().apply {
            putLong(Constants.KEY_VIDEO_POSITION_ARG, videoPosition)
            putLong(Constants.KEY_SUBTITLE_ID_ARG, subtitleId)
            putInt(Constants.KEY_CUE_INDEX_ARG, cueIndex)
          }
      }
    }
  }

  private val viewModel by viewModels<CueEditViewModel>()
  private var progress: ProgressDialogFragment? = null
  private var _binding: FragmentSheetCueEditBinding? = null
  private val binding: FragmentSheetCueEditBinding
    get() = checkNotNull(_binding) { "CueEditSheetFragment has been destroyed" }

  private var videoPosition: Long = 0L
  private var subtitleId: Long = 0L
  private var cueIndex: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      videoPosition = it.getLong(Constants.KEY_VIDEO_POSITION_ARG)
      subtitleId = it.getLong(Constants.KEY_SUBTITLE_ID_ARG)
      cueIndex = it.getInt(Constants.KEY_CUE_INDEX_ARG)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentSheetCueEditBinding.inflate(inflater)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    observeViewModel()
    configureUI()

    viewModel.doIntent(
      CueEditIntent.LoadCue(subtitleId = subtitleId, cueIndex = cueIndex)
    )
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun observeViewModel() {
    viewModel.cueEditViewState
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { state ->
        when (state) {
          is CueEditViewState.Loading -> onLoadingChange(true)
          is CueEditViewState.Loaded -> {
            onLoadingChange(false)
            onCueLoaded(state)
          }
          is CueEditViewState.Error -> {
            requireContext().showToastShort(state.message)
            dismiss()
          }
        }
      }
      .launchIn(viewLifecycleOwner.lifecycleScope)

    viewModel.customViewEvent
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { event ->
        when (event) {
          is CueEditViewEvent.ShowProgress -> showProgress(event.message)
          is CueEditViewEvent.DismissProgress -> dismissProgress()
          is CueEditViewEvent.Dismiss -> {
            dismissProgress()
            dismiss()
          }
          is CueEditViewEvent.UpdateField -> updateField(event)
        }
      }
      .launchIn(viewLifecycleOwner.lifecycleScope)
  }

  private fun onCueLoaded(state: CueEditViewState.Loaded) {
    binding.apply {
      val cue = state.cue
      if (cue != null) {
        title.setText(R.string.subtitle_cue_edit)
        tieStartTime.setText(cue.startTime.getFormattedTime())
        tieEndTime.setText(cue.endTime.getFormattedTime())
        tieText.setText(cue.text)
      } else {
        title.setText(R.string.subtitle_cue_add)
        tieStartTime.setText(videoPosition.getFormattedTime())
        tieEndTime.setText((videoPosition + 2000).getFormattedTime())
      }
    }
    validateAllFields()
  }

  private fun onLoadingChange(isLoading: Boolean) {
    setCancelable(!isLoading)
    binding.apply {
      tieStartTime.isEnabled = !isLoading
      tieEndTime.isEnabled = !isLoading
      tieText.isEnabled = !isLoading
      dialogButtons.cancel.isEnabled = !isLoading
      dialogButtons.save.isEnabled = !isLoading
    }
  }

  private fun showProgress(message: Int) {
    progress =
      ProgressDialogFragment.newInstance(
        orientation = ProgressDialogFragment.ORIENTATION_HORIZONTAL,
        style = ProgressDialogFragment.STYLE_DEFAULT,
        message = getString(message),
      )
    progress?.show(childFragmentManager, null)
  }

  private fun dismissProgress() {
    progress?.dismiss()
    progress = null
  }

  private fun configureUI() {
    configureListeners()
    configureTextWatchers()
    configureChips()
  }

  private fun configureListeners() {
    binding.apply {
      removeCue.isEnabled = cueIndex >= 0
      removeCue.setOnClickListener {
        requireContext().showConfirmDialog(
          title = R.string.remove,
          message = R.string.subtitle_cue_remove_msg,
        ) { _, _ ->
          viewModel.doIntent(
            CueEditIntent.RemoveCue(
              subtitleId = subtitleId,
              cueIndex = cueIndex,
            )
          )
        }
      }
      dialogButtons.cancel.setOnClickListener { dismiss() }
      dialogButtons.save.setOnClickListener {
        if (!isValidFields()) {
          return@setOnClickListener
        }

        val startTime = binding.tieStartTime.text.toString().getMilliseconds()
        val endTime = binding.tieEndTime.text.toString().getMilliseconds()
        val text = binding.tieText.text.toString().trim()

        viewModel.doIntent(
          if (cueIndex >= 0) {
            CueEditIntent.UpdateCue(
              subtitleId = subtitleId,
              cueIndex = cueIndex,
              startTime = startTime,
              endTime = endTime,
              text = text,
            )
          } else {
            CueEditIntent.InsertCue(
              subtitleId = subtitleId,
              startTime = startTime,
              endTime = endTime,
              text = text,
            )
          }
        )
      }
    }
  }

  private fun configureTextWatchers() {
    binding.tieStartTime.doAfterTextChanged { text ->
      viewModel.doIntent(
        CueEditIntent.ValidateCueField(
          type = CueFieldType.START_TIME,
          text = text.toString(),
        )
      )
    }

    binding.tieEndTime.doAfterTextChanged { text ->
      viewModel.doIntent(
        CueEditIntent.ValidateCueField(
          type = CueFieldType.END_TIME,
          text = text.toString(),
        )
      )
    }

    binding.tieText.doAfterTextChanged { text ->
      viewModel.doIntent(
        CueEditIntent.ValidateCueField(
          type = CueFieldType.TEXT,
          text = text.toString(),
        )
      )
    }
  }

  private fun configureChips() {
    val chipTimes = arrayOf(1, 5, 10, 15)

    chipTimes.forEach { time ->
      val chipDecrease =
        createTimeChip(requireContext(), "-${time}s") {
          onTimeChipClick(false, time.toLong())
        }
      binding.chipGroupTime.addView(chipDecrease)
    }
    chipTimes.reverse()
    chipTimes.forEach { time ->
      val chipIncrease =
        createTimeChip(requireContext(), "+${time}s") {
          onTimeChipClick(true, time.toLong())
        }
      binding.chipGroupTime.addView(chipIncrease)
    }
  }

  private fun onTimeChipClick(isIncrease: Boolean, millis: Long) {
    val focusedField =
      when {
        binding.tieStartTime.isFocused -> binding.tieStartTime
        binding.tieEndTime.isFocused -> binding.tieEndTime
        else -> null
      }

    focusedField?.apply {
      val savedSelection = selectionStart
      val updatedTime =
        if (isIncrease) {
          text.toString().increaseTime(millis)
        } else {
          text.toString().decreaseTime(millis)
        }
      setText(updatedTime)
      setSelection(savedSelection)
    }
  }

  private fun validateAllFields() {
    viewModel.doIntent(
      CueEditIntent.ValidateCueField(
        type = CueFieldType.START_TIME,
        text = binding.tieStartTime.text.toString(),
      )
    )

    viewModel.doIntent(
      CueEditIntent.ValidateCueField(
        type = CueFieldType.END_TIME,
        text = binding.tieEndTime.text.toString(),
      )
    )

    viewModel.doIntent(
      CueEditIntent.ValidateCueField(
        type = CueFieldType.TEXT,
        text = binding.tieText.text.toString(),
      )
    )
  }

  private fun updateField(event: CueEditViewEvent.UpdateField) {
    when (event.type) {
      CueFieldType.START_TIME ->
        updateFieldError(binding.tilStartTime, event.validationResult)
      CueFieldType.END_TIME ->
        updateFieldError(binding.tilEndTime, event.validationResult)
      CueFieldType.TEXT ->
        updateFieldError(binding.tilText, event.validationResult)
    }
  }

  private fun updateFieldError(
    inputLayout: TextInputLayout,
    result: ValidationResult,
  ) {
    inputLayout.isErrorEnabled = result is ValidationResult.Error
    if (result is ValidationResult.Error) {
      inputLayout.setError(result.message)
    }
    updateSaveButton()
  }

  private fun updateSaveButton() {
    binding.dialogButtons.save.isEnabled = isValidFields() && subtitleId > 0
  }

  private fun isValidFields(): Boolean {
    return (binding.tilStartTime.isErrorEnabled.not() &&
      binding.tilEndTime.isErrorEnabled.not() &&
      binding.tilText.isErrorEnabled.not())
  }
}
