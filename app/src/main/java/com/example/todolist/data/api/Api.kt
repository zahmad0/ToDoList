package com.example.todolist.data.api

import com.example.todolist.data.model.ToDoListResponse
import retrofit2.Response
import retrofit2.http.GET

interface Api {


    @GET("970ec59d-1762-492b-90c0-2e60fa2d1bb4")
    suspend fun getToDoList(): Response<ToDoListResponse>



}