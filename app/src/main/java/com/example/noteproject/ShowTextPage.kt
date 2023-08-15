package com.example.noteproject

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import java.io.File
import java.io.FileOutputStream
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
                val launcher2 = rememberLauncherForActivityResult(
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
                                    launcher2.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                })
                        if (foundNote != null) {
                            TTSComponent(foundNote.script!!, textToSpeech)
                        }
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

                    }
                    Divider()
                    LazyRow() {
                        item {
                            if (selectUris.isNotEmpty()) {
                                saveImagesToInternalStorage(context, selectUris)
                            }
                            val loadedImages = loadImagesFromInternalStorage(context)
                            for (bitmap in loadedImages) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Loaded Image",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(4.dp)
                                )
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

private fun saveImagesToInternalStorage(context: Context, uris: List<Uri?>) {
    val directory = File(context.filesDir, "images")
    if (!directory.exists()) {
        directory.mkdirs()
    }
    for ((index, uri) in uris.withIndex()) {
        val inputStream = context.contentResolver.openInputStream(uri!!)
        val file = File(directory, "image_$index.jpg")
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }
}

private fun loadImagesFromInternalStorage(context: Context): List<Bitmap> {
    val imageBitmaps = mutableListOf<Bitmap>()
    val directory = File(context.filesDir, "images")

    if (directory.exists()) {
        val imageFiles = directory.listFiles()

        imageFiles?.forEach { file ->
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                imageBitmaps.add(bitmap)
            }
        }
    }
    return imageBitmaps
}