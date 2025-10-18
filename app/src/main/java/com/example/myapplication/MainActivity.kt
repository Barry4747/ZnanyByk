package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
                    AddUserForm(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AddUserForm(modifier: Modifier = Modifier) {
    val repository = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    val user = User(
                        first_name = firstName,
                        last_name = lastName
                    )
                    repository.addUser(user)
                        .onSuccess { id ->
                            statusMessage = "Added: $firstName $lastName (ID: $id)"
                            firstName = ""
                            lastName = ""
                        }
                        .onFailure { e ->
                            statusMessage = "Error: ${e.message}"
                        }
                }
            },
            enabled = firstName.isNotBlank() && lastName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add User")
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = statusMessage,
                color = if (statusMessage.startsWith("Added")) Color.Green else Color.Red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        AddUserForm()
    }
}