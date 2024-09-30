package com.josuerdx.lab08.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.josuerdx.lab08.data.model.Task

@Dao
interface TaskDao {


    // Obtener todas las tareas
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>


    // Insertar una nueva tarea
    @Insert
    suspend fun insertTask(task: Task)


    // Marcar una tarea como completada o no completada
    @Update
    suspend fun updateTask(task: Task)


    // Eliminar todas las tareas
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // Eliminar una tarea individua
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)
}