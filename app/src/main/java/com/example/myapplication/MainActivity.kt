package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestFirebaseScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun TestFirebaseScreen(modifier: Modifier = Modifier) {
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Ready to test") }
    var userId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Firebase Test")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = status)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            scope.launch {
                status = "Adding Jan Kowalski..."
                val user = User(
                    first_name = "Jan",
                    last_name = "Kowalski"
                )
                repository.addUser(user)
                    .onSuccess { id ->
                        userId = id
                        status = "User added! ID: $id"
                    }
                    .onFailure { e ->
                        status = "Error adding: ${e.message}"
                    }
            }
        }) {
            Text("Add Jan Kowalski")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    userId?.let { id ->
                        status = "Checking if user exists..."
                        repository.getUser(id)
                            .onSuccess { user ->
                                if (user != null) {
                                    status = "✓ User found: ${user.first_name} ${user.last_name}"
                                } else {
                                    status = "✗ User not found"
                                }
                            }
                            .onFailure { e ->
                                status = "Error checking: ${e.message}"
                            }
                    } ?: run {
                        status = "Add user first!"
                    }
                }
            },
            enabled = userId != null
        ) {
            Text("Check if Added")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                status = "Getting all users..."
                repository.getAllUsers()
                    .onSuccess { users ->
                        status = "Found ${users.size} users in database"
                    }
                    .onFailure { e ->
                        status = "Error: ${e.message}"
                    }
            }
        }) {
            Text("Get All Users")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Skibd $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        TestFirebaseScreen()
    }
}