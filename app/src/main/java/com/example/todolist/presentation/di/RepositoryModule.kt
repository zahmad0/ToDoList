package com.example.todolist.presentation.di

import com.example.todolist.data.api.Api
import com.example.todolist.data.db.TaskCategoryDao
import com.example.todolist.domain.TaskCategoryRepository
import com.example.todolist.data.repository.TaskCategoryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTaskCategoryRepository(taskCategoryDao: TaskCategoryDao,api:Api) : TaskCategoryRepository {
        return TaskCategoryRepositoryImpl(taskCategoryDao,api)
    }
}