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

package com.teixeira0x.subtypo.ui.common.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.IOException

object VideoUtils {
  fun Context.getVideoThumbnail(path: String): Bitmap? {
    return getVideoThumbnail(Uri.fromFile(File(path)))
  }

  fun Context.getVideoThumbnail(uri: Uri): Bitmap? {
    var thumbnail: Bitmap? = null
    val retriever = MediaMetadataRetriever()

    try {
      retriever.setDataSource(this, uri)
      thumbnail = retriever.getFrameAtTime()
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        retriever.release()
      } catch (ioe: IOException) {
        // Nothing
      }
    }

    return thumbnail
  }
}
