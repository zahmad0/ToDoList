package com.example.todolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "taskInfo")
data class TaskInfo(
    @PrimaryKey(autoGenerate = false)
    var id : Int,
    var title : String,
    var todo : String,
    var date : Date,
    var priority : Int,
    var completed : Boolean,
    var category: String
) : Serializable

