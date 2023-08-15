package com.example.noteproject

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.util.Locale

class ShowTextPage : ComponentActivity() {
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    // Handle language data missing or not supported
                }
            }
        }

        setContent {
            NoteProjectTheme {
                val context = LocalContext.current
                val db = remember { NoteAppDatabase.getDatabase(context) }
                val noteList by db.noteDao().getAll().collectAsState(initial = emptyList())
                val targetUid = intent.getIntExtra("Uid", 0)
                var selectUris by remember { mutableStateOf<List<Uri?>>(emptyList()) }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickMultipleVisualMedia(),
                    onResult = { uris ->
                        selectUris = uris
                    }
                )


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
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "get Image",
                            modifier = Modifier
                                .clickable {
                                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                })
                        if (foundNote != null) {
                            TTSComponent(foundNote.script!!,textToSpeech)
                        }
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
                    LazyRow() {
                        item {
                            if (selectUris.isNotEmpty()) {
                                for (uri in selectUris) {

                                    val bitmap =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                            ImageDecoder.decodeBitmap(
                                                ImageDecoder.createSource(
                                                    context.contentResolver,
                                                    uri!!
                                                )
                                            )
                                        } else {
                                            MediaStore.Images.Media.getBitmap(
                                                context.contentResolver,
                                                uri
                                            )
                                        }
                                    Image(
                                        bitmap = bitmap.asImageBitmap(), contentDescription = "",
                                        modifier = Modifier
                                            .size(100.dp)
                                            .shadow(2.dp)
                                    )
                                }
                            }
                        }
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}

@Composable
fun TTSComponent(text: String, tts: TextToSpeech) {
    Icon(
        imageVector = Icons.Default.PlayArrow,
        contentDescription = "tts play",
        modifier = Modifier
            .clickable {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        )
}