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

package com.teixeira0x.subtypo.ui.videopicker.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teixeira0x.subtypo.core.domain.model.Video
import com.teixeira0x.subtypo.ui.common.databinding.LayoutVideoPreviewItemBinding

class VideoPreviewViewHolder(
  private val binding: LayoutVideoPreviewItemBinding,
  private val onVideoPreviewClick: (Video) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

  fun bind(video: Video) {
    Glide.with(binding.root.context).load(video.path).into(binding.imgThumbnail)
    binding.txtTitle.isSelected = true
    binding.txtTitle.text = video.title
    binding.root.setOnClickListener { onVideoPreviewClick(video) }
  }
}
