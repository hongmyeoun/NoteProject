package com.example.noteproject

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

                val selectUri = foundNote?.imageUri?.let { Uri.parse(it) } // 이미지 Uri 초기화

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
                            if (selectUri != null) {
                                val context = LocalContext.current
                                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    ImageDecoder.decodeBitmap(
                                        ImageDecoder.createSource(
                                            context.contentResolver,
                                            selectUri
                                        )
                                    )
                                } else {
                                    MediaStore.Images.Media.getBitmap(
                                        context.contentResolver,
                                        selectUri
                                    )
                                }
                                Image(
                                    bitmap = bitmap.asImageBitmap(), contentDescription = "",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .shadow(2.dp)
                                )
                            }
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
