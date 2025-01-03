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

package com.teixeira0x.subtypo.ui.preferences.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.color.DynamicColors
import com.teixeira0x.subtypo.App
import com.teixeira0x.subtypo.BuildConfig
import com.teixeira0x.subtypo.core.preference.PreferencesManager
import com.teixeira0x.subtypo.ui.activity.Navigator.navigateToLibsActivity
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.interface.Selectable
import com.teixeira0x.subtypo.ui.common.util.openUrl
import com.teixeira0x.subtypo.ui.preferences.viewmodel.PreferencesViewModel

class PreferencesFragment :
  PreferenceFragmentCompat(), OnSharedPreferenceChangeListener, Selectable {

  private val viewModel by viewModels<PreferencesViewModel>()

  private val onBackPressedCallback =
    object : OnBackPressedCallback(false) {
      override fun handleOnBackPressed() = viewModel.navigateBack()
    }

  private val versionSummary: String
    get() = "${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE})"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PreferencesManager.registerOnSharedPreferenceChangeListener(this)
    requireActivity()
      .onBackPressedDispatcher
      .addCallback(this, onBackPressedCallback)
  }

  override fun onDestroy() {
    super.onDestroy()
    PreferencesManager.unregisterOnSharedPreferenceChangeListener(this)
    onBackPressedCallback.isEnabled = false
  }

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?,
  ) {
    observeViewModel()
  }

  private fun observeViewModel() {
    viewModel.currentScreenId.observe(this) { screenId ->
      onBackPressedCallback.isEnabled = screenId != R.xml.preferences
      setPreferencesFromResource(screenId, null)
      onScreenIdChange(screenId)
    }
  }

  private fun onScreenIdChange(screenId: Int) {
    when (screenId) {
      R.xml.preferences -> {
        findPreference<Preference>(PreferencesManager.KEY_GENERAL)
          ?.setOnPreferenceClickListener { _ ->
            viewModel.navigateToScreen(R.xml.preferences_general)

            true
          }

        findPreference<Preference>(PreferencesManager.KEY_ABOUT_GITHUB)
          ?.setOnPreferenceClickListener { _ ->
            requireContext().openUrl(App.APP_REPO_URL)
            true
          }

        findPreference<Preference>(PreferencesManager.KEY_ABOUT_LIBRARIES)
          ?.setOnPreferenceClickListener { _ ->
            navigateToLibsActivity(requireContext())
            true
          }

        findPreference<Preference>(PreferencesManager.KEY_ABOUT_VERSION)
          ?.setSummary(versionSummary)
      }
      R.xml.preferences_general -> {
        findPreference<Preference>(
            PreferencesManager.KEY_APPEARANCE_DYNAMICCOLORS
          )
          ?.apply {
            if (!DynamicColors.isDynamicColorAvailable()) {
              if (PreferencesManager.appearanceDynamicColors) {
                PreferencesManager.appearanceDynamicColors = false
              }

              isEnabled = false
            }
          }
      }
    }
  }

  override fun onSharedPreferenceChanged(
    prefs: SharedPreferences,
    key: String?,
  ) {
    when (key) {
      PreferencesManager.KEY_APPEARANCE_UI_MODE ->
        AppCompatDelegate.setDefaultNightMode(
          PreferencesManager.appearanceUIMode
        )
      PreferencesManager.KEY_APPEARANCE_DYNAMICCOLORS -> activity?.recreate()
    }
  }

  override fun onSelect() {
    onBackPressedCallback.isEnabled =
      viewModel.currentScreenId.value != R.xml.preferences
  }

  override fun onUnselect() {
    onBackPressedCallback.isEnabled = false
  }
}
