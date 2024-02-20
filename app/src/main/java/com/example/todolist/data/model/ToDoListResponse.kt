package com.example.todolist.data.model

data class ToDoListResponse(
    val todos: List<Todo>
){
    data class Todo(
        val Category: String,
        val Title: String,
        val completed: Boolean,
        val date: String,
        val id: Int,
        val priority: String,
        val todo: String,
        val userId: Int
    )
}