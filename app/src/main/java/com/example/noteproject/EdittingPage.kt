package com.example.noteproject

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.noteproject.data.Note
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EdittingPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {
                val targetUid = intent.getIntExtra("Uid", 0)
                val title = intent.getStringExtra("title") ?: "제목"
                val script = intent.getStringExtra("script") ?: ""
                ThisNoteEditPage(targetUid, title, script)
            }
        }
    }
}

@Composable
private fun ThisNoteEditPage(targetUid: Int, title: String, script: String) {
    val context = LocalContext.current
    val db = remember { NoteAppDatabase.getDatabase(context) }
    //엑티비티가 이동되면서 데이터를 로드하는데 그과정에서 처음에 noteList는 emptylist값을 갖고 있고 데이터가 안들어옴
    //그러면서 밑의 코드들이 작동
    //                val noteList by db.noteDao().getAll().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var editNoteTitle by remember { mutableStateOf(title) }
    var editNoteText by remember { mutableStateOf(script) }

    var recognizedText by remember { mutableStateOf("") }
    var isRecognitionEnabled by remember { mutableStateOf(true) }

    //데이터가 로드되지 않은 상태에서 noteList에 uid를 찾기 때문에 foundNote2는 null이 됨
    val foundNote by db.noteDao().getNoteByUid(targetUid).collectAsState(initial = null)
    //                val title = foundNote?.title?: ""
    //                val foundNote = noteList.find { it.uid == targetUid }

    //foundNote2는 null이기 때문에 uriList는 결과적으로 emptyList값으로 처음에 저장됨
    //그러나 데이터가 로드된 이후에는 noteList가 db에 값을 가져오게 되면서 foundNote2값에 변동이 생김
    //remember(key)는 키값에 변경이있을시 람다식 내부의 식을 한번 돌려주는 스코프임
    //그러므로 foundNote2에 값이 변동되었을때 람다식이 발동하면서 selectUris값이 바뀌게 됨
    //그이후 코드동작에서 또한번 foundNote2값이 변동이되면 selectUris값이 변동될것임.
    var selectUris by remember(foundNote) {
        val selectedUrisList = foundNote?.imageListString
        val uriList: List<Uri?>? =
            selectedUrisList?.map { uriString -> Uri.parse(uriString) }
        mutableStateOf<List<Uri?>>(uriList ?: emptyList())
    }

    val uriStringList: List<String?>? = if (selectUris.isNotEmpty()) {
        selectUris.map { uri -> uri.toString() }
    } else {
        null
    }
    Box {
        EditNoteLayout(
            editNoteTitle = editNoteTitle,
            editNoteText = editNoteText,
            isRecognitionEnabled = isRecognitionEnabled,
            uriStringList = uriStringList,
            scope = scope,
            db = db,
            selectUris = selectUris,
            recognizedText = recognizedText,
            foundNote = foundNote,
            onTitleChange = {
                editNoteTitle = it
            },
            onClicked = {
                selectUris -= it
            },
            onScriptChange = {
                editNoteText = it
                recognizedText = ""
                isRecognitionEnabled = true
            }
        )
        ImageAndVoiceBox(
            modifier = Modifier.align(Alignment.BottomEnd),
            isRecognitionEnabled = isRecognitionEnabled,
            onImageResult = {
                selectUris += it
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                for (uri in selectUris) {
                    uri?.let {
                        context.contentResolver.takePersistableUriPermission(uri, flag)
                    }
                }
            },
            onVoiceResult = {
                if (it.resultCode == RESULT_OK) {
                    val data: Intent? = it.data
                    val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (!results.isNullOrEmpty()) {
                        recognizedText = " ${results[0]}"
                        isRecognitionEnabled = false
                    }
                }
            },
            onResetClicked = {
                recognizedText = ""
                isRecognitionEnabled = true
            }
        )
    }
}

@Composable
private fun EditDoneIconButton(
    isRecognitionEnabled: Boolean,
    scope: CoroutineScope,
    foundNote: Note?,
    editNoteTitle: String,
    editNoteText: String,
    uriStringList: List<String?>?,
    db: NoteAppDatabase,
) {
    val context = LocalContext.current
    Icon(painter = painterResource(id = if (isRecognitionEnabled) R.drawable.baseline_save_24 else R.drawable.baseline_not_interested_24),
        contentDescription = "Save",
        modifier = Modifier
            .padding(10.dp)
            .size(height = 30.dp, width = 40.dp)
            .clickable(enabled = isRecognitionEnabled) {
                scope.launch(Dispatchers.IO) {
                    foundNote?.title = editNoteTitle
                    foundNote?.script = editNoteText
                    foundNote?.imageListString = uriStringList
                    if (foundNote != null) {
                        db
                            .noteDao()
                            .update(foundNote)
                    }
                }
                val intent = Intent(context, ShowTextPage::class.java)
                intent.putExtra("Uid", foundNote!!.uid)
                context.startActivity(intent)
            }
    )
}

@Composable
private fun EditPageBackIconButton(
    scope: CoroutineScope,
    foundNote: Note?,
    editNoteTitle: String,
    editNoteText: String,
    uriStringList: List<String?>?,
    db: NoteAppDatabase,
) {
    val context = LocalContext.current
    Icon(
        painter = painterResource(id = R.drawable.back),
        contentDescription = "Back Button",
        modifier = Modifier
            .padding(10.dp)
            .size(40.dp)
            .clickable {
                scope.launch(Dispatchers.IO) {
                    foundNote?.title = editNoteTitle
                    foundNote?.script = editNoteText
                    foundNote?.imageListString = uriStringList
                    if (foundNote != null) {
                        db
                            .noteDao()
                            .update(foundNote)
                    }
                }
                val intent = Intent(context, ShowTextPage::class.java)
                intent.putExtra("Uid", foundNote!!.uid)
                context.startActivity(intent)
            }
    )
}

@Composable
private fun EditNoteLayout(
    editNoteTitle: String,
    editNoteText: String,
    isRecognitionEnabled: Boolean,
    uriStringList: List<String?>?,
    scope: CoroutineScope,
    db: NoteAppDatabase,
    selectUris: List<Uri?>,
    recognizedText: String,
    foundNote: Note?,
    onTitleChange: (String) -> Unit,
    onClicked: (Uri) -> Unit,
    onScriptChange: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            EditPageBackIconButton(
                scope = scope,
                foundNote = foundNote,
                editNoteTitle = editNoteTitle,
                editNoteText = editNoteText,
                uriStringList = uriStringList,
                db = db
            )
            NoteTitle(
                noteTitle = editNoteTitle,
                onChange = { onTitleChange(it) },
                modifier = Modifier.weight(1f)
            )
            EditDoneIconButton(isRecognitionEnabled, scope, foundNote, editNoteTitle, editNoteText, uriStringList, db)
        }
        Divider()
        ShowSelectedImage(
            selectUris = selectUris,
            onClickImage = { onClicked(it) }
        )
        NoteScript(
            noteText = editNoteText,
            recognizedText = recognizedText,
            onChange = {
                onScriptChange(it)
            })
    }

}