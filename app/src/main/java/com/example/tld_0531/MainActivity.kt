package com.example.tld_0531

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.tld_0531.ui.theme.TLD0531Theme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import androidx.compose.ui.Alignment
import io.github.jan.supabase.postgrest.postgrest

val supabaseUrl = ""
val supabaseKey = ""
val supabaseClient = createSupabaseClient(supabaseUrl, supabaseKey) {
    install(Postgrest)
}


@Serializable
data class ToDoItem(val id: Int? = null, val text: String)

suspend fun fetchToDoItems(): List<ToDoItem> {
    return withContext(Dispatchers.IO) {
        try {
            val response = supabaseClient.postgrest.from("todos").select().decodeList<ToDoItem>()
            response
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun addToDoItem(text: String): ToDoItem? {
    return withContext(Dispatchers.IO) {
        try {
            val newToDo = ToDoItem(text = text)
            val startTime = System.currentTimeMillis()
            val response = supabaseClient.postgrest.from("todos").insert(newToDo) {
                select()
            }.decodeSingleOrNull<ToDoItem>()
            val endTime = System.currentTimeMillis()
            Log.d("Supabase", "Data insertion took ${endTime - startTime} ms")
            response ?: newToDo
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TLD0531Theme {
                ToDoApp()
            }
        }
    }
}


@Composable
fun ToDoApp() {
    var newToDoText by remember { mutableStateOf("") }
    var toDoList by remember { mutableStateOf(listOf<ToDoItem>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            toDoList = fetchToDoItems()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedTextField(
            value = newToDoText,
            onValueChange = { newToDoText = it },
            label = { Text("Enter a new to-do item") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val newToDoItem = addToDoItem(newToDoText)
                    if (newToDoItem != null) {
                        toDoList = toDoList + newToDoItem
                        newToDoText = ""
                        // 데이터를 추가한 후 다시 불러옵니다.
//                        toDoList = fetchToDoItems()

                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(toDoList) { item ->
                Text(text = item.text, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TLD0531Theme {
        ToDoApp()
    }
}