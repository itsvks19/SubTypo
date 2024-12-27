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

package com.teixeira0x.subtypo.ui.activity.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teixeira0x.subtypo.ui.activity.main.model.NavigationItem
import com.teixeira0x.subtypo.ui.common.R
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

  private val _navigationItems =
    listOf(
      NavigationItem(
        navigationItemId = R.id.item_projects,
        fragmentIndex = 0,
        title = R.string.projects,
      ),
      NavigationItem(
        navigationItemId = R.id.item_settings,
        fragmentIndex = 1,
        title = R.string.settings,
      ),
    )

  private val _currentSelectedItem =
    MutableStateFlow<NavigationItem?>(_navigationItems.first())

  val currentSelectedItem: StateFlow<NavigationItem?> =
    _currentSelectedItem.asStateFlow()

  fun navigateTo(id: Int) {
    viewModelScope.launch {
      val previousSelectedItem = _currentSelectedItem.value
      val selectItem = _navigationItems.find { it.navigationItemId == id }

      if (selectItem != null && selectItem != previousSelectedItem) {
        _currentSelectedItem.value = selectItem
      }
    }
  }
}
