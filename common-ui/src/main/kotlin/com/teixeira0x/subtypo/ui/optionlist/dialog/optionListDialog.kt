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

package com.teixeira0x.subtypo.ui.optionlist.dialog

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.teixeira0x.subtypo.ui.common.databinding.LayoutOptionListModalBinding
import com.teixeira0x.subtypo.ui.optionlist.adapter.OptionListAdapter
import com.teixeira0x.subtypo.ui.optionlist.model.OptionItem

inline fun showOptionListDialog(
  context: Context,
  options: List<OptionItem>,
  crossinline configureDialog: BottomSheetDialog.() -> Unit = {},
  crossinline optionClickListener: (Int, OptionItem) -> Unit,
) {
  BottomSheetDialog(context).apply {
    configureDialog()

    setContentView(
      LayoutOptionListModalBinding.inflate(layoutInflater)
        .apply {
          rvOptions.layoutManager = LinearLayoutManager(context)
          rvOptions.adapter =
            OptionListAdapter(options) { position, option ->
              dismiss()
              optionClickListener(position, option)
            }
        }
        .root
    )
    show()
  }
}
