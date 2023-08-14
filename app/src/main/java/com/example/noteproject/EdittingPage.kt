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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EdittingPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {

                val context = LocalContext.current
                val db = remember { NoteAppDatabase.getDatabase(context) }
                val noteList by db.noteDao().getAll().collectAsState(initial = emptyList())
                val targetUid = intent.getIntExtra("Uid", 0)
                val title = intent.getStringExtra("title") ?: "제목"
                val script = intent.getStringExtra("script") ?: ""
                val scope = rememberCoroutineScope()

                val foundNote2 = noteList.find { it.uid == targetUid }

                var editNoteTitle by remember { mutableStateOf(title) }
                var editNoteText by remember { mutableStateOf(script) }

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
                            value = editNoteTitle,
                            onValueChange = { editNoteTitle = it },
                            colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(height = 30.dp, width = 40.dp)
                                .clickable {
                                    scope.launch(Dispatchers.IO) {
                                        foundNote2?.title = editNoteTitle
                                        foundNote2?.script = editNoteText
                                        if (foundNote2 != null) {
                                            db
                                                .noteDao()
                                                .update(foundNote2)
                                        }
                                    }
                                    val intent = Intent(context, ShowTextPage::class.java)
                                    intent.putExtra("Uid", foundNote2!!.uid)
                                    startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "\uD83D\uDCBE", color = Color.Black)
                        }
                    }
                    TextField(
                        value = editNoteText,
                        onValueChange = { editNoteText = it },
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
fun SHowPre() {
    NoteProjectTheme {

        var editNoteTitle by remember { mutableStateOf("제목") }
        var editNoteText by remember { mutableStateOf("내용") }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(text = "◁", color = Color.Black)
                }
                TextField(
                    value = editNoteTitle,
                    onValueChange = { editNoteTitle = it },
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
                )
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(text = "💾")
                }
            }
            TextField(
                value = editNoteText,
                onValueChange = { editNoteText = it },
                modifier = Modifier.fillMaxSize(),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
            )
        }
    }
}