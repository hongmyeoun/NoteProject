package com.example.noteproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.data.Note
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {
                val context = LocalContext.current
                val db = remember { NoteAppDatabase.getDatabase(context) }
                val noteList by db.noteDao().getAll().collectAsState(initial = emptyList())

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.size(50.dp))
                    Text(text = "모든 노트", fontWeight = FontWeight.Bold, fontSize = 25.sp)
                    Text(text = "노트 30개", fontWeight = FontWeight.Light, fontSize = 10.sp)
                    Spacer(modifier = Modifier.size(50.dp))
                    Column() {
                        for (note in noteList) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(7.dp)
                                    .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                                    .clickable {
                                        val intent = Intent(context, ShowTextPage::class.java)
                                        intent.putExtra("Uid", note.uid)
                                        context.startActivity(intent)
                                    }
                            ) {
                                Column() {
                                    UserItem(note)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = 16.dp, end = 16.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(context, NewNotePage::class.java)
                                context.startActivity(intent)
                            },
                        ) {
                            Text("📝")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserItem(note: Note) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = note.title!!, fontWeight = FontWeight.Bold)

    }
}