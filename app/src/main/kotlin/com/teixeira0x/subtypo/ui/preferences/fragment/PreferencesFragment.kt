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

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.teixeira0x.subtypo.App
import com.teixeira0x.subtypo.BuildConfig
import com.teixeira0x.subtypo.core.preferences.global.AboutPreferences.Keys as AboutKeys
import com.teixeira0x.subtypo.core.preferences.global.GeneralPreferences.Keys as GeneralKeys
import com.teixeira0x.subtypo.ui.activity.Navigator.navigateToLibsActivity
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.interfaces.Selectable
import com.teixeira0x.subtypo.ui.common.utils.openUrl
import com.teixeira0x.subtypo.ui.preferences.viewmodel.PreferencesViewModel

class PreferencesFragment : PreferenceFragmentCompat(), Selectable {

  private val viewModel by viewModels<PreferencesViewModel>()

  private val onBackPressedCallback =
    object : OnBackPressedCallback(false) {
      override fun handleOnBackPressed() = viewModel.navigateBack()
    }

  private val versionSummary: String
    get() =
      "${BuildConfig.VERSION_NAME} (${BuildConfig.BUILD_TYPE.uppercase()})"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requireActivity()
      .onBackPressedDispatcher
      .addCallback(this, onBackPressedCallback)
  }

  override fun onDestroy() {
    super.onDestroy()
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
        findPreference<Preference>(GeneralKeys.KEY)
          ?.setOnPreferenceClickListener { _ ->
            viewModel.navigateToScreen(R.xml.preferences_general)

            true
          }

        findPreference<Preference>(AboutKeys.ABOUT_GITHUB_KEY)
          ?.setOnPreferenceClickListener { _ ->
            requireContext().openUrl(App.APP_REPO_URL)
            true
          }

        findPreference<Preference>(AboutKeys.ABOUT_LIBRARIES_KEY)
          ?.setOnPreferenceClickListener { _ ->
            navigateToLibsActivity(requireContext())
            true
          }

        findPreference<Preference>(AboutKeys.ABOUT_VERSION_KEY)
          ?.setSummary(versionSummary)
      }
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
