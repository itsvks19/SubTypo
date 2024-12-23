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

package com.teixeira0x.subtypo.ui.preferences.components

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.teixeira0x.subtypo.App
import com.teixeira0x.subtypo.ui.common.R

class UIModePreference : Preference {

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int,
  ) : super(context, attrs, defStyleAttr)

  constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int,
    defStyleRes: Int,
  ) : super(context, attrs, defStyleAttr, defStyleRes)

  override fun onClick() {
    super.onClick()

    val items =
      arrayOf(
        getString(R.string.pref_appearance_ui_mode_value_follow),
        getString(R.string.pref_appearance_ui_mode_value_light),
        getString(R.string.pref_appearance_ui_mode_value_dark),
      )

    val prefs = sharedPreferences ?: return
    val editor = prefs.edit()
    val selectedItemIndex = prefs.getInt(key, 0)

    MaterialAlertDialogBuilder(context)
      .setTitle(title)
      .setSingleChoiceItems(items, selectedItemIndex) { _, w ->
        editor.putInt(key, w)
      }
      .setNegativeButton(R.string.cancel, null)
      .setPositiveButton(R.string.save) { _, _ ->
        editor.apply()
        App.instance.updateUIMode()
      }
      .show()
  }

  private fun getString(resId: Int): String {
    return context.getString(resId)
  }
}
