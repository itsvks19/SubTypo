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

package com.teixeira0x.subtypo.ui.videoplayer.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.EVENT_AVAILABLE_COMMANDS_CHANGED
import androidx.media3.common.Player.EVENT_IS_PLAYING_CHANGED
import androidx.media3.common.Player.EVENT_PLAYBACK_STATE_CHANGED
import androidx.media3.common.Player.EVENT_PLAY_WHEN_READY_CHANGED
import androidx.media3.common.Player.Events
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue.Builder
import androidx.media3.exoplayer.ExoPlayer
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.databinding.FragmentPlayerBinding
import com.teixeira0x.subtypo.ui.videoplayer.mvi.VideoPlayerViewEvent
import com.teixeira0x.subtypo.ui.videoplayer.util.PlayerErrorMessageProvider
import com.teixeira0x.subtypo.ui.videoplayer.viewmodel.VideoPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class VideoPlayerFragment : Fragment() {

  companion object {
    private const val HORIZONTAL_ASPECT_RATIO = 1.7770000f
    private const val DEFAULT_SEEK_BACK_MS = 5_000L
    private const val DEFAULT_SEEK_FOWARD_MS = 5_000L
  }

  private val mainHandler = Handler(Looper.getMainLooper())
  private val viewModel by activityViewModels<VideoPlayerViewModel>()

  private var _binding: FragmentPlayerBinding? = null
  private val binding: FragmentPlayerBinding
    get() = checkNotNull(_binding) { "VideoPlayerFragment has been destroyed" }

  private var updateSubtitleAction: Runnable? = null
  private var player: ExoPlayer? = null

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentPlayerBinding.inflate(inflater, container, false)
      .also { _binding = it }
      .root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    releasePlayer()
    _binding = null
  }

  override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
    binding.playerView.setErrorMessageProvider(
      PlayerErrorMessageProvider(requireContext())
    )
    binding.playerView.controllerShowTimeoutMs = 2000
    binding.playerView.useController = true
    observeViewModel()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    saveVideoPosition()
  }

  override fun onStart() {
    super.onStart()
    initializePlayer()
    _binding?.playerView?.onResume()
  }

  override fun onStop() {
    super.onStop()
    _binding?.playerView?.onPause()
    releasePlayer()
  }

  private fun observeViewModel() {
    viewModel.customViewEvent
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { event ->
        when (event) {
          is VideoPlayerViewEvent.LoadUri -> prepareMedia(event.videoUri)
          is VideoPlayerViewEvent.LoadCues -> updateSubtitle()
          is VideoPlayerViewEvent.Visibility -> {
            if (event.visible) {
              initializePlayer()
            } else releasePlayer()
          }
          is VideoPlayerViewEvent.Pause -> pauseVideo()
          is VideoPlayerViewEvent.Play -> playVideo()
        }
      }
      .launchIn(viewLifecycleOwner.lifecycleScope)
  }

  private fun initializePlayer() {
    if (player == null) {
      updateSubtitleAction = Runnable { updateSubtitle() }
      _binding?.playerView?.player =
        ExoPlayer.Builder(requireContext())
          .setSeekBackIncrementMs(DEFAULT_SEEK_BACK_MS)
          .setSeekForwardIncrementMs(DEFAULT_SEEK_FOWARD_MS)
          .build()
          .also { player = it }
      player?.addListener(Listener())
    }

    prepareMedia(viewModel.videoUri)
  }

  private fun releasePlayer() {
    updateSubtitleAction?.let { mainHandler.removeCallbacks(it) }
    updateSubtitleAction = null
    saveVideoPosition()
    player?.release()
    player = null
  }

  private fun prepareMedia(videoUri: String) {
    player?.apply {
      clearMediaItems()
      if (videoUri.isNotEmpty()) {
        setMediaItem(MediaItem.fromUri(videoUri))
        prepare()

        seekTo(viewModel.videoPosition)
      }
    }
  }

  private fun saveVideoPosition() {
    player?.let { viewModel.saveVideoPosition(it.currentPosition) }
  }

  private fun playVideo() {
    player?.play()
  }

  private fun pauseVideo() {
    player?.pause()
    saveVideoPosition()
  }

  private fun updateSubtitle() {
    val player = player
    val cues = viewModel.cues
    if (cues.isEmpty() || player == null) {
      return
    }

    val currentPosition = player.currentPosition
    val filteredCues =
      cues.filter {
        it.startTime <= currentPosition && it.endTime >= currentPosition
      }

    binding.playerView.subtitleView?.setCues(
      filteredCues.map { Builder().setText(it.text).build() }
    )

    if (player.isPlaying()) {
      updateSubtitleAction?.let { mainHandler.post(it) }
    }
  }

  inner class Listener : Player.Listener {

    override fun onVideoSizeChanged(videoSize: VideoSize) {
      val orientation = resources.configuration.orientation
      if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
        val width = videoSize.width
        val height = videoSize.height
        val videoAspectRatio: Float =
          if (height == 0 || width == 0) 0F
          else (width * videoSize.pixelWidthHeightRatio) / height

        _binding?.root?.apply {
          layoutParams.height =
            if (videoAspectRatio <= HORIZONTAL_ASPECT_RATIO) {
              resources.getDimensionPixelSize(R.dimen.video_view_height_max)
            } else ViewGroup.LayoutParams.WRAP_CONTENT

          requestLayout()
        }
      }
    }

    override fun onEvents(player: Player, events: Events) {
      if (
        events.containsAny(
          EVENT_PLAYBACK_STATE_CHANGED,
          EVENT_PLAY_WHEN_READY_CHANGED,
          EVENT_IS_PLAYING_CHANGED,
          EVENT_AVAILABLE_COMMANDS_CHANGED,
        )
      ) {
        updateSubtitleAction?.let { mainHandler.post(it) }
      }
    }
  }
}
