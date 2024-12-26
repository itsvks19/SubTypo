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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.TooltipCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.UriUtils
import com.teixeira0x.subtypo.core.domain.model.Project
import com.teixeira0x.subtypo.ui.activity.Navigator.navigateToProjectActivity
import com.teixeira0x.subtypo.ui.common.Constants
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.databinding.FragmentProjectEditorBinding
import com.teixeira0x.subtypo.ui.common.fragment.BaseBottomSheetFragment
import com.teixeira0x.subtypo.ui.common.mvi.ViewEvent
import com.teixeira0x.subtypo.ui.common.permission.PermissionResultContract
import com.teixeira0x.subtypo.ui.common.permission.PermissionResultContract.Companion.isPermissionsGranted
import com.teixeira0x.subtypo.ui.common.utils.VideoUtils.getVideoThumbnail
import com.teixeira0x.subtypo.ui.common.utils.showToastShort
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditorIntent
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditorViewEvent
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditorViewState
import com.teixeira0x.subtypo.ui.projectedit.viewmodel.ProjectEditorViewModel
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

  private var _binding: FragmentProjectEditorBinding? = null
  private val binding: FragmentProjectEditorBinding
    get() =
      checkNotNull(_binding) { "ProjectEditSheetFragment has been destroyed!" }

  private val viewModel by viewModels<ProjectEditorViewModel>()

  private val videoPickerLauncher =
    registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      uri?.also { viewModel.doIntent(ProjectEditorIntent.UpdateVideoUri(uri)) }
    }

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
    return FragmentProjectEditorBinding.inflate(inflater)
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

      videoPickerLauncher.launch(arrayOf("video/*"))
    }

    binding.imgRemoveVideo.setOnClickListener {
      if (viewModel.videoUri != null) {
        viewModel.doIntent(ProjectEditorIntent.UpdateVideoUri(null))
      }
    }

    binding.dialogButtons.cancel.setOnClickListener { dismiss() }
    binding.dialogButtons.save.setOnClickListener { createProject() }
    viewModel.doIntent(ProjectEditorIntent.Load(projectId))
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun observeViewModel() {
    viewModel.projectEditorState
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { state ->
        onLoadingChange(state is ProjectEditorViewState.Loading)
        when (state) {
          is ProjectEditorViewState.Loading -> Unit
          is ProjectEditorViewState.Loaded -> onLoaded(state.project)
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
          is ProjectEditorViewEvent.UpdateVideo -> {
            val file = event.videoFile

            binding.imgVideoThumbnail.setImageBitmap(
              requireContext().getVideoThumbnail(file?.absolutePath ?: "")
            )
            binding.tvVideoName.setText(
              file?.name ?: getString(R.string.proj_editor_video_no_select)
            )
          }
          is ProjectEditorViewEvent.Dismiss -> dismiss()
          is ProjectEditorViewEvent.NavigateToProject -> {
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
        viewModel.doIntent(
          ProjectEditorIntent.UpdateVideoUri(File(project.videoUri).toUri())
        )
      }
    }
  }

  private fun createProject() {
    val name = binding.tieName.text.toString().trim()
    val videoUri = UriUtils.uri2File(viewModel.videoUri)?.absolutePath ?: ""

    viewModel.doIntent(
      if (projectId > 0) {
        ProjectEditorIntent.Update(
          id = projectId,
          name = name,
          videoUri = videoUri,
        )
      } else {
        ProjectEditorIntent.Create(name = name, videoUri = videoUri)
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
