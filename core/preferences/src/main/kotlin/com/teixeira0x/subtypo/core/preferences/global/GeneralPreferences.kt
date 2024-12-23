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

package com.teixeira0x.subtypo.core.preferences.global

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class GeneralPreferences(private val preferences: SharedPreferences) {
  companion object Keys {
    const val KEY = "pref_configure_general_key"
    const val APPEARANCE_UI_MODE_KEY = "pref_appearance_ui_mode_key"
    const val APPEARANCE_MATERIALYOU_KEY = "pref_appearance_materialyou_key"
  }

  val appearanceUIMode: Int
    get() =
      when (preferences.getInt(APPEARANCE_UI_MODE_KEY, 0)) {
        1 -> AppCompatDelegate.MODE_NIGHT_NO
        2 -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
      }

  val appearanceMaterialYou: Boolean
    get() = preferences.getBoolean(APPEARANCE_MATERIALYOU_KEY, true)
}
