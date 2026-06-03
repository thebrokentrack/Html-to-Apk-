package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): Project?

    @Query("SELECT * FROM project_files WHERE projectId = :projectId")
    fun getFilesForProject(projectId: Int): Flow<List<ProjectFile>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId")
    suspend fun getFilesForProjectSync(projectId: Int): List<ProjectFile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFile): Long

    @Update
    suspend fun updateFile(file: ProjectFile)

    @Delete
    suspend fun deleteFile(file: ProjectFile)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteFilesByProject(projectId: Int)
}
