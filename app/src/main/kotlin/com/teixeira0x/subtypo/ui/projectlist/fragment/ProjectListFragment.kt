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

package com.teixeira0x.subtypo.ui.projectlist.fragment

import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.DefaultSelectionTracker
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.teixeira0x.subtypo.core.domain.model.Project
import com.teixeira0x.subtypo.ui.activity.Navigator.navigateToProjectActivity
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.base.Selectable
import com.teixeira0x.subtypo.ui.common.databinding.FragmentProjectListBinding
import com.teixeira0x.subtypo.ui.common.dialog.showConfirmDialog
import com.teixeira0x.subtypo.ui.common.util.showToastShort
import com.teixeira0x.subtypo.ui.optionlist.dialog.showOptionListDialog
import com.teixeira0x.subtypo.ui.optionlist.model.OptionItem
import com.teixeira0x.subtypo.ui.projectedit.fragment.ProjectEditSheetFragment
import com.teixeira0x.subtypo.ui.projectlist.adapter.ProjectClickListener
import com.teixeira0x.subtypo.ui.projectlist.adapter.ProjectListAdapter
import com.teixeira0x.subtypo.ui.projectlist.mvi.ProjectListIntent
import com.teixeira0x.subtypo.ui.projectlist.mvi.ProjectListViewState
import com.teixeira0x.subtypo.ui.projectlist.util.ProjectKeyProvider
import com.teixeira0x.subtypo.ui.projectlist.viewmodel.ProjectListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ProjectListFragment : Fragment(), ProjectClickListener, Selectable {

  private var _binding: FragmentProjectListBinding? = null
  private val binding: FragmentProjectListBinding
    get() = checkNotNull(_binding) { "ProjectsFragment has been destroyed!" }

  private val viewModel by
    viewModels<ProjectListViewModel>(ownerProducer = { requireActivity() })

  private var selectionActionMode: SelectionActionMode? = null
  private lateinit var selectionTracker: SelectionTracker<Long>
  private lateinit var projectsAdapter: ProjectListAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    return FragmentProjectListBinding.inflate(inflater, container, false)
      .also { _binding = it }
      .root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    observeViewModel()
    configureUI()

    viewModel.doIntent(ProjectListIntent.Refresh)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun observeViewModel() {
    viewModel.projectListViewState
      .flowWithLifecycle(viewLifecycleOwner.lifecycle)
      .onEach { state ->
        when (state) {
          is ProjectListViewState.Loading -> onChangeLoadingState(true, true)
          is ProjectListViewState.Loaded -> {
            onChangeLoadingState(false, state.projects.isEmpty())
            projectsAdapter.submitList(
              state.projects.sortedByDescending { it.id }
            )
          }
          is ProjectListViewState.Error -> Unit
        }
      }
      .launchIn(viewLifecycleOwner.lifecycleScope)
  }

  private fun onChangeLoadingState(isLoading: Boolean, isEmptyList: Boolean) =
    binding.apply {
      if (isLoading) fabCreateProject.hide() else fabCreateProject.show()
      noProjects.isVisible = !isLoading && isEmptyList
      rvProjects.isVisible = !isLoading
      progress.isVisible = isLoading
    }

  private fun configureUI() {
    binding.apply {
      fabCreateProject.setOnClickListener { showEditProjectSheet() }

      rvProjects.layoutManager = LinearLayoutManager(requireContext())
      rvProjects.adapter =
        ProjectListAdapter(
            DefaultSelectionTracker<Long>(
                "DefaultSelectionTracker",
                ProjectKeyProvider(binding.rvProjects),
                SelectionPredicates.createSelectAnything(),
                StorageStrategy.createLongStorage(),
              )
              .also { selectionTracker = it },
            this@ProjectListFragment,
          )
          .also { projectsAdapter = it }
    }

    selectionTracker.addObserver(
      object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionCleared() {
          val selection = projectsAdapter.selection(selectionTracker.selection)
        }

        override fun onSelectionChanged() {
          val selection = projectsAdapter.selection(selectionTracker.selection)
          requireContext().showToastShort(selection.toString())
        }
      }
    )
  }

  override fun onProjectClickListener(view: View, project: Project) {
    navigateToProjectActivity(requireContext(), project.id)
  }

  override fun onProjectLongClickListener(
    view: View,
    project: Project,
  ): Boolean {
    showProjectMenu(view, project)
    return true
  }

  override fun onProjectMenuClickListener(view: View, project: Project) {
    showProjectMenu(view, project)
  }

  private fun showProjectMenu(view: View, project: Project) {
    showOptionListDialog(
      context = requireContext(),
      options =
        listOf(
          OptionItem(R.drawable.ic_pencil, getString(R.string.edit)),
          OptionItem(R.drawable.ic_delete, getString(R.string.delete)),
        ),
    ) { position, _ ->
      when (position) {
        0 -> showEditProjectSheet(project.id)
        1 -> {
          requireContext().showConfirmDialog(
            title = getString(R.string.delete),
            message = getString(R.string.msg_delete_confirmation, project.name),
          ) { _, _ ->
            viewModel.doIntent(ProjectListIntent.DeleteProject(project.id))
          }
        }
      }
    }
  }

  private fun showEditProjectSheet(projectId: Long = 0) {
    ProjectEditSheetFragment.newInstance(projectId)
      .show(childFragmentManager, null)
  }

  override fun onSelect() {
    // Nothing
  }

  override fun onUnselect() {
    // Nothing
  }

  inner class SelectionActionMode : ActionMode.Callback {

    override fun onCreateActionMode(
      actionMode: ActionMode,
      menu: Menu,
    ): Boolean {
      return true
    }

    override fun onPrepareActionMode(
      actionMode: ActionMode,
      menu: Menu,
    ): Boolean {
      return true
    }

    override fun onActionItemClicked(
      actionMode: ActionMode,
      item: MenuItem,
    ): Boolean {
      return true
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {}
  }
}
