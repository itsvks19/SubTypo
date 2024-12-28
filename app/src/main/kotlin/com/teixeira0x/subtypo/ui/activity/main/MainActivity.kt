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

package com.teixeira0x.subtypo.ui.activity.main

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.R.attr
import com.google.android.material.color.MaterialColors
import com.teixeira0x.subtypo.ui.activity.main.model.NavigationItem
import com.teixeira0x.subtypo.ui.activity.main.viewmodel.MainViewModel
import com.teixeira0x.subtypo.ui.common.R
import com.teixeira0x.subtypo.ui.common.activity.BaseEdgeToEdgeActivity
import com.teixeira0x.subtypo.ui.common.databinding.ActivityMainBinding
import com.teixeira0x.subtypo.ui.common.interfaces.Selectable
import com.teixeira0x.subtypo.ui.preferences.fragment.PreferencesFragment
import com.teixeira0x.subtypo.ui.projectlist.fragment.ProjectListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : BaseEdgeToEdgeActivity() {

  private var _binding: ActivityMainBinding? = null
  private val binding: ActivityMainBinding
    get() = checkNotNull(_binding) { "MainActivity has been destroyed!" }

  private val viewModel by viewModels<MainViewModel>()

  private val onBackPressedCallback =
    object : OnBackPressedCallback(false) {
      override fun handleOnBackPressed() =
        viewModel.navigateTo(R.id.item_projects)
    }

  override val statusBarColor: Int
    get() = MaterialColors.getColor(this, attr.colorOnSurfaceInverse, 0)

  override val navigationBarColor: Int
    get() = MaterialColors.getColor(this, attr.colorOnSurfaceInverse, 0)

  override val navigationBarDividerColor: Int
    get() = MaterialColors.getColor(this, attr.colorOnSurfaceInverse, 0)

  override fun bindView(): View {
    return ActivityMainBinding.inflate(layoutInflater)
      .also { _binding = it }
      .root
  }

  override fun onApplySystemBarInsets(insets: Insets) {
    _binding?.apply {
      appBar.updatePadding(top = insets.top)
      toolbar.updatePaddingRelative(start = insets.left, end = insets.right)

      fragmentsContainer.updatePadding(left = insets.left, right = insets.right)
      bottomNavigation.updatePadding(
        left = insets.left,
        right = insets.right,
        bottom = insets.bottom,
      )
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)
    observeViewModel()

    onBackPressedDispatcher.addCallback(onBackPressedCallback)
    configureListeners()
  }

  override fun onDestroy() {
    super.onDestroy()
    onBackPressedCallback.isEnabled = false
    _binding = null
  }

  private fun configureListeners() {
    binding.toolbar.setNavigationOnClickListener {
      onBackPressedDispatcher.onBackPressed()
    }

    binding.bottomNavigation.setOnItemSelectedListener { item ->
      viewModel.navigateTo(item.itemId)

      true
    }
  }

  private fun observeViewModel() {
    viewModel.currentSelectedItem
      .flowWithLifecycle(lifecycle)
      .onEach(this::onSelectedNavigationItemChange)
      .launchIn(lifecycleScope)
  }

  private fun onSelectedNavigationItemChange(item: NavigationItem?) {
    if (item == null) {
      return
    }
    supportActionBar?.setTitle(item.title)

    setNavigationSelectedItem(item.navigationItemId)
    updateSelectedFragment(item.fragmentIndex)
  }

  private fun updateSelectedFragment(index: Int) {
    binding.fragmentsContainer.displayedChild = index

    val fragments =
      arrayOf(
        binding.fragmentProjectList.getFragment<ProjectListFragment>(),
        binding.fragmentPreferences.getFragment<PreferencesFragment>(),
      )

    fragments.forEachIndexed { idx, fragment ->
      (fragment as? Selectable)?.let { selectableFragment ->
        if (idx == index) {
          selectableFragment.onSelect()
        } else {
          selectableFragment.onUnselect()
        }
      }
    }

    setBackEnabled(index > 0)
  }

  private fun setBackEnabled(enabled: Boolean) {
    onBackPressedCallback.isEnabled = enabled
    supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    supportActionBar?.setHomeButtonEnabled(enabled)
  }

  private fun setNavigationSelectedItem(id: Int) {
    if (binding.bottomNavigation.selectedItemId != id) {
      binding.bottomNavigation.setSelectedItemId(id)
    }
  }
}
