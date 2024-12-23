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

package com.teixeira0x.subtypo.ui.common.fragment

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.teixeira0x.subtypo.ui.common.Constants
import com.teixeira0x.subtypo.ui.common.databinding.LayoutProgressDialogBinding

class ProgressDialogFragment : DialogFragment() {

  companion object {

    @JvmStatic
    fun newInstance(message: String? = null): ProgressDialogFragment {
      return ProgressDialogFragment().apply {
        if (message != null) {
          arguments =
            Bundle().apply { putString(Constants.KEY_MESSAGE_ARG, message) }
        }
      }
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val binding = LayoutProgressDialogBinding.inflate(layoutInflater)
    val message = arguments?.getString(Constants.KEY_MESSAGE_ARG)
    if (message != null) {
      binding.message.text = message
    }

    return AlertDialog.Builder(requireContext())
      .setView(binding.root)
      .create()
      .apply { window?.setBackgroundDrawable(GradientDrawable()) }
  }
}
