package com.teixeira0x.subtypo.core.domain.repository.subtitle

import com.teixeira0x.subtypo.core.subtitle.model.Subtitle
import kotlinx.coroutines.flow.Flow

interface SubtitleRepository {

  fun getAll(projectId: Long): Flow<List<Subtitle>>

  fun getSubtitle(id: Long): Flow<Subtitle?>

  suspend fun insertSubtitle(subtitle: Subtitle): Long

  suspend fun updateSubtitle(subtitle: Subtitle)

  suspend fun removeSubtitle(id: Long): Int
}
