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

package com.teixeira0x.subtypo.core.preference

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * Class to manage preferences, as I will use this class a lot I decided to turn
 * it into an 'object' as it is easier than always injecting it.
 *
 * @author Felipe Teixeira
 */
object PreferencesManager {

  // General
  const val KEY_GENERAL = "pref_configure_general_key"
  const val KEY_APPEARANCE_UI_MODE = "pref_appearance_ui_mode_key"
  const val KEY_APPEARANCE_DYNAMICCOLORS = "pref_appearance_dynamiccolors_key"

  // About
  const val KEY_ABOUT_GITHUB = "pref_about_github_key"
  const val KEY_ABOUT_LIBRARIES = "pref_about_libraries_key"
  const val KEY_ABOUT_VERSION = "pref_about_version_key"

  private lateinit var preferences: SharedPreferences

  /* Initialize in the application class */
  fun init(context: Context) {
    this.preferences = PreferenceManager.getDefaultSharedPreferences(context)
  }

  fun registerOnSharedPreferenceChangeListener(
    listener: OnSharedPreferenceChangeListener
  ) {
    this.preferences.registerOnSharedPreferenceChangeListener(listener)
  }

  fun unregisterOnSharedPreferenceChangeListener(
    listener: OnSharedPreferenceChangeListener
  ) {
    this.preferences.registerOnSharedPreferenceChangeListener(listener)
  }

  val appearanceUIMode: Int
    get() =
      when (preferences.getInt(KEY_APPEARANCE_UI_MODE, 0)) {
        1 -> AppCompatDelegate.MODE_NIGHT_NO
        2 -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
      }

  var appearanceDynamicColors: Boolean
    get() = preferences.getBoolean(KEY_APPEARANCE_DYNAMICCOLORS, true)
    set(value) =
      preferences.edit { putBoolean(KEY_APPEARANCE_DYNAMICCOLORS, value) }
}
