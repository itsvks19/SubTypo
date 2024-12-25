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

package com.teixeira0x.subtypo.ui.projectlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.core.domain.usecase.project.GetAllProjectsUseCase
import com.teixeira0x.subtypo.core.domain.usecase.project.RemoveProjectUseCase
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.mvi.ViewEvent
import com.teixeira0x.subtypo.ui.projectlist.mvi.ProjectListIntent
import com.teixeira0x.subtypo.ui.projectlist.mvi.ProjectListViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectListViewModel
@Inject
constructor(
  private val getAllProjectsUseCase: GetAllProjectsUseCase,
  private val removeProjectUseCase: RemoveProjectUseCase,
) : ViewModel() {

  private val _projectListViewState =
    MutableStateFlow<ProjectListViewState>(ProjectListViewState.Loading)
  val projectListViewState: StateFlow<ProjectListViewState>
    get() = _projectListViewState.asStateFlow()

  private val _viewEvent = Channel<ViewEvent>(Channel.BUFFERED)
  val viewEvent: Flow<ViewEvent> = _viewEvent.receiveAsFlow()

  fun doIntent(event: ProjectListIntent) {
    when (event) {
      is ProjectListIntent.Refresh -> refreshProjects()
      is ProjectListIntent.DeleteProject -> deleteProject(event)
    }
  }

  private fun refreshProjects() {
    _projectListViewState.value = ProjectListViewState.Loading
    viewModelScope.launch {
      getAllProjectsUseCase().collect { projects ->
        _projectListViewState.value = ProjectListViewState.Loaded(projects)
      }
    }
  }

  private fun deleteProject(event: ProjectListIntent.DeleteProject) {
    viewModelScope.launch {
      val rowsDeleted = removeProjectUseCase(event.projectId)

      _viewEvent.send(
        ViewEvent.Toast(
          if (rowsDeleted > 0) {
            R.string.proj_deleted
          } else R.string.common_failed
        )
      )
    }
  }
}
