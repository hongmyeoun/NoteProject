package com.example.noteproject

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

                val scope = rememberCoroutineScope()

                val foundNote = noteList.find { it.uid == targetUid }

                val selectedUrisList = foundNote?.imageListString
                val uriList: List<Uri?>? =
                    selectedUrisList?.map { uriString -> Uri.parse(uriString) }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back Button",
                            modifier = Modifier
                                .padding(10.dp)
                                .size(40.dp)
                                .clickable {
                                    val intent = Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)
                                }
                        )
                        Text(
                            text = foundNote?.title ?: "제목",
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = fontFamily()
                        )
                        if (foundNote != null) {
                            TTSComponent(foundNote.script!!, textToSpeech)
                        }
                        Spacer(modifier = Modifier.size(3.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "edit",
                            modifier = Modifier.clickable {
                                val intent = Intent(context, EdittingPage::class.java)
                                intent.putExtra("Uid", foundNote!!.uid)
                                intent.putExtra("title", foundNote.title)
                                intent.putExtra("script", foundNote.script)
                                context.startActivity(intent)
                            })
                        Spacer(modifier = Modifier.size(3.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete",
                            modifier = Modifier
                                .clickable {
                                    scope.launch(Dispatchers.IO) {
                                        db
                                            .noteDao()
                                            .delete(foundNote!!) // 선택한 노트를 삭제
                                    }
                                    val intent = Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)
                                })
                        Spacer(modifier = Modifier.size(3.dp))
                    }
                    Divider()
                    if (!uriList.isNullOrEmpty()){
                        ExpandableImageRow(uriList)
                    }
                    LazyColumn() {
                        item {
                            if (foundNote?.script != null) {
                                SelectionContainer {
                                    Text(
                                        text = foundNote.script ?: "",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(15.dp),
                                        fontFamily = fontFamily(),
                                        fontSize = 15.sp
                                    )
                                }
                            } else {
                                SelectionContainer {
                                    Text(
                                        text = "내용이없음",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(15.dp),
                                        fontFamily = fontFamily(),
                                        fontSize = 15.sp
                                    )
                                }
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
        painter = painterResource(id = R.drawable.volume),
        contentDescription = "tts play",
        modifier = Modifier
            .clickable {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
    )
}

@Composable
fun ExpandableImageRow(uriList: List<Uri?>) {
    var expandedIndex by remember { mutableStateOf(-1) }

    LazyRow(modifier=Modifier.padding(3.dp)) {
        itemsIndexed(uriList) { index, uri ->
            if (uri != null) {

                val isExpanded = index == expandedIndex

                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            LocalContext.current.contentResolver,
                            uri
                        )
                    )
                } else {
                    MediaStore.Images.Media.getBitmap(
                        LocalContext.current.contentResolver,
                        uri
                    )
                }

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(if (isExpanded) 410.dp else 100.dp)
                        .padding(2.dp)
                        .shadow(1.dp)
                        .clickable {
                            expandedIndex = if (isExpanded) -1 else index
                        }
                )
            }
        }
    }
}
