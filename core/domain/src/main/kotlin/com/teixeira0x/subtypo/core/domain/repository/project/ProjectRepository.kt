package com.teixeira0x.subtypo.core.domain.repository.project

import com.teixeira0x.subtypo.core.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {

  fun getAll(): Flow<List<Project>>

  fun getProject(id: Long): Flow<Project?>

  suspend fun insertProject(project: Project): Long

  suspend fun updateProject(project: Project): Int

  suspend fun removeProject(id: Long): Int
}
