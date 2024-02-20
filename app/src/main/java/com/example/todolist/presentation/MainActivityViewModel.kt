package com.example.todolist.presentation

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.CategoryInfo
import com.example.todolist.data.model.NoOfTaskForEachCategory
import com.example.todolist.data.model.TaskCategoryInfo
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.domain.TaskCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val repository: TaskCategoryRepository,private val pref:SharedPreferences) :
    ViewModel() {

    fun updateTaskStatus(task: TaskInfo) {
        viewModelScope.launch(IO) {
            repository.updateTaskStatus(task)
        }
    }

    fun deleteTask(task: TaskInfo) {
        viewModelScope.launch(IO) {
            repository.deleteTask(task)
        }
    }

    fun insertTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        viewModelScope.launch(IO) {
            repository.insertTaskAndCategory(taskInfo, categoryInfo)
        }
    }

    fun updateTaskAndAddCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        viewModelScope.launch(IO) {
            repository.updateTaskAndAddCategory(taskInfo, categoryInfo)
        }
    }

    fun updateTaskAndAddDeleteCategory(
        taskInfo: TaskInfo,
        categoryInfoAdd: CategoryInfo,
        categoryInfoDelete: CategoryInfo
    ) {
        viewModelScope.launch(IO) {
            repository.updateTaskAndAddDeleteCategory(taskInfo, categoryInfoAdd, categoryInfoDelete)
        }
    }

    fun deleteTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        viewModelScope.launch(IO) {
            repository.deleteTaskAndCategory(taskInfo, categoryInfo)
        }
    }

    fun getUncompletedTask(): LiveData<List<TaskCategoryInfo>> {
        return repository.getUncompletedTask()
    }

    fun getCompletedTask(): LiveData<List<TaskCategoryInfo>> {
        return repository.getCompletedTask()
    }

    fun getAllTasks(): LiveData<List<TaskCategoryInfo>> {
        return repository.getAllTask()
    }

    fun getSortedTaskByPriority(): LiveData<List<TaskCategoryInfo>> {
        return repository.getSortedTaskByPriority()
    }
    fun getSortedTaskByDate(): LiveData<List<TaskCategoryInfo>> {
        return repository.getSortedTaskByDate()
    }
    fun getSortedTaskByCompleted(): LiveData<List<TaskCategoryInfo>> {
        return repository.getSortedTaskByCompleted()
    }

    fun getAllTaskFromNetwork() {
       if(! pref.getBoolean("networkData",false))
       {
           pref.edit().putBoolean("networkData",true).apply()
           viewModelScope.launch {
               repository.getAllTaskFromNetwork()
           }
       }

    }

    fun getUncompletedTaskOfCategory(category: String): LiveData<List<TaskCategoryInfo>> {
        return repository.getUncompletedTaskOfCategory(category)
    }

    fun getCompletedTaskOfCategory(category: String): LiveData<List<TaskCategoryInfo>> {
        return repository.getCompletedTaskOfCategory(category)
    }

    fun getAllTaskOfCategory(category: String): LiveData<List<TaskCategoryInfo>> {
        return repository.getAllTaskOfCategory(category)
    }

    fun searchTask(searchText: String) : LiveData<List<TaskCategoryInfo>>{
        return repository.searchTask(searchText)
    }

    fun getNoOfTaskForEachCategory(): LiveData<List<NoOfTaskForEachCategory>> {
        return repository.getNoOfTaskForEachCategory()
    }

    fun getCategories(): LiveData<List<CategoryInfo>> {
        return repository.getCategories()
    }

    suspend fun getCountOfCategory(category: String): Int {
        var count: Int
        coroutineScope() {
            count = withContext(IO) { repository.getCountOfCategory(category) }
        }
        return count
    }

    fun getAlarms(currentTime: Date) {
        CoroutineScope(Dispatchers.Main).launch {
            val list = repository.getActiveAlarms(currentTime)
            Log.d("DATA", list.toString())
        }
    }
}