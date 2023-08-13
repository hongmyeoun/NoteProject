package com.example.noteproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme

class ShowTextPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {
                val context = LocalContext.current
                val db = remember { NoteAppDatabase.getDatabase(context) }
                val noteList by db.noteDao().getAll().collectAsState(initial = emptyList())
                val targetUid = intent.getIntExtra("Uid", 0)

                val foundNote = noteList.find { it.uid == targetUid }

                Column {
                    Row {
                        Button(onClick = {
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Text(text = "<")
                        }
                        if (foundNote?.title != null) {
                            Text(text = foundNote.title ?:"제목")
                        } else {
                            Text(text = "제목")
                        }
                        Button(onClick = {
                            val intent = Intent(context, EdittingPage::class.java)
                            intent.putExtra("Uid", foundNote!!.uid)
                            intent.putExtra("title",foundNote.title)
                            intent.putExtra("script",foundNote.script)
                            context.startActivity(intent)
                        }) {
                            Text(text = "편집")
                        }
                    }
                    if (foundNote?.script != null) {
                        Text(text = foundNote.script ?:"", modifier = Modifier.fillMaxSize())
                    } else {
                        Text(text = "내용이없음", modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}