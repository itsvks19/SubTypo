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

package com.teixeira0x.subtypo.ui.projectedit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.core.domain.model.Project
import com.teixeira0x.subtypo.core.domain.usecase.project.GetProjectUseCase
import com.teixeira0x.subtypo.core.domain.usecase.project.InsertProjectUseCase
import com.teixeira0x.subtypo.core.domain.usecase.project.UpdateProjectUseCase
import com.teixeira0x.subtypo.core.domain.usecase.subtitle.InsertSubtitleUseCase
import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.mvi.ViewEvent
import com.teixeira0x.subtypo.ui.projectedit.model.SelectedVideo
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditIntent
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditViewEvent
import com.teixeira0x.subtypo.ui.projectedit.mvi.ProjectEditViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectEditViewModel
@Inject
constructor(
  private val getProjectUseCase: GetProjectUseCase,
  private val insertProjectUseCase: InsertProjectUseCase,
  private val updateProjectUseCase: UpdateProjectUseCase,
  private val insertSubtitleUseCase: InsertSubtitleUseCase,
) : ViewModel() {

  private val _projectEditorState =
    MutableStateFlow<ProjectEditViewState>(ProjectEditViewState.Loading)
  val projectEditorState: StateFlow<ProjectEditViewState> =
    _projectEditorState.asStateFlow()

  private val _viewEvent = Channel<ViewEvent>(Channel.BUFFERED)
  val viewEvent: Flow<ViewEvent> = _viewEvent.receiveAsFlow()

  private val _customViewEvent = MutableSharedFlow<ProjectEditViewEvent>()
  val customViewEvent: SharedFlow<ProjectEditViewEvent> =
    _customViewEvent.asSharedFlow()

  var selectedVideo: SelectedVideo? = null
    private set

  fun doIntent(intent: ProjectEditIntent) {
    when (intent) {
      is ProjectEditIntent.Load -> loadProject(intent)
      is ProjectEditIntent.SelectVideo -> selectVideo(intent)
      is ProjectEditIntent.Create -> insertProject(intent)
      is ProjectEditIntent.Update -> updateProject(intent)
    }
  }

  private fun loadProject(intent: ProjectEditIntent.Load) {
    _projectEditorState.value = ProjectEditViewState.Loading
    viewModelScope.launch {
      val project = getProjectUseCase(intent.id).firstOrNull()

      _projectEditorState.value = ProjectEditViewState.Loaded(project)
    }
  }

  private fun selectVideo(intent: ProjectEditIntent.SelectVideo) {
    viewModelScope.launch {
      selectedVideo = intent.video
      _customViewEvent.emit(
        ProjectEditViewEvent.UpdateSelectedVideo(intent.video)
      )
    }
  }

  private fun insertProject(intent: ProjectEditIntent.Create) {
    viewModelScope.launch {
      val name = intent.name
      val videoUri = intent.videoUri

      if (name.isEmpty() || name.isBlank()) {
        _viewEvent.send(
          ViewEvent.Toast(R.string.proj_editor_error_name_required)
        )
        return@launch
      }

      val projectId =
        insertProjectUseCase(Project(id = 0, name = name, videoUri = videoUri))

      if (projectId > 0) {
        insertSubtitleUseCase(
          Subtitle(projectId = projectId, name = "subtitle", cues = emptyList())
        )

        _customViewEvent.emit(ProjectEditViewEvent.NavigateToProject(projectId))
      }

      _viewEvent.send(
        ViewEvent.Toast(
          if (projectId > 0) {
            R.string.proj_editor_success_create
          } else {
            R.string.common_failed
          }
        )
      )
    }
  }

  private fun updateProject(intent: ProjectEditIntent.Update) {
    viewModelScope.launch {
      val id = intent.id
      val name = intent.name
      val videoUri = intent.videoUri

      if (name.isEmpty() || name.isBlank()) {
        _viewEvent.send(
          ViewEvent.Toast(R.string.proj_editor_error_name_required)
        )
        return@launch
      }

      val updated =
        updateProjectUseCase(Project(id = id, name = name, videoUri = videoUri))
      if (updated > 0) {
        _customViewEvent.emit(ProjectEditViewEvent.Dismiss)
      }

      _viewEvent.send(
        ViewEvent.Toast(
          if (updated > 0) {
            R.string.proj_editor_success_update
          } else {
            R.string.common_failed
          }
        )
      )
    }
  }
}
