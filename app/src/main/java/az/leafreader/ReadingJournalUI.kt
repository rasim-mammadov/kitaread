package az.leafreader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

/** A unified, book-aware reading journal for legacy and new local notes. */
@Composable
fun ReadingJournalScreen(
    store:BookStore,
    onBack:()->Unit,
    onExportMarkdown:()->Unit,
    onExportJson:()->Unit
) {
    var revision by remember { mutableIntStateOf(0) }
    val books=remember(revision){store.records()}
    val entries=remember(books){ReadingJournal.entries(books)}
    val annotations=remember(books){ReadingJournal.annotations(books)}
    var selectedBook by remember {mutableStateOf("")}
    var draft by remember {mutableStateOf("")}
    var reviewDraft by remember(selectedBook,revision) {
        mutableStateOf(store.find(selectedBook)?.optString("personalReview").orEmpty())
    }
    var tab by remember {mutableStateOf("journal")}
    var query by remember {mutableStateOf("")}
    var deleteEntry by remember {mutableStateOf<JournalEntry?>(null)}
    val visible=books.filter { it.optString("title").contains(query,true) ||
        it.optString("author").contains(query,true) }
    val pathAllowed=selectedBook.isNotBlank() && store.find(selectedBook)!=null
    Scaffold(
        contentWindowInsets=WindowInsets.safeDrawing,
        containerColor=MaterialTheme.colorScheme.background,
        topBar={
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=12.dp,vertical=8.dp),
                verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick=onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,ui("Kitabxanaya qayıt"))
                }
                Text(ui("Oxu gündəliyi"),fontSize=22.sp,fontWeight=FontWeight.Bold,
                    modifier=Modifier.weight(1f))
            }
        }
    ) { insets->
        Column(Modifier.fillMaxSize().padding(insets).padding(horizontal=16.dp)) {
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick=onExportMarkdown,modifier=Modifier.weight(1f)) {
                    Text(ui("Markdown ixrac et"))
                }
                OutlinedButton(onClick=onExportJson,modifier=Modifier.weight(1f)) {
                    Text(ui("JSON ixrac et"))
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),
                singleLine=true,label={Text(ui("Kitab və müəllif axtar..."))})
            val titles=listOf("" to ui("Bütün kitablar")) + visible.map {it.optString("path") to it.optString("title")}
            var menu by remember {mutableStateOf(false)}
            Box {
                OutlinedButton(onClick={menu=true},modifier=Modifier.fillMaxWidth()) {
                    Text(titles.firstOrNull {it.first==selectedBook}?.second
                        ?:ui("Kitab seç"),maxLines=1)
                }
                DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
                    titles.forEach { (path,title)->
                        DropdownMenuItem(text={Text(title,maxLines=1)},onClick={
                            selectedBook=path;menu=false
                        })
                    }
                }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("journal" to "Gündəlik","notes" to "Sitatlar və qeydlər",
                    "reviews" to "Rəylər").forEach {(key,label)->
                    FilterChip(selected=tab==key,onClick={tab=key},label={Text(ui(label))})
                }
            }
            if(tab=="journal" && pathAllowed) {
                OutlinedTextField(draft,{draft=it},modifier=Modifier.fillMaxWidth(),
                    label={Text(ui("Bu günün oxu qeydi"))},minLines=2,maxLines=4)
                Button(onClick={
                    if(pathAllowed && draft.isNotBlank()) {
                        ReadingJournal.save(store,selectedBook,draft)
                        draft="";revision++
                    }
                },enabled=pathAllowed && draft.isNotBlank(),
                    modifier=Modifier.fillMaxWidth()) {Text(ui("Gündəliyə əlavə et"))}
            } else if(tab=="reviews" && pathAllowed) {
                OutlinedTextField(reviewDraft,{reviewDraft=it},modifier=Modifier.fillMaxWidth(),
                    label={Text(ui("Kitab haqqında şəxsi rəy"))},minLines=3,maxLines=7)
                Button(onClick={
                    if(pathAllowed) {
                        ReadingJournal.review(store,selectedBook,reviewDraft)
                        revision++
                    }
                },enabled=pathAllowed,modifier=Modifier.fillMaxWidth()) {
                    Text(ui("Rəyi saxla"))
                }
            }
            HorizontalDivider(Modifier.padding(vertical=8.dp))
            val shownEntries=entries.filter {it.bookPath==selectedBook || selectedBook.isBlank()}
            val shownNotes=annotations.filter {it.bookPath==selectedBook || selectedBook.isBlank()}
            LazyColumn(contentPadding=PaddingValues(bottom=32.dp),
                verticalArrangement=Arrangement.spacedBy(6.dp)) {
                if(tab=="journal") {
                    items(shownEntries,key={it.id}) {entry->
                        ListItem(headlineContent={Text(entry.date+" · "+entry.title)},
                            supportingContent={Text(entry.body)},
                            trailingContent={TextButton(onClick={deleteEntry=entry}) {
                                Text(ui("Sil"))
                            }})
                        HorizontalDivider()
                    }
                } else if(tab=="notes") {
                    items(shownNotes) {item->
                        ListItem(headlineContent={Text(item.quote.ifBlank {ui("Şəxsi qeyd")})},
                            supportingContent={Text(item.bookTitle+" · "+ui("Fəsil")+
                                " "+(item.chapter+1)+"\n"+item.note)})
                        HorizontalDivider()
                    }
                } else {
                    items(books.filter {it.optString("personalReview").isNotBlank() &&
                        (selectedBook.isBlank() || it.optString("path")==selectedBook)}) {book->
                        ListItem(headlineContent={Text(book.optString("title"))},
                            supportingContent={Text(book.optString("personalReview"))})
                    }
                }
            }
        }
    }
    deleteEntry?.let {entry->
        AlertDialog(onDismissRequest={deleteEntry=null},
            title={Text(ui("Gündəlik qeydi silinsin?"))},
            text={Text(entry.body.take(180))},
            confirmButton={TextButton(onClick={
                ReadingJournal.delete(store,entry.bookPath,entry.id)
                deleteEntry=null;revision++
            }) {Text(ui("Sil"))}},
            dismissButton={TextButton(onClick={deleteEntry=null}) {Text(ui("Ləğv et"))}})
    }
}
