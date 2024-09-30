package com.josuerdx.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.josuerdx.lab08.components.MyTabBarScreen
import com.josuerdx.lab08.components.MyToolbar
import com.josuerdx.lab08.data.database.TaskDatabase
import com.josuerdx.lab08.data.model.Task
import com.josuerdx.lab08.ui.theme.Lab08Theme
import com.josuerdx.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                ).build()

                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)

                // Toolbar - TabBar
                Scaffold(
                    topBar = { MyToolbar() },
                    content = { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues)) {
                            MyTabBarScreen {
                                TaskScreen(viewModel)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var newTaskDescription by remember { mutableStateOf("") }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var editedDescription by remember { mutableStateOf("") }

    // Estructura de pantalla
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Campo de texto
            TextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                label = { Text("Nueva tarea") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Comprobar si hay tareas
            if (tasks.isEmpty()) {
                EmptyStateView()
            } else {
                // Lista de tareas
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically // Alinear verticalmente
                    ) {
                        // Mostrar la descripción de la tarea
                        Text(
                            text = task.description,
                            color = if (task.isCompleted) Color.Gray else Color.Black,
                            style = if (task.isCompleted) MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough)
                            else MaterialTheme.typography.bodyMedium
                        )

                        // Botón para editar tarea
                        IconButton(onClick = {
                            editingTask = task
                            editedDescription = task.description
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar tarea",
                            )
                        }

                        // Botón para eliminar tarea
                        IconButton(onClick = { viewModel.deleteTask(task) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar tarea")
                        }

                        // Checkbox para completar tarea
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = {
                                viewModel.toggleTaskCompletion(task)
                            }
                        )
                    }
                }

                // Diálogo de edición
                if (editingTask != null) {
                    EditTaskDialog(
                        task = editingTask!!,
                        onEdit = { newDescription ->
                            coroutineScope.launch {
                                viewModel.editTask(editingTask!!, newDescription)
                                editingTask = null
                            }
                        },
                        onDismiss = { editingTask = null }
                    )
                }
            }
        }

        // FloatingActionButton para eliminar todas las tareas
        FloatingActionButton(
            onClick = {
                coroutineScope.launch { viewModel.deleteAllTasks() }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Alinear en la parte inferior derecha

                .padding(bottom = 90.dp, end = 16.dp)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Eliminar todas las tareas")
        }

        // FloatingActionButton para agregar una nueva tarea
        FloatingActionButton(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    viewModel.addTask(newTaskDescription)
                    newTaskDescription = ""
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar tarea")
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Imagen si no hay tareas
        Image(
            painter = painterResource(id = R.drawable.tasksss),
            contentDescription = "No hay tareas",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.FillBounds
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Texto si no hay tareas
        Text(text = "No hay tareas hoy", style = MaterialTheme.typography.headlineSmall)

        Text(
            text = "Sal a caminar",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 15.dp)
        )
    }
}

@Composable
fun EditTaskDialog(task: Task, onEdit: (String) -> Unit, onDismiss: () -> Unit) {
    var taskDescription by remember { mutableStateOf(task.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Editar Tarea") },
        text = {
            TextField(
                value = taskDescription,
                onValueChange = { taskDescription = it },
                label = { Text("Descripción de la tarea") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskDescription.isNotBlank()) {
                        onEdit(taskDescription)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}