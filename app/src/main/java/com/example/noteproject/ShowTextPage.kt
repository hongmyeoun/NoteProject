package com.example.noteproject

import android.content.Context
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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.data.Note
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.CoroutineScope
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
                val targetUid = intent.getIntExtra("Uid", 0)
                ThisShowPage(targetUid = targetUid, textToSpeech = textToSpeech)
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
fun ThisShowPage(
    targetUid: Int,
    textToSpeech: TextToSpeech,
){
    val context = LocalContext.current
    val db = remember { NoteAppDatabase.getDatabase(context) }
    //State<Note?>라는 state를 가져와서 사용하는것인데 state에 접근할때는 value를 사용한다. 하지만 지금 상황은 바로 가져다 쓸것이므로 by를 사용해 바로 객체를 넘겨받는다.
    //initial = null인 부분은 초기값 즉 상태가 변하기 전까지 값이 없다는 뜻이다.
    val foundNote by db.noteDao().getNoteByUid(targetUid).collectAsState(initial = null)

    val scope = rememberCoroutineScope()

    val selectedUrisList = foundNote?.imageListString
    val uriList: List<Uri?>? =
        selectedUrisList?.map { uriString -> Uri.parse(uriString) }

    Box {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                BackIconButton()
                ShowTitle(foundNote, Modifier.weight(1f))
                if (foundNote != null) {
                    TTSIconButton(foundNote?.script ?: "", textToSpeech)
                }
                EditIconButton(foundNote)
                DeleteIconButton(scope, db, foundNote)
            }
            Divider()
            if (!uriList.isNullOrEmpty()) {
                ExpandableImageRow(uriList)
            }
            ShowNoteScript(foundNote)
        }
    }

}

@Composable
private fun ShowTitle(foundNote: Note?, modifier: Modifier){
    Text(
        text = foundNote?.title ?: "제목",
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontFamily = fontFamily()
    )
}

@Composable
private fun ShowNoteScript(foundNote: Note?) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
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

@Composable
private fun DeleteIconButton(
    scope: CoroutineScope,
    db: NoteAppDatabase,
    foundNote: Note?,
) {
    val context = LocalContext.current
    Icon(
        painter = painterResource(id = R.drawable.delete),
        contentDescription = "Delete",
        modifier = Modifier
            .clickable {
                scope.launch(Dispatchers.IO) { db.noteDao().delete(foundNote!!) }
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            })
    Spacer(modifier = Modifier.size(9.dp))
}

@Composable
private fun EditIconButton(
    foundNote: Note?
) {
    val context = LocalContext.current
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
    Spacer(modifier = Modifier.size(9.dp))

}

@Composable
fun BackIconButton() {
    val context = LocalContext.current
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
}

@Composable
fun TTSIconButton(text: String, tts: TextToSpeech) {
    Icon(
        painter = painterResource(id = R.drawable.volume),
        contentDescription = "tts play",
        modifier = Modifier
            .clickable {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
            .size(25.dp)
    )
    Spacer(modifier = Modifier.size(9.dp))
}

@Composable
fun ExpandableImageRow(uriList: List<Uri?>) {
    var expandedIndex by remember { mutableStateOf(-1) }

    LazyRow(modifier = Modifier.padding(3.dp)) {
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
                        .clickable {
                            expandedIndex = if (isExpanded) -1 else index
                        }
                )
            }
        }
    }
}
