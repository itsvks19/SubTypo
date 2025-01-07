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

package com.teixeira0x.subtypo.core.data.repository

import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import com.teixeira0x.subtypo.core.domain.model.Album
import com.teixeira0x.subtypo.core.domain.model.Video
import com.teixeira0x.subtypo.core.domain.repository.video.VideoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.slf4j.LoggerFactory

class VideoRepositoryImpl
@Inject
constructor(@ApplicationContext private val appContext: Context) :
  VideoRepository {

  companion object {
    private val log = LoggerFactory.getLogger(VideoRepositoryImpl::class.java)
  }

  override fun getVideos(albumId: String?) =
    flow<List<Video>> {
        val videoList = mutableListOf<Video>()
        val projection =
          arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID,
          )

        val selection =
          if (!albumId.isNullOrEmpty())
            "${MediaStore.Video.Media.BUCKET_ID} = ?"
          else null
        val selectionArgs =
          if (!albumId.isNullOrEmpty()) arrayOf(albumId) else null

        appContext.contentResolver
          .query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_ADDED} DESC",
          )
          ?.use { cursor ->
            val titleIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val idIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val albumIndex =
              cursor.getColumnIndexOrThrow(
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
              )
            val sizeIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val pathIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
              try {
                val path = cursor.getString(pathIndex)
                val file = File(path)
                if (file.exists()) {
                  videoList.add(
                    Video(
                      title = cursor.getString(titleIndex),
                      id = cursor.getString(idIndex),
                      duration = cursor.getLong(durationIndex),
                      albumName = cursor.getString(albumIndex),
                      size = cursor.getString(sizeIndex),
                      path = path,
                      videoUri = file.toUri(),
                    )
                  )
                }
              } catch (e: Exception) {
                log.error("Error processing video", e)
              }
            }
          }
        emit(videoList)
      }
      .flowOn(Dispatchers.IO)

  override fun getAlbums() =
    flow<List<Album>> {
        val albumList = mutableListOf<Album>()
        val albumSet = mutableSetOf<String>()

        val projection =
          arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
          )

        appContext.contentResolver
          .query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null,
          )
          ?.use { cursor ->
            val albumIdIndex =
              cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
            val albumNameIndex =
              cursor.getColumnIndexOrThrow(
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
              )

            while (cursor.moveToNext()) {
              val albumId = cursor.getString(albumIdIndex)
              if (albumSet.add(albumId)) {
                albumList.add(
                  Album(id = albumId, name = cursor.getString(albumNameIndex))
                )
              }
            }
          }
        emit(albumList)
      }
      .flowOn(Dispatchers.IO)
}
