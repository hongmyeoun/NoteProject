package com.example.noteproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.noteproject.data.Note
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewNotePage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {
                val context = LocalContext.current
                val db = remember { NoteAppDatabase.getDatabase(context) }
                val scope = rememberCoroutineScope()

                var noteTitle by remember { mutableStateOf("") }
                var noteText by remember { mutableStateOf("") }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(40.dp)
                                .clickable {
                                    val intent = Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "◁", color = Color.Black)
                        }
                        TextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            placeholder = { Text(text = "제목", fontStyle = FontStyle.Italic)},
                            colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(height = 30.dp, width = 40.dp)
                                .clickable {
                                    val currentDate = SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(Date())
                                    val newNote = Note(title = noteTitle, script = noteText, createdDate = currentDate)
                                    scope.launch(Dispatchers.IO) { db.noteDao().insertAll(newNote) }
                                    val intent = Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "\uD83D\uDCBE", color = Color.Black)
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

//@OptIn(ExperimentalMaterial3Api::class)
//@Preview(showBackground = true)
//@Composable
//fun Preview2() {
//    var noteTitle by remember { mutableStateOf("제목") }
//    var noteText by remember { mutableStateOf("") }
//    NoteProjectTheme {
//        Column {
//            Row {
//                Button(onClick = { /*TODO*/ }) {
//                    Text(text = "<")
//                }
//                TextField(
//                    value = noteTitle,
//                    onValueChange = { noteTitle = it },
//                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
//                )
//                Button(onClick = { /*TODO*/ }) {
//                    Text(text = "💾")
//                }
//            }
//            TextField(
//                value = noteText,
//                onValueChange = { noteText = it },
//                modifier = Modifier.fillMaxSize(),
//                colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
//            )
//        }
//    }
//}
