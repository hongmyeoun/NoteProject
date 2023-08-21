package com.example.noteproject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.view.WindowInsets.Type.systemBars
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.noteproject.data.Note
import com.example.noteproject.data.NoteAppDatabase
import com.example.noteproject.ui.theme.NoteProjectTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                val scope = rememberCoroutineScope()

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        NoteMainPage(noteList, navController, context, scope, db)
                    }
                    composable("search") {
                        NoteSearchPage(navController, noteList, context)
                    }
                }
            }
        }
    }
}
@Composable
fun NoteMainPage(noteList: List<Note>, navController: NavHostController, context: Context, scope: CoroutineScope, db: NoteAppDatabase){
    Box() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            NoteTopLayout(noteList)
            NoteBottomLayout(noteList, navController, context, scope, db)
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 50.dp),
            contentAlignment = Alignment.Center
        ) {
            NewNoteAction(context)
        }
    }

}

@Composable
fun NoteBottomLayout(noteList: List<Note>, navController: NavHostController, context: Context, scope: CoroutineScope, db: NoteAppDatabase){
    val activity = LocalContext.current as? Activity
    val sharedPref =
        remember { activity?.getPreferences(Context.MODE_PRIVATE) }
    var upSort by remember {
        val upSortValue = sharedPref?.getBoolean("upSort", true) ?: true
        mutableStateOf(upSortValue)
    }
    var sortOption by remember {
        val sortOptionValue =
            sharedPref?.getString("sortOption", SortOption.TITLE.name)
                ?: SortOption.TITLE.name
        mutableStateOf(sortOptionValue)
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        val sortedNoteList = when (sortOption) {
            SortOption.TITLE.name -> if (upSort) {
                noteList.sortedBy { it.title }
            } else {
                noteList.sortedByDescending { it.title }
            }

            else -> if (upSort) {
                noteList.sortedBy { it.createdDate }
            } else {
                noteList.sortedByDescending { it.createdDate }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    modifier = Modifier.clickable {
                        navController.navigate("search")
                    })
                Row(modifier = Modifier.clickable {
                    sortOption = when (sortOption) {
                        SortOption.TITLE.name -> SortOption.CREATED_DATE.name
                        else -> SortOption.TITLE.name
                    }
                    titleAndTimeSortEdit(sharedPref, sortOption)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.sort),
                        contentDescription = "Sort by title or time"
                    )
                    Text(
                        when (sortOption) {
                            SortOption.TITLE.name -> "제목순"
                            else -> "날짜순"
                        }
                    )
                }
                Spacer(modifier = Modifier.size(3.dp))
                Icon(painter = painterResource(id = if (upSort) R.drawable.arrow_upward else R.drawable.baseline_arrow_downward_24),
                    contentDescription = "Sort by up or down",
                    modifier = Modifier.clickable {
                        upSort = !upSort
                        upAndDownSortEdit(sharedPref, upSort)
                    }
                )
            }
        }

        val columns = 3 // 열 개수

        val chunkedNoteList = sortedNoteList.chunked(columns)

        items(chunkedNoteList.size) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (note in chunkedNoteList[rowIndex]) {
                    NoteItems(context, note, scope, db)
                }
            }
        }
    }

}

@Composable
fun NoteItems(context: Context, note: Note, scope: CoroutineScope, db: NoteAppDatabase){
    var deletPressed by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(9.dp)
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
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        note.let {
            if (deletPressed) {
                DeleteAlet(
                    onDismiss = {
                        deletPressed = false
                    },
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            db.noteDao().delete(note)
                        }
                        deletPressed = false
                    }
                )
            }
        }
        NoteBox(note, context)
        NoteTitle(note)
        NoteDate(note)
    }
}




@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NoteSearchPage(
    navController: NavHostController,
    noteList: List<Note>,
    context: Context
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        var searchText by remember { mutableStateOf("") }
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
                        .clickable { navController.navigate("main") }
                )
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = {
                        Text(
                            text = "검색",
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
                val speechRecognizerLauncher =
                    rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == RESULT_OK) {
                            val data: Intent? = result.data
                            val results =
                                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                            if (!results.isNullOrEmpty()) {
                                searchText = results[0]
                            }
                        }
                    }
                Icon(
                    painter = painterResource(id = R.drawable.baseline_mic_24),
                    contentDescription = "STT",
                    modifier = Modifier
                        .padding(10.dp)
                        .size(height = 30.dp, width = 40.dp)
                        .clickable() {
                            val intent =
                                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            intent.putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            speechRecognizerLauncher.launch(intent)
                        }
                )
            }
            val searchResult =
                remember(searchText) { searchNotes(searchText, noteList) }

            if (searchText.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    val columns = 3
                    val chunkedNoteList = searchResult.chunked(columns)

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
                                        .clickable {
                                            val intent = Intent(
                                                context,
                                                ShowTextPage::class.java
                                            )
                                            intent.putExtra("Uid", note.uid)
                                            context.startActivity(intent)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    SearchNoteBox(note, context, searchText)
                                    SearchingTitle(note, searchText)
                                    NoteDate(note)
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "검색으로 노트찾기")
                }
            }
        }
    }
}


fun searchNotes(searchText: String, noteList: List<Note>): List<Note> {
    val searchResult = mutableListOf<Note>()
    for (note in noteList) {
        if (note.title!!.contains(other = searchText, ignoreCase = true) ||
            note.script?.contains(other = searchText, ignoreCase = true) == true
        ) {
            searchResult.add(note)
        }
    }
    return searchResult
}

fun upAndDownSortEdit(sharedPref: SharedPreferences?, sort: Boolean) {
    sharedPref?.edit {
        putBoolean("upSort", sort)
    }
}

fun titleAndTimeSortEdit(sharedPref: SharedPreferences?, sort: String) {
    sharedPref?.edit {
        putString("sortOption", sort)
    }
}

enum class SortOption {
    TITLE, CREATED_DATE
}

@Composable
private fun NewNoteAction(context: Context) {
    Icon(
        painter = painterResource(id = R.drawable.note_add),
        contentDescription = "newNote",
        modifier = Modifier
            .clickable {
                val intent = Intent(context, NewNotePage::class.java)
                context.startActivity(intent)
            }
            .size(50.dp)
    )
}

@Composable
private fun NoteBox(note: Note, context: Context) {
    Box(
        modifier = Modifier
            .size(height = 170.dp, width = 110.dp)
            .shadow(
                0.3f.dp,
                shape = RoundedCornerShape(1.dp)
            )
            .padding(10.dp)
    ) {
        NoteScript(note, context)
    }
}

@Composable
private fun SearchNoteBox(note: Note, context: Context, searchText: String) {
    Box(
        modifier = Modifier
            .size(height = 170.dp, width = 120.dp)
            .shadow(
                0.3f.dp,
                shape = RoundedCornerShape(1.dp)
            )
            .padding(10.dp)
    ) {
        SearchingScript(note, context, searchText)
    }
}

@Composable
private fun NoteDate(note: Note) {
    val currentDate = Date()
    val dateUtils = DateUtils()
    val noteDate = dateUtils.stringToDate(note.createdDate)
    val formattedDate = if (dateUtils.isToday(noteDate)) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(noteDate) // 오늘이면 HH:mm 형식
    } else if (dateUtils.isSameYear(noteDate, currentDate)) {
        SimpleDateFormat("MM.dd", Locale.getDefault()).format(noteDate) // 같은 해이면 MM.dd 형식
    } else {
        SimpleDateFormat("yy.MM.dd", Locale.getDefault()).format(noteDate) // 다른 해면 yy.MM.dd 형식
    }

    Text(
        text = formattedDate,
        fontWeight = FontWeight.Light,
        fontSize = 10.sp,
        color = Color.LightGray,
        fontFamily = fontFamily()
    )
    Spacer(modifier = Modifier.height(20.dp))
}

class DateUtils {
    fun isSameYear(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    fun stringToDate(dateString: String): Date {
        val std = SimpleDateFormat("yy.MM.dd HH:mm", Locale.getDefault())
        return std.parse(dateString)!!
    }

    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val comparisonDate = Calendar.getInstance()
        comparisonDate.time = date
        return today.get(Calendar.YEAR) == comparisonDate.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == comparisonDate.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == comparisonDate.get(Calendar.DAY_OF_MONTH)
    }
}

@Composable
private fun NoteTitle(note: Note) {
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = "${note.title}",
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        fontFamily = fontFamily(),
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SearchingTitle(note: Note, searchText: String) {
    val highLightTitle = buildAnnotatedString {
        val title = note.title ?: ""
        val index = title.indexOf(searchText, ignoreCase = true)
        if (index != -1) {
            append(title.substring(0, index))
            withStyle(style = SpanStyle(background = Color.Yellow)) {
                append(title.substring(index, index + searchText.length))
            }
            append(title.substring(index + searchText.length))
        } else {
            append(title)
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = highLightTitle,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        fontFamily = fontFamily(),
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun NoteScript(note: Note, context: Context) {
    Column {
        ShowDBImage(note, context)
        Text(
            text = note.script!!,
            fontFamily = fontFamily(),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SearchingScript(note: Note, context: Context, searchText: String) {
    val highLightScript = buildAnnotatedString {
        val script = note.script ?: ""
        val index = script.indexOf(searchText, ignoreCase = true)
        if (index != -1) {
            append(script.substring(0, index))
            withStyle(style = SpanStyle(background = Color.Yellow)) {
                append(script.substring(index, index + searchText.length))
            }
            append(script.substring(index + searchText.length))
        } else {
            append(script)
        }
    }
    Column {
        ShowDBImage(note, context)
        Text(
            text = highLightScript,
            fontFamily = fontFamily(),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun NoteTopLayout(noteList: List<Note>) {
    Spacer(modifier = Modifier.size(50.dp))
    Text(
        text = "나의 메모장",
        fontSize = 50.sp,
        fontFamily = fontFamily()
    )
    Text(
        text = "노트 ${noteList.size}개",
        fontWeight = FontWeight.Light,
        fontSize = 10.sp,
        fontFamily = fontFamily()
    )
    Spacer(modifier = Modifier.size(50.dp))
}

@Composable
private fun ShowDBImage(note: Note, context: Context) {
    val selectedUrisList = note.imageListString
    val uriList: List<Uri?>? =
        selectedUrisList?.map { uriString ->
            Uri.parse(uriString)
        }
    LazyRow() {
        if (!uriList.isNullOrEmpty()) {
            items(uriList) { uri ->
                if (uri != null) {
                    val bitmap =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(
                                    context.contentResolver,
                                    uri
                                )
                            )
                        } else {
                            MediaStore.Images.Media.getBitmap(
                                context.contentResolver,
                                uri
                            )
                        }
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "",
                        modifier = Modifier
                            .size(33.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun fontFamily() = FontFamily(Font(R.font.roboto))

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
                Text(text = "삭제!")
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