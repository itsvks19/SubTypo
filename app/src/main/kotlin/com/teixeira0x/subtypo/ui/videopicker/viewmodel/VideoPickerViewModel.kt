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

package com.teixeira0x.subtypo.ui.videopicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.StringUtils
import com.teixeira0x.subtypo.core.domain.model.Album
import com.teixeira0x.subtypo.core.domain.usecase.GetAlbumsUseCase
import com.teixeira0x.subtypo.core.domain.usecase.GetVideosUseCase
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerIntent
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerViewEvent
import com.teixeira0x.subtypo.ui.videopicker.mvi.VideoPickerViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class VideoPickerViewModel
@Inject
constructor(
  private val getVideosUseCase: GetVideosUseCase,
  private val getAlbumsUseCase: GetAlbumsUseCase,
) : ViewModel() {

  private val _videoPickerViewState =
    MutableStateFlow<VideoPickerViewState>(VideoPickerViewState.Loading)
  val videoPickerViewState: StateFlow<VideoPickerViewState> =
    _videoPickerViewState.asStateFlow()

  private val _customViewEvent = MutableSharedFlow<VideoPickerViewEvent>()
  val customViewEvent: SharedFlow<VideoPickerViewEvent> =
    _customViewEvent.asSharedFlow()

  var currentAlbumId: String? = null
    private set

  fun doIntent(intent: VideoPickerIntent) {
    when (intent) {
      is VideoPickerIntent.Load -> loadAlbumsAndVideos()
      is VideoPickerIntent.LoadVideos -> loadVideos(intent)
    }
  }

  private fun loadAlbumsAndVideos() {
    _videoPickerViewState.value = VideoPickerViewState.Loading
    viewModelScope.launch(Dispatchers.IO) {
      val albums =
        getAlbumsUseCase().first().toMutableList().apply {
          add(
            0,
            Album(
              id = "",
              name = StringUtils.getString(R.string.video_picker_album_all),
            ),
          )
        }

      currentAlbumId =
        currentAlbumId.takeIf { id -> albums.any { it.id == id } }
          ?: albums.firstOrNull()?.id

      val videos = getVideosUseCase(currentAlbumId).first()

      _videoPickerViewState.value =
        if (videos.isNotEmpty()) {
          VideoPickerViewState.Loaded(albums, videos)
        } else {
          VideoPickerViewState.Error(R.string.video_picker_error_no_videos)
        }
    }
  }

  private fun loadVideos(intent: VideoPickerIntent.LoadVideos) {
    viewModelScope.launch(Dispatchers.IO) {
      _customViewEvent.emit(VideoPickerViewEvent.ShowLoading)

      val videos = getVideosUseCase(intent.albumId).first()
      currentAlbumId = intent.albumId
      _customViewEvent.emit(VideoPickerViewEvent.UpdateVideoList(videos))

      _customViewEvent.emit(VideoPickerViewEvent.HideLoading)
    }
  }
}
