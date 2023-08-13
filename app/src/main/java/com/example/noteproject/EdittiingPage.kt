package com.example.noteproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.noteproject.ui.theme.NoteProjectTheme

class EdittiingPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {
                var noteTitle by remember { mutableStateOf("제목") }
                var noteText by remember { mutableStateOf("") }
                NoteProjectTheme {
                    Column {
                        Row {
                            Button(onClick = { /*TODO*/ }) {
                                Text(text = "<")
                            }
                            TextField(
                                value = noteTitle,
                                onValueChange = { noteTitle = it },
                                colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
                            )
                            Button(onClick = { /*TODO*/ }) {
                                Text(text = "💾")
                            }
                        }
                        TextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            modifier = Modifier.fillMaxSize(),
                            colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Preview2() {
    var noteTitle by remember { mutableStateOf("제목") }
    var noteText by remember { mutableStateOf("") }
    NoteProjectTheme {
        Column {
            Row {
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "<")
                }
                TextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
                )
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "💾")
                }
            }
            TextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxSize(),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
            )
        }
    }
}
