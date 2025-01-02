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

package com.teixeira0x.subtypo.ui.optionlist.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teixeira0x.subtypo.ui.common.databinding.LayoutOptionItemBinding
import com.teixeira0x.subtypo.ui.common.utils.layoutInflater
import com.teixeira0x.subtypo.ui.optionlist.model.OptionItem

class OptionListAdapter(
  private val options: List<OptionItem>,
  private val optionClickListener: (Int, OptionItem) -> Unit,
) : RecyclerView.Adapter<OptionViewHolder>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int,
  ): OptionViewHolder {
    return OptionViewHolder(
      LayoutOptionItemBinding.inflate(
        parent.context.layoutInflater,
        parent,
        false,
      ),
      optionClickListener,
    )
  }

  override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
    holder.bind(position, options[position])
  }

  override fun getItemCount() = options.size
}
