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

package com.teixeira0x.subtypo.ui.preference.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.teixeira0x.subtypo.ui.common.R

class PreferencesViewModel : ViewModel() {

  private val _screenHistory =
    MutableLiveData<List<Int>>(listOf(R.xml.preferences))
  val screenHistory: LiveData<List<Int>> = _screenHistory

  val currentScreenId: LiveData<Int> = _screenHistory.map { it.last() }

  fun navigateToScreen(id: Int) {
    _screenHistory.value = _screenHistory.value!!.plus(id)
  }

  fun navigateBack() {
    val newHistory = _screenHistory.value!!.dropLast(1)
    if (newHistory.isNotEmpty()) {
      _screenHistory.value = newHistory
    }
  }
}
