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

package com.teixeira0x.subtypo.ui.projectedit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.teixeira0x.subtypo.core.domain.model.Project
import com.teixeira0x.subtypo.ui.activity.Navigator.navigateToProjectActivity
import com.teixeira0x.subtypo.ui.common.Constants
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.databinding.FragmentSheetProjectEditBinding
import com.teixeira0x.subtypo.ui.common.fragment.BaseBottomSheetFragment
import com.teixeira0x.subtypo.ui.common.mvi.ViewEvent
import com.teixeira0x.subtypo.ui.common.permission.PermissionResultContract
import com.teixeira0x.subtypo.ui.common.permission.PermissionResultContract.Companion.isPermissionsGranted
import com.teixeira0x.subtypo.ui.common.util.showToastShort
import com.teixeira0x.subtypo.ui.projectedit.model.SelectedVideo
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditIntent
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditViewEvent
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditViewState
import com.teixeira0x.subtypo.ui.projectedit.viewmodel.ProjectEditViewModel
import com.teixeira0x.subtypo.ui.videopicker.fragment.VideoPickerSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ProjectEditSheetFragment : BaseBottomSheetFragment() {

  companion object {

    @JvmStatic
    fun newInstance(projectId: Long = 0): ProjectEditSheetFragment {
      return ProjectEditSheetFragment().also {
        it.arguments =
          Bundle().apply { putLong(Constants.KEY_PROJECT_ID_ARG, projectId) }
      }
    }
  }

  private var _binding: FragmentSheetProjectEditBinding? = null
  private val binding: FragmentSheetProjectEditBinding
    get() =
      checkNotNull(_binding) { "ProjectEditSheetFragment has been destroyed!" }

  private val viewModel by viewModels<ProjectEditViewModel>()

  private lateinit var permissionContract: PermissionResultContract
  private var projectId: Long = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    permissionContract = PermissionResultContract(this)

    projectId = arguments?.getLong(Constants.KEY_PROJECT_ID_ARG) ?: 0
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentSheetProjectEditBinding.inflate(inflater)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    observeViewModel()

    TooltipCompat.setTooltipText(
      binding.imgRemoveVideo,
      getString(R.string.proj_editor_video_remove),
    )

    binding.cardSelectVideo.setOnClickListener {
      if (!requireContext().isPermissionsGranted()) {
        permissionContract.requestPermissions()
        return@setOnClickListener
      }

      VideoPickerSheetFragment.newSingleChoice { video ->
          viewModel.doIntent(
            ProjectEditIntent.SelectVideo(
              SelectedVideo(title = video.title, path = video.path)
            )
          )
        }
        .show(childFragmentManager, "VideoPickerSheetFragment")
    }

    binding.imgRemoveVideo.setOnClickListener {
      if (viewModel.selectedVideo != null) {
        viewModel.doIntent(ProjectEditIntent.SelectVideo(null))
      }
    }

    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener { createProject() }
    viewModel.doIntent(ProjectEditIntent.Load(projectId))
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun observeViewModel() {
    viewModel.projectEditorState
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { state ->
        onLoadingChange(state is ProjectEditViewState.Loading)
        when (state) {
          is ProjectEditViewState.Loading -> Unit
          is ProjectEditViewState.Loaded -> onLoaded(state.project)
        }
      }
      .launchIn(viewLifecycleOwner.lifecycleScope)

    viewModel.viewEvent
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { event ->
        when (event) {
          is ViewEvent.Toast -> requireContext().showToastShort(event.message)
        }
      }
      .launchIn(viewLifecycleOwner.lifecycleScope)

    viewModel.customViewEvent
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { event ->
        when (event) {
          is ProjectEditViewEvent.UpdateSelectedVideo -> {
            val video = event.video

            Glide.with(requireContext())
              .load(video?.path ?: "")
              .into(binding.imgVideoThumbnail)

            binding.tvVideoTitle.setText(
              video?.title ?: getString(R.string.proj_editor_video_no_select)
            )
          }
          is ProjectEditViewEvent.Dismiss -> dismiss()
          is ProjectEditViewEvent.NavigateToProject -> {
            navigateToProjectActivity(requireContext(), event.projectId)
            dismiss()
          }
        }
      }
      .launchIn(viewLifecycleOwner.lifecycleScope)
  }

  private fun onLoaded(project: Project?) {
    binding.apply {
      if (project == null) {
        tvTitle.setText(R.string.proj_editor_create)
      } else {
        tvTitle.setText(R.string.proj_editor_edit)
        tieName.setText(project.name)

        if (project.videoUri.isNotEmpty()) {
          val file = File(project.videoUri)
          viewModel.doIntent(
            ProjectEditIntent.SelectVideo(
              SelectedVideo(title = file.name, path = file.absolutePath)
            )
          )
        }
      }
    }
  }

  private fun createProject() {
    val name = binding.tieName.text.toString().trim()
    val videoUri = viewModel.selectedVideo?.path ?: ""

    viewModel.doIntent(
      if (projectId > 0) {
        ProjectEditIntent.Update(
          id = projectId,
          name = name,
          videoUri = videoUri,
        )
      } else {
        ProjectEditIntent.Create(name = name, videoUri = videoUri)
      }
    )
  }

  private fun onLoadingChange(isLoading: Boolean) {
    binding.cardSelectVideo.isClickable = !isLoading
    binding.imgRemoveVideo.isClickable = !isLoading
    binding.tieName.isEnabled = !isLoading
    binding.dialogButtons.cancel.isEnabled = !isLoading
    binding.dialogButtons.save.isEnabled = !isLoading
    setCancelable(!isLoading)
  }
}
