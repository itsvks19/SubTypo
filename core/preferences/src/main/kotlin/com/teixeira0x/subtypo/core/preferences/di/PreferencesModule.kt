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

package com.teixeira0x.subtypo.core.preferences.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.teixeira0x.subtypo.core.preferences.global.AboutPreferences
import com.teixeira0x.subtypo.core.preferences.global.GeneralPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

  @Provides
  @Singleton
  fun providesDefaultSharedPreferences(
    @ApplicationContext applicationContext: Context
  ): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(applicationContext)
  }

  @Provides
  @Singleton
  fun providesGeneralPreferences(
    preferences: SharedPreferences
  ): GeneralPreferences {
    return GeneralPreferences(preferences)
  }

  @Provides
  @Singleton
  fun providesAboutPreferences(
    preferences: SharedPreferences
  ): AboutPreferences {
    return AboutPreferences(preferences)
  }
}
