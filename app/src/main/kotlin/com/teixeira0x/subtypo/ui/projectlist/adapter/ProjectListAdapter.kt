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

package com.teixeira0x.subtypo.ui.projectlist.adapter

import android.view.ViewGroup
import androidx.recyclerview.selection.Selection
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.teixeira0x.subtypo.core.domain.model.Project
import com.teixeira0x.subtypo.ui.common.databinding.LayoutProjectItemBinding
import com.teixeira0x.subtypo.ui.common.utils.layoutInflater

class ProjectListAdapter(
  private val selectionTracker: SelectionTracker<Long>,
  private val listener: ProjectClickListener,
) : ListAdapter<Project, ProjectViewHolder>(ProjectDiffCallback()) {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ): ProjectViewHolder {
    return ProjectViewHolder(
      listener,
      LayoutProjectItemBinding.inflate(
        parent.context.layoutInflater,
        parent,
        false,
      ),
    )
  }

  override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
    val project = getItem(position)
    val isSelected = selectionTracker.isSelected(project.id)
    holder.bind(project, isSelected)
  }

  fun selection(keys: Selection<Long>): List<Project> {
    return currentList.filter { keys.contains(it.id) }
  }

  class ProjectDiffCallback : DiffUtil.ItemCallback<Project>() {
    override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
      oldItem: Project,
      newItem: Project,
    ): Boolean {
      return oldItem == newItem
    }
  }
}
