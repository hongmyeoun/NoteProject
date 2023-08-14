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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        Text(
                            text = foundNote?.title ?: "제목",
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(height = 30.dp, width = 40.dp)
                                .border(1.dp, shape = RoundedCornerShape(5.dp), color = Color.Black)
                                .clickable {
                                    val intent = Intent(context, EdittingPage::class.java)
                                    intent.putExtra("Uid", foundNote!!.uid)
                                    intent.putExtra("title", foundNote.title)
                                    intent.putExtra("script", foundNote.script)
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center

                        ) {
                            Text(text = "편집", color = Color.Black)
                        }
                    }
                    Divider()
                    LazyColumn() {
                        item {
                            if (foundNote?.script != null) {
                                Text(
                                    text = foundNote.script ?: "",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(15.dp)
                                )
                            } else {
                                Text(
                                    text = "내용이없음",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Preee() {
    NoteProjectTheme {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)// ◁ 버튼 크기 지정
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "◁", color = Color.Black)
                }
                Text(
                    text = "제목근데김근데김근데김근데김근데김",
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis, // 제목이 길어질 경우 ...로 처리
                )
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(height = 30.dp, width = 40.dp) // 편집 버튼 크기 지정
                        .border(1.dp, shape = RoundedCornerShape(5.dp), color = Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "편집", color = Color.Black)
                }
            }
            Divider()
            LazyColumn() {
                item {
                    Text(
                        text = "내용이없음", modifier = Modifier
                            .fillMaxSize()
                            .padding(15.dp)
                    )

                }
            }
        }
    }
}