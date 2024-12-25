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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.teixeira0x.subtypo.core.domain.model.Project
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.adapter.holder.BindingViewHolder
import com.teixeira0x.subtypo.ui.common.databinding.LayoutProjectItemBinding
import com.teixeira0x.subtypo.ui.common.utils.VideoUtils.getVideoThumbnail
import com.teixeira0x.subtypo.ui.common.utils.layoutInflater

class ProjectListAdapter(
  private val projectClickListener: ProjectClickListener
) :
  ListAdapter<Project, ProjectListAdapter.ProjectViewHolder>(
    ProjectDiffCallback()
  ) {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ): ProjectViewHolder {
    return ProjectViewHolder(
      projectClickListener = projectClickListener,
      binding =
        LayoutProjectItemBinding.inflate(
          parent.context.layoutInflater,
          parent,
          false,
        ),
    )
  }

  override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  class ProjectViewHolder(
    private val projectClickListener: ProjectClickListener,
    binding: LayoutProjectItemBinding,
  ) : BindingViewHolder<LayoutProjectItemBinding>(binding) {

    fun bind(project: Project) {
      binding.apply {
        with(project) {
          tvName.text = name

          if (videoUri.isNotEmpty()) {
            imgVideoThumbnail.isVisible = true
            imgVideoThumbnail.setImageBitmap(
              root.context.getVideoThumbnail(videoUri)
            )
            tvVideoName.text = videoName
          } else {
            imgVideoThumbnail.isVisible = false
            tvVideoName.setText(R.string.proj_no_video)
          }
        }

        root.setOnClickListener {
          projectClickListener.onProjectClickListener(it, project)
        }
        root.setOnLongClickListener {
          projectClickListener.onProjectLongClickListener(it, project)
        }
        imgMenu.setOnClickListener {
          projectClickListener.onProjectMenuClickListener(it, project)
        }
      }
    }
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
