package az.leafreader

import android.text.Html
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

data class ChapterSearchHit(val chapter:Int,val snippet:String)

private fun plainChapter(file:File):String {
    val html=file.readText(Charsets.UTF_8)
        .replace(Regex("<script\\b[^>]*>.*?</script>",
            setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL))," ")
        .replace(Regex("<style\\b[^>]*>.*?</style>",
            setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL))," ")
    return Html.fromHtml(html,Html.FROM_HTML_MODE_COMPACT).toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSearchSheet(
    chapters:List<File>,
    labels:List<String>,
    onJump:(Int,String)->Unit,
    onSaveQuote:(Int,String)->Unit,
    onDismiss:()->Unit
) {
    var query by remember {mutableStateOf("")}
    var results by remember {mutableStateOf<List<ChapterSearchHit>>(emptyList())}
    var searching by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf<String?>(null)}
    LaunchedEffect(query,chapters) {
        results=emptyList()
        error=null
        val target=ReaderSearch.normalize(query)
        if(target.length<2)return@LaunchedEffect
        delay(250)
        searching=true
        try {
            results=withContext(Dispatchers.IO) {
                buildList {
                    chapters.forEachIndexed {chapter,file->
                        if(size>=80)return@forEachIndexed
                        ReaderSearch.snippets(plainChapter(file),target,6)
                            .take(80-size).forEach {add(ChapterSearchHit(chapter,it))}
                    }
                }
            }
        }catch(e:Exception){error=e.message ?: "Axtarış xətası"}
        finally {searching=false}
    }
    ModalBottomSheet(onDismissRequest=onDismiss) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.82f).padding(horizontal=20.dp)) {
            Text("Kitab daxilində axtarış",fontSize=23.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),
                singleLine=true,label={Text("Söz və ya ifadə")},
                leadingIcon={Icon(Icons.Default.Search,null)})
            Spacer(Modifier.height(10.dp))
            Text(if(searching)"Axtarılır…" else if(query.trim().length<2)
                "Ən azı 2 simvol daxil et" else "${results.size} nəticə (maksimum 80)",
                fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            if(error!=null)Text(error ?: "",color=MaterialTheme.colorScheme.error)
            if(searching)LinearProgressIndicator(Modifier.fillMaxWidth())
            LazyColumn(contentPadding=PaddingValues(bottom=32.dp)) {
                items(results) {hit->
                    Column(Modifier.fillMaxWidth().clickable {onJump(hit.chapter,query)}
                        .padding(vertical=12.dp)) {
                        Text(labels.getOrNull(hit.chapter) ?: "Fəsil ${hit.chapter+1}",
                            fontWeight=FontWeight.SemiBold)
                        Text(hit.snippet,fontSize=13.sp,maxLines=4)
                        Row {
                            TextButton(onClick={onJump(hit.chapter,query)}) {
                                Text("Fəsilə keç")
                            }
                            TextButton(onClick={onSaveQuote(hit.chapter,hit.snippet)}) {
                                Text("Qeyd / sitat saxla")
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingNotesSheet(
    store:BookStore,bookPath:String,currentChapter:Int,
    initialQuote:String="",
    onJump:(Int)->Unit,
    onClose:()->Unit
) {
    var refresh by remember {mutableIntStateOf(0)}
    var quote by remember(initialQuote) {mutableStateOf(initialQuote)}
    var note by remember {mutableStateOf("")}
    var editingId by remember {mutableStateOf<String?>(null)}
    var chapter by remember {mutableIntStateOf(currentChapter)}
    val notes=remember(refresh,bookPath) {store.annotations(bookPath)}
    ModalBottomSheet(onDismissRequest=onClose) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.84f).padding(horizontal=20.dp)) {
            Text("Qeydlər və sitatlar",fontSize=23.sp,fontWeight=FontWeight.Bold)
            Text("Fəsil ${chapter+1} · offline saxlanılır",
                fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(quote,{quote=it},modifier=Modifier.fillMaxWidth(),
                label={Text("İşarələnəcək mətn (istəyə bağlı)")},maxLines=3)
            OutlinedTextField(note,{note=it},modifier=Modifier.fillMaxWidth(),
                label={Text("Şəxsi qeyd")},minLines=2,maxLines=4)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End) {
                if(editingId!=null)TextButton(onClick={
                    editingId=null;quote="";note="";chapter=currentChapter
                }) {Text("Ləğv et")}
                Button(enabled=quote.isNotBlank() || note.isNotBlank(),onClick={
                    store.saveAnnotation(bookPath,chapter,quote,note,editingId)
                    editingId=null;quote="";note="";chapter=currentChapter
                    refresh++
                }) {Text(if(editingId==null)"Saxla" else "Yenilə")}
            }
            Text("Saxlanılmış qeydlər · ${notes.size}",
                fontWeight=FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            LazyColumn(contentPadding=PaddingValues(bottom=32.dp)) {
                items(notes,key={it.id}) {item->
                    ListItem(
                        headlineContent={Text(item.quote.ifBlank {"Fəsil ${item.chapter+1} qeydi"},
                            maxLines=3)},
                        supportingContent={Column {
                            Text("Fəsil ${item.chapter+1}",fontSize=12.sp)
                            if(item.note.isNotBlank())Text(item.note,maxLines=3)
                        }},
                        trailingContent={
                            IconButton(onClick={
                                store.deleteAnnotation(bookPath,item.id)
                                refresh++
                            }) {Icon(Icons.Default.DeleteOutline,"Qeydi sil")}
                        },
                        modifier=Modifier.clickable {
                            editingId=item.id
                            quote=item.quote
                            note=item.note
                            chapter=item.chapter
                        }
                    )
                    TextButton(onClick={onJump(item.chapter)}) {Text("Fəsili aç")}
                    HorizontalDivider()
                }
            }
        }
    }
}
