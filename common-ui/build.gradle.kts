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

import com.teixeira0x.subtypo.build.BuildConfig

plugins {
  id("com.android.library")
  id("kotlin-android")
}

android {
  namespace = "${BuildConfig.packageName}.ui.common"

  buildFeatures.viewBinding = true
}

dependencies {
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.preference)
  implementation(libs.androidx.exoplayer)
  implementation(libs.androidx.exoplayer.dash)
  implementation(libs.androidx.exoplayer.ui)

  // Google
  implementation(libs.google.material)
}
