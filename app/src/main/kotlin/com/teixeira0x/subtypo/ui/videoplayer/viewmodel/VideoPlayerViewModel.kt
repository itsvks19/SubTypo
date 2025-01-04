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

package com.teixeira0x.subtypo.ui.videoplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.core.subtitle.model.Cue
import com.teixeira0x.subtypo.ui.videoplayer.mvi.VideoPlayerViewEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VideoPlayerViewModel : ViewModel() {

  private val _customViewEvent = MutableSharedFlow<VideoPlayerViewEvent>()
  val customViewEvent: SharedFlow<VideoPlayerViewEvent> =
    _customViewEvent.asSharedFlow()

  var videoUri: String = ""
    private set

  var videoPosition: Long = 0L
    private set

  var cues: List<Cue> = emptyList()
    private set

  var isPlayerVisible: Boolean = true
    private set

  fun doEvent(event: VideoPlayerViewEvent) {
    viewModelScope.launch { _customViewEvent.emit(event) }
  }

  fun loadVideo(videoUri: String) {
    viewModelScope.launch {
      this@VideoPlayerViewModel.videoUri = videoUri
      _customViewEvent.emit(VideoPlayerViewEvent.LoadUri(videoUri))
    }
  }

  fun setCues(cues: List<Cue>) {
    viewModelScope.launch {
      this@VideoPlayerViewModel.cues = cues
      _customViewEvent.emit(VideoPlayerViewEvent.LoadCues(cues))
    }
  }

  fun setPlayerVisibility(visible: Boolean) {
    viewModelScope.launch {
      this@VideoPlayerViewModel.isPlayerVisible = visible
      _customViewEvent.emit(VideoPlayerViewEvent.Visibility(visible))
    }
  }

  fun saveVideoPosition(videoPosition: Long) {
    this.videoPosition = videoPosition
  }
}
