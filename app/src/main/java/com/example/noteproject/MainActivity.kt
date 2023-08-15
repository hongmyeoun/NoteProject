package com.example.noteproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets.Type.systemBars
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.noteproject.data.Note
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

//    private val READ_REQUEST_CODE = 42
//
//    private val requestPermissionLauncher: ActivityResultLauncher<String> =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                openMediaDocument()
//            } else {
//                // Handle permission denied
//            }
//        }
//
//    private val pickMediaLauncher: ActivityResultLauncher<String> =
//        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//            if (uri != null) {
//                // Process the selected media URI
//                // Here you can use the URI to display or manipulate the media
//            }
//        }


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
//        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
//            openMediaDocument()
//        } else {
//            requestPermissionLauncher.launch(permission)
//        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(systemBars())

        setContent {
            NoteProjectTheme {

                val context = LocalContext.current

                val db = remember { NoteAppDatabase.getDatabase(context) }
                val noteList by db.noteDao().getAll().collectAsState(initial = emptyList())
                var deletPressed by remember { mutableStateOf(false) }
                var deletingNote by remember { mutableStateOf<Note?>(null) }

                val scope = rememberCoroutineScope()

                Box() {

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.size(50.dp))
                        Text(text = "모든 노트", fontWeight = FontWeight.Bold, fontSize = 25.sp)
                        Text(
                            text = "노트 ${noteList.size}개",
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.size(50.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val columns = 3 // 열 개수

                            val chunkedNoteList = noteList.chunked(columns)

                            items(chunkedNoteList.size) { rowIndex ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (note in chunkedNoteList[rowIndex]) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .pointerInput(Unit) {
                                                    detectTapGestures(
                                                        onTap = {
                                                            val intent = Intent(
                                                                context,
                                                                ShowTextPage::class.java
                                                            )
                                                            intent.putExtra("Uid", note.uid)
                                                            context.startActivity(intent)
                                                        },
                                                        onLongPress = {
                                                            deletPressed = true
                                                            deletingNote = note // 삭제될 노트 저장
                                                        }
                                                    )
                                                },
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            deletingNote?.let { note ->
                                                if (deletPressed) {
                                                    DeleteAlet(
                                                        onDismiss = {
                                                            deletPressed = false
                                                            deletingNote =
                                                                null // 삭제 모드가 해제되면 노트도 초기화
                                                        },
                                                        onDelete = {
                                                            scope.launch(Dispatchers.IO) {
                                                                db.noteDao()
                                                                    .delete(note) // 선택한 노트를 삭제
                                                            }
                                                            deletPressed = false // 삭제 완료 후 삭제 모드 해제
                                                            deletingNote = null // 삭제 완료 후 노트 초기화
                                                        }
                                                    )
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(height = 170.dp, width = 120.dp)
                                                    .shadow(
                                                        0.3f.dp,
                                                        shape = RoundedCornerShape(1.dp)
                                                    )
                                                    .padding(10.dp)

                                            ) {
                                                Text(text = note.script!!)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${note.title}",
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(text = "${note.createdDate}")
                                            Spacer(modifier = Modifier.height(20.dp))

                                        }
                                    }
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 30.dp, bottom = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(context, NewNotePage::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(60.dp)
                                .shadow(1.dp, shape = CircleShape)
                                .align(Alignment.Center),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = CircleShape
                        ) {
                            Text(
                                "📝",
                                color = Color.Black,
                                fontSize = 20.sp,
                            )
                        }
                    }
                }
            }
        }
    }
//    private fun openMediaDocument() {
//        pickMediaLauncher.launch("image/*") // Change "image/*" to the desired media type
//    }
}


@Composable
fun DeleteAlet(onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "삭제하시겠습니까?")
        },
        text = {
            Text(text = "진짜진짜 삭제됩니다ㅠㅠ 하실껀가요??")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete()
                    onDismiss()
                }
            ) {
                Text(text = "진행시켜!")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}