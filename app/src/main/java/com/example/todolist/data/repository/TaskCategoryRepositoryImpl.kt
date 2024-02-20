package com.example.todolist.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.todolist.data.api.Api
import com.example.todolist.data.db.TaskCategoryDao
import com.example.todolist.data.model.CategoryInfo
import com.example.todolist.data.model.NoOfTaskForEachCategory
import com.example.todolist.data.model.TaskCategoryInfo
import com.example.todolist.data.model.TaskInfo
import com.example.todolist.data.util.DateToString
import com.example.todolist.domain.TaskCategoryRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

class TaskCategoryRepositoryImpl @Inject constructor(
    private val taskCategoryDao: TaskCategoryDao,
    private val api: Api
) :
    TaskCategoryRepository {

    override suspend fun updateTaskStatus(task: TaskInfo): Int {
        return taskCategoryDao.updateTaskStatus(task)
    }

    override suspend fun deleteTask(task: TaskInfo) {
        taskCategoryDao.deleteTask(task)
    }


    override suspend fun insertTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        taskCategoryDao.insertTaskAndCategory(taskInfo, categoryInfo)
    }

    override suspend fun insertTaskAndCategory(
        taskInfo: List<TaskInfo>,
        categoryInfo: List<CategoryInfo>
    ) {
        taskCategoryDao.insertTaskAndCategoryList(taskInfo, categoryInfo)
    }

    override suspend fun deleteTaskAndCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        taskCategoryDao.deleteTaskAndCategory(taskInfo, categoryInfo)
    }

    override suspend fun updateTaskAndAddDeleteCategory(
        taskInfo: TaskInfo,
        categoryInfoAdd: CategoryInfo,
        categoryInfoDelete: CategoryInfo
    ) {
        taskCategoryDao.updateTaskAndAddDeleteCategory(
            taskInfo,
            categoryInfoAdd,
            categoryInfoDelete
        )
    }

    override suspend fun updateTaskAndAddCategory(taskInfo: TaskInfo, categoryInfo: CategoryInfo) {
        taskCategoryDao.updateTaskAndAddCategory(taskInfo, categoryInfo)
    }

    override fun getUncompletedTask(): LiveData<List<TaskCategoryInfo>> {
        return taskCategoryDao.getUncompletedTask()
    }

    override fun getSortedTaskByPriority(): LiveData<List<TaskCategoryInfo>> {
        return taskCategoryDao.getSortedTaskByPriority()
    }

    override fun getSortedTaskByDate(): LiveData<List<TaskCategoryInfo>> {
        return taskCategoryDao.getSortedTaskByDate()
    }

    override fun getSortedTaskByCompleted(): LiveData<List<TaskCategoryInfo>> {
        return taskCategoryDao.getSortedTaskByCompleted()
    }

    override fun getCompletedTask(): LiveData<List<TaskCategoryInfo>> =
        taskCategoryDao.getCompletedTask()

    override fun getAllTask(): LiveData<List<TaskCategoryInfo>> {
        return taskCategoryDao.getAllTask()
    }

    override suspend fun getAllTaskFromNetwork():Boolean {
        val res = api.getToDoList()
        if (res.isSuccessful) {
            Log.w("TAG", "getAllTaskFromNetwork: ${res.body()?.todos}", )
            val taskList = (res.body()?.todos?.map {
                TaskInfo(
                    id = it.id,
                    title = it.Title,
                    todo = it.todo,
                    date = DateToString.convertStringToDate(it.date),
                    priority = when (it.priority) {
                        "Low" -> 0
                        "Medium" -> 1
                        else -> 2
                    },
                    completed = it.completed,
                    category = it.Category
                )
            })
            val catList = taskList?.distinctBy { it.category }?.map {
                CategoryInfo(
                    categoryInformation = it.category,
                    color = String.format("#%06x", Random(0xffffff + 1).nextInt())
                )
            }
            if (catList != null) {
                 insertTaskAndCategory(taskList, catList)
            }
            return true
        }
        return false
    }


    override fun getUncompletedTaskOfCategory(category: String): LiveData<List<TaskCategoryInfo>> =
        taskCategoryDao.getUncompletedTaskOfCategory(category)

    override fun getCompletedTaskOfCategory(category: String): LiveData<List<TaskCategoryInfo>> =
        taskCategoryDao.getCompletedTaskOfCategory(category)

    override fun getAllTaskOfCategory(category: String): LiveData<List<TaskCategoryInfo>> =
        taskCategoryDao.getAllTaskOfCategory(category)

    override fun searchTask(searchText: String) : LiveData<List<TaskCategoryInfo>> =
        taskCategoryDao.searchTask(searchText)


    override fun getNoOfTaskForEachCategory(): LiveData<List<NoOfTaskForEachCategory>> =
        taskCategoryDao.getNoOfTaskForEachCategory()

    override fun getCategories(): LiveData<List<CategoryInfo>> = taskCategoryDao.getCategories()
    override suspend fun getCountOfCategory(category: String): Int =
        taskCategoryDao.getCountOfCategory(category)

    override suspend fun getActiveAlarms(currentTime: Date): List<TaskInfo> {
        var list: List<TaskInfo>
        coroutineScope {
            list = withContext(IO) { taskCategoryDao.getActiveAlarms(currentTime) }
        }
        return list
    }
}