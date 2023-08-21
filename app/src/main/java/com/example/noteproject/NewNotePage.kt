package com.example.noteproject

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.data.Note
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.CoroutineScope
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

                var selectUris by remember { mutableStateOf<List<Uri?>>(emptyList()) }
                val uriStringList: List<String?>? = if (selectUris.isNotEmpty()) {
                    selectUris.map { uri -> uri.toString() }
                } else {
                    null
                }

                var recognizedText by remember { mutableStateOf("") }
                var isRecognitionEnabled by remember { mutableStateOf(true) }


                Box {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BackIconButton()
                            NoteTitle(
                                noteTitle = noteTitle,
                                onChange = { noteTitle = it },
                                modifier = Modifier.weight(1f)
                            )
                            SaveIconButton(isRecognitionEnabled, uriStringList, noteTitle, noteText, scope, db)
                        }
                        Divider()
                        ShowSelectedImage(selectUris = selectUris, onClickImage = { selectUris -= it })
                        NoteScript(
                            noteText = noteText,
                            recognizedText = recognizedText,
                            onChange = {
                                noteText = it
                                recognizedText = ""
                                isRecognitionEnabled = true
                            })
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 30.dp, bottom = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            GetImagesIconButton(selectUris = selectUris, onResult = { selectUris += it })
                            GetVoiceTextIconButtons(
                                isRecognitionEnabled = isRecognitionEnabled,
                                onResult = {
                                    if (it.resultCode == ComponentActivity.RESULT_OK) {
                                        val data: Intent? = it.data
                                        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                        if (!results.isNullOrEmpty()) {
                                            recognizedText = " ${results[0]}"
                                            isRecognitionEnabled = false
                                        }
                                    }
                                }
                            )
                            CustomIcon(
                                id = R.drawable.baseline_restart_alt_24,
                                contentDescription = "recognized reset",
                                onClicked = {
                                    recognizedText = ""
                                    isRecognitionEnabled = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun GetVoiceTextIconButtons(isRecognitionEnabled: Boolean, onResult:(ActivityResult)->Unit){
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onResult(result)
    }
    CustomIcon(
        id = if (isRecognitionEnabled) R.drawable.baseline_mic_24 else R.drawable.baseline_mic_off_24,
        contentDescription = "mic",
        enabled = isRecognitionEnabled,
        onClicked = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            speechRecognizerLauncher.launch(intent)
        }
    )
}

@Composable
fun GetImagesIconButton(selectUris: List<Uri?>, onResult: (List<Uri?>) -> Unit){
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            //기존에 골랐던 사진에 추가로 들어가기
            onResult(uris)
            //selectUris는 list이기 때문에 권한을 하나하나 다줘야 된다.
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            for (uri in selectUris) {
                uri?.let {
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                }
            }
        }
    )
    CustomIcon(
        id = R.drawable.image_icon,
        contentDescription = "get image",
        onClicked = {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScript(noteText: String, recognizedText: String, onChange: (String) -> Unit) {
    TextField(
        value = noteText + recognizedText,
        onValueChange = { onChange(it) },
        modifier = Modifier.fillMaxSize(),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontFamily = fontFamily()
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTitle(noteTitle: String, onChange: (String) -> Unit, modifier: Modifier) {
    TextField(
        value = noteTitle,
        onValueChange = { onChange(it) },
        modifier = modifier,
        placeholder = {
            Text(
                text = "제목",
                fontStyle = FontStyle.Italic,
                fontSize = 25.sp,
                fontFamily = fontFamily()
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        maxLines = 1,
        textStyle = TextStyle(
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily()
        )
    )
}

@Composable
fun ShowSelectedImage(selectUris: List<Uri?>, onClickImage: (Uri) -> Unit) {
    val context = LocalContext.current
    LazyRow() {
        items(selectUris) { uri ->
            uri?.let {
                val bitmap =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(context.contentResolver, uri)
                        )
                    } else {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable {
                            onClickImage(uri)
                        }
                )
            }
        }
    }
}


@Composable
fun CustomIcon(
    id: Int,
    contentDescription: String,
    enabled: Boolean = true,
    onClicked: () -> Unit
) {
    Icon(painter = painterResource(id = id),
        contentDescription = contentDescription,
        modifier = Modifier
            .clickable(enabled = enabled) { onClicked() }
            .size(50.dp))

}

@Composable
private fun SaveIconButton(
    isRecognitionEnabled: Boolean,
    uriStringList: List<String?>?,
    noteTitle: String,
    noteText: String,
    scope: CoroutineScope,
    db: NoteAppDatabase,
) {
    val context = LocalContext.current
    Icon(
        painter = painterResource(id = if (isRecognitionEnabled) R.drawable.baseline_save_24 else R.drawable.baseline_not_interested_24),
        contentDescription = "Save",
        modifier = Modifier
            .padding(10.dp)
            .size(height = 30.dp, width = 40.dp)
            .clickable(enabled = isRecognitionEnabled) {
                val currentDate = SimpleDateFormat("yy.MM.dd HH:mm", Locale.getDefault()).format(Date())
                val newNote = Note(
                    title = noteTitle,
                    script = noteText,
                    createdDate = currentDate,
                    imageListString = uriStringList
                )
                scope.launch(Dispatchers.IO) {
                    db
                        .noteDao()
                        .insertAll(newNote)
                }
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            })
}
