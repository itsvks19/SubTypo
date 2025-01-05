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

package com.teixeira0x.subtypo.ui.textedit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.core.domain.usecase.subtitle.GetSubtitleUseCase
import com.teixeira0x.subtypo.core.domain.usecase.subtitle.UpdateSubtitleUseCase
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditIntent
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditViewEvent
import com.teixeira0x.subtypo.ui.textedit.mvi.CueEditViewState
import com.teixeira0x.subtypo.ui.textedit.util.CueFieldType
import com.teixeira0x.subtypo.ui.textedit.validate.CueValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class CueEditViewModel
@Inject
constructor(
  private val getSubtitleUseCase: GetSubtitleUseCase,
  private val updateSubtitleUseCase: UpdateSubtitleUseCase,
  private val cueValidator: CueValidator,
) : ViewModel() {

  private val _cueEditViewState =
    MutableStateFlow<CueEditViewState>(CueEditViewState.Loading)
  val cueEditViewState: StateFlow<CueEditViewState> =
    _cueEditViewState.asStateFlow()

  private val _customViewEvent = MutableSharedFlow<CueEditViewEvent>()
  val customViewEvent: SharedFlow<CueEditViewEvent> =
    _customViewEvent.asSharedFlow()

  private var subtitle: Subtitle? = null

  fun doIntent(event: CueEditIntent) {
    when (event) {
      is CueEditIntent.LoadCue -> loadCue(event)
      is CueEditIntent.InsertCue -> insertCue(event)
      is CueEditIntent.UpdateCue -> updateCue(event)
      is CueEditIntent.RemoveCue -> removeCue(event)
      is CueEditIntent.ValidateCueField -> validateCueField(event)
    }
  }

  private fun loadCue(event: CueEditIntent.LoadCue) {
    _cueEditViewState.value = CueEditViewState.Loading
    viewModelScope.launch {
      val subtitle = getSubtitleUseCase(event.subtitleId).first()
      this@CueEditViewModel.subtitle = subtitle

      _cueEditViewState.value =
        if (subtitle != null) {
          CueEditViewState.Loaded(subtitle.cues.getOrNull(event.cueIndex))
        } else CueEditViewState.Error(R.string.subtitle_error_not_found)
    }
  }

  private fun insertCue(event: CueEditIntent.InsertCue) {
    viewModelScope.launch {
      val currentSubtitle =
        subtitle
          ?: run {
            _cueEditViewState.value =
              CueEditViewState.Error(R.string.subtitle_error_not_found)
            return@launch
          }

      val cues = currentSubtitle.cues.toMutableList()
      cues.add(
        Cue(
          startTime = event.startTime,
          endTime = event.endTime,
          text = event.text,
        )
      )

      updateSubtitleUseCase(currentSubtitle.copy(cues = cues))

      _customViewEvent.emit(CueEditViewEvent.Dismiss)
    }
  }

  private fun updateCue(event: CueEditIntent.UpdateCue) {
    viewModelScope.launch {
      val currentSubtitle =
        subtitle
          ?: run {
            _cueEditViewState.value =
              CueEditViewState.Error(R.string.subtitle_error_not_found)
            return@launch
          }

      val cues = currentSubtitle.cues.toMutableList()

      cues[event.cueIndex] =
        Cue(
          startTime = event.startTime,
          endTime = event.endTime,
          text = event.text,
        )

      updateSubtitleUseCase(currentSubtitle.copy(cues = cues))

      _customViewEvent.emit(CueEditViewEvent.Dismiss)
    }
  }

  private fun removeCue(event: CueEditIntent.RemoveCue) {
    viewModelScope.launch {
      val currentSubtitle =
        subtitle
          ?: run {
            _cueEditViewState.value =
              CueEditViewState.Error(R.string.subtitle_error_not_found)
            return@launch
          }

      _customViewEvent.emit(
        CueEditViewEvent.ShowProgress(R.string.subtitle_cue_removing)
      )

      val cues = currentSubtitle.cues.toMutableList()

      cues.removeAt(event.cueIndex)

      updateSubtitleUseCase(currentSubtitle.copy(cues = cues))

      _customViewEvent.emit(CueEditViewEvent.Dismiss)
    }
  }

  private fun validateCueField(event: CueEditIntent.ValidateCueField) {
    viewModelScope.launch {
      val validationResult =
        when (event.type) {
          CueFieldType.START_TIME,
          CueFieldType.END_TIME -> cueValidator.checkTime(event.text)
          CueFieldType.TEXT -> cueValidator.checkText(event.text)
        }

      _customViewEvent.emit(
        CueEditViewEvent.UpdateField(event.type, validationResult)
      )
    }
  }
}
