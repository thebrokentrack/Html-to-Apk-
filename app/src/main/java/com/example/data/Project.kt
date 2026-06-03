package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val defaultFile: String = "index.html",
    val permNotification: Boolean = true,
    val permMicrophone: Boolean = false,
    val permCamera: Boolean = false,
    val permAutoplay: Boolean = true,
    val permRefresh: Boolean = true,
    val hideTitleBar: Boolean = true,
    val appIconIndex: Int = 0,
    val splashIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
