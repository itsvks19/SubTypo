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

package com.teixeira0x.subtypo.core.subtitle.converter

import com.teixeira0x.subtypo.core.subtitle.format.SRTFormat
import com.teixeira0x.subtypo.core.subtitle.format.SubtitleFormat

object FormatConverter {
  private val subtitleFormats = mapOf(1 to SRTFormat)

  fun fromId(id: Int): SubtitleFormat {
    return subtitleFormats[id]
      ?: throw IllegalArgumentException("Invalid format id: $id")
  }

  fun fromFileExtension(extension: String): SubtitleFormat {
    return subtitleFormats.values.find { it.extension == extension }
      ?: throw IllegalArgumentException("Unknown format extension: $extension")
  }

  fun toId(format: SubtitleFormat): Int {
    return subtitleFormats.entries.find { it.value == format }?.key
      ?: throw IllegalArgumentException("Unknown format: ${format.extension}")
  }
}
