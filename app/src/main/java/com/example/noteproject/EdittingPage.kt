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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EdittingPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteProjectTheme {

                val context = LocalContext.current
                val db = remember { NoteAppDatabase.getDatabase(context) }
                val noteList by db.noteDao().getAll().collectAsState(initial = emptyList())
                val targetUid = intent.getIntExtra("Uid", 0)
                val title = intent.getStringExtra("title") ?: "제목"
                val script = intent.getStringExtra("script") ?: ""
                val scope = rememberCoroutineScope()

                val foundNote2 = noteList.find { it.uid == targetUid }

                var editNoteTitle by remember { mutableStateOf(title) }
                var editNoteText by remember { mutableStateOf(script) }

                var recognizedText by remember { mutableStateOf("") }
                var isRecognitionEnabled by remember { mutableStateOf(true) }

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
                val selectedUrisList = foundNote2?.imageListString
                var uriList:List<Uri?>? = selectedUrisList?.map { uriString -> Uri.parse(uriString) }

                var selectUris by remember { mutableStateOf<List<Uri?>>(emptyList()) }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickMultipleVisualMedia(),
                    onResult = { uris ->
                        //기존에 골랐던 사진에 추가로 들어가기
                        selectUris += uris
                        //selectUris는 list이기 때문에 권한을 하나하나 다줘야 된다.
                        for (uri in selectUris) {
                            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            context.contentResolver.takePersistableUriPermission(uri!!, flag)
                        }
                    }
                )
                val uriStringList = selectUris.map { uri -> uri.toString() }


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
                                value = editNoteTitle,
                                onValueChange = { editNoteTitle = it },
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
                            Icon(painter = painterResource(id = if (isRecognitionEnabled) R.drawable.baseline_save_24 else R.drawable.baseline_not_interested_24),
                                contentDescription = "Save",
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(height = 30.dp, width = 40.dp)
                                    .clickable(enabled = isRecognitionEnabled) {
                                        scope.launch(Dispatchers.IO) {
                                            foundNote2?.title = editNoteTitle
                                            foundNote2?.script = editNoteText
                                            foundNote2?.imageListString = uriStringList
                                            if (foundNote2 != null) {
                                                db.noteDao().update(foundNote2)
                                            }
                                        }
                                        val intent = Intent(context, ShowTextPage::class.java)
                                        intent.putExtra("Uid", foundNote2!!.uid)
                                        startActivity(intent)
                                    }
                            )
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
//                                if (uriList != null) {
//                                    if (uriList!!.isNotEmpty()) {
//                                        for (uri in uriList!!) {
//                                            val bitmap =
//                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                                                    ImageDecoder.decodeBitmap(
//                                                        ImageDecoder.createSource(
//                                                            context.contentResolver,
//                                                            uri!!
//                                                        )
//                                                    )
//                                                } else {
//                                                    MediaStore.Images.Media.getBitmap(
//                                                        context.contentResolver,
//                                                        uri
//                                                    )
//                                                }
//                                            Image(
//                                                bitmap = bitmap.asImageBitmap(),
//                                                contentDescription = "",
//                                                modifier = Modifier
//                                                    .size(100.dp)
//                                                    .shadow(2.dp)
//                                            )
////                                            selectUris += uri!!
//                                        }
//                                    }
//                                }
                            }
                        }
                        TextField(
                            value = editNoteText + recognizedText,
                            onValueChange = {
                                editNoteText = it
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
                                        selectUris = uriList!!
                                        uriList = null
                                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                    .size(50.dp))
                            Icon(
                                painter = painterResource(id = if (isRecognitionEnabled) R.drawable.baseline_mic_24 else R.drawable.baseline_mic_off_24),
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
                                    .size(50.dp)
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_restart_alt_24),
                                contentDescription = "mic",
                                modifier = Modifier
                                    .clickable {
                                        recognizedText = ""
                                        isRecognitionEnabled = true
                                    }
                                    .size(50.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
