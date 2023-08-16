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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
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

                var recognizedText by remember { mutableStateOf("") }
                var isRecognitionEnabled by remember { mutableStateOf(true) }

                var selectUris by remember { mutableStateOf<List<Uri?>>(emptyList()) }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickMultipleVisualMedia(),
                    onResult = { uris ->
                        selectUris = uris
                        //selectUris는 list이기 때문에 권한을 하나하나 다줘야 된다.
                        for (uri in selectUris) {
                            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            context.contentResolver.takePersistableUriPermission(uri!!, flag)
                        }
                    }
                )
                val uriStringList = selectUris.map { uri -> uri.toString() }

                val speechRecognizerLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val data: Intent? = result.data
                        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        if (!results.isNullOrEmpty()) {
                            recognizedText = " " + results[0]
                            isRecognitionEnabled = false
                        }
                    }
                }

                Box {
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
                            TextField(
                                value = noteTitle,
                                onValueChange = { noteTitle = it },
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
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                textStyle = TextStyle(
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = fontFamily()
                                )
                            )
                            Icon(
                                painter = painterResource(id = if (isRecognitionEnabled) R.drawable.baseline_save_24 else R.drawable.baseline_not_interested_24),
                                contentDescription = "Save",
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(height = 30.dp, width = 40.dp)
                                    .clickable(enabled = isRecognitionEnabled) {
                                        val currentDate = SimpleDateFormat(
                                            "yy.MM.dd", Locale.getDefault()
                                        ).format(
                                            Date()
                                        )
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
                        Divider()
                        LazyRow() {
                            item {
                                val removedUris = remember { mutableSetOf<Uri>() }
                                if (selectUris.isNotEmpty()) {
                                    for (uri in selectUris) {
                                        if (uri in removedUris){
                                            continue
                                        }
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
                                                .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        removedUris.add(uri!!)
                                                        selectUris = selectUris - uri
                                                    }
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        TextField(
                            value = noteText + recognizedText,
                            onValueChange = {
                                noteText = it
                                recognizedText = ""
                                isRecognitionEnabled = true
                            },
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
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 30.dp, bottom = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            Icon(painter = painterResource(id = R.drawable.image_icon),
                                contentDescription = "get image",
                                modifier = Modifier
                                    .clickable {
                                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                    .size(50.dp))

                            Icon(painter = painterResource(id = if (isRecognitionEnabled) R.drawable.baseline_mic_24 else R.drawable.baseline_mic_off_24),
                                contentDescription = "mic",
                                modifier = Modifier
                                    .clickable(enabled = isRecognitionEnabled) {
                                        val intent =
                                            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                        intent.putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        speechRecognizerLauncher.launch(intent)
                                    }
                                    .size(50.dp))
                            Icon(painter = painterResource(id = R.drawable.baseline_restart_alt_24),
                                contentDescription = "recognized reset",
                                modifier = Modifier
                                    .clickable {
                                        recognizedText = ""
                                        isRecognitionEnabled = true
                                    }
                                    .size(50.dp))
                        }
                    }
                }
            }
        }
    }
}