package com.example.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: Int): Project? {
        return projectDao.getProjectById(id)
    }

    fun getFilesForProject(projectId: Int): Flow<List<ProjectFile>> {
        return projectDao.getFilesForProject(projectId)
    }

    suspend fun getFilesForProjectSync(projectId: Int): List<ProjectFile> {
        return projectDao.getFilesForProjectSync(projectId)
    }

    suspend fun insertProject(project: Project): Int {
        return projectDao.insertProject(project).toInt()
    }

    suspend fun updateProject(project: Project) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: Project) {
        projectDao.deleteFilesByProject(project.id)
        projectDao.deleteProject(project)
    }

    suspend fun insertFile(file: ProjectFile): Int {
        return projectDao.insertFile(file).toInt()
    }

    suspend fun updateFile(file: ProjectFile) {
        projectDao.updateFile(file)
    }

    suspend fun deleteFile(file: ProjectFile) {
        projectDao.deleteFile(file)
    }
}
