package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_files")
data class ProjectFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val name: String,
    val content: String,
    val fileType: String // e.g. "html", "css", "js", "python", "json", "jsx"
)
