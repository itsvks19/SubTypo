package com.teixeira0x.subtypo.ui.projectlist.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teixeira0x.subtypo.core.domain.model.Project
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.databinding.LayoutProjectItemBinding

class ProjectViewHolder(
  private val listener: ProjectClickListener,
  private val binding: LayoutProjectItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

  fun bind(project: Project, isSelected: Boolean) {
    binding.apply {
      configureListeners(project, isSelected)
      with(project) {
        tvName.text = name

        if (videoUri.isNotEmpty()) {
          Glide.with(root.context).load(videoUri).into(imgVideoThumbnail)

          imgVideoThumbnail.isVisible = true
          tvVideoName.text = videoName
        } else {
          imgVideoThumbnail.isVisible = false
          tvVideoName.setText(R.string.proj_no_video)
        }
      }
    }
  }

  private fun configureListeners(project: Project, isSelected: Boolean) {
    binding.root.apply {
      setOnClickListener { listener.onProjectClickListener(it, project) }
      setOnLongClickListener {
        listener.onProjectLongClickListener(it, project)
      }
    }
  }
}
