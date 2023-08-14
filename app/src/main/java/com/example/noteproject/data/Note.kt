package com.example.noteproject.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) var uid: Int = 0,
    @ColumnInfo(name = "title") var title: String?,
    @ColumnInfo(name = "script") var script: String?,
    @ColumnInfo(name = "created_date") var createdDate: String?
)
