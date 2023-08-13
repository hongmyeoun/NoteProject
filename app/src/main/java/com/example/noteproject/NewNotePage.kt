package com.example.noteproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.noteproject.data.Note
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewNotePage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {
                val context = LocalContext.current
                val db = remember { NoteAppDatabase.getDatabase(context) }
                val scope = rememberCoroutineScope()

                var noteTitle by remember { mutableStateOf("제목") }
                var noteText by remember { mutableStateOf("") }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(text = "◁", color = Color.Black)
                        }
                        TextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
                        )
                        Button(
                            onClick = {
                                val newNote = Note(title = noteTitle, script = noteText)
                                scope.launch(Dispatchers.IO) { db.noteDao().insertAll(newNote) }
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
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
