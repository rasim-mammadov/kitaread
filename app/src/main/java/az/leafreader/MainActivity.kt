@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package az.leafreader

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import org.json.JSONObject
import android.view.MotionEvent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val Ink=Color(0xFF1C3332)
private val Accent=Color(0xFF267A71)
private val Cream=Color(0xFFF8F6F0)
private val Gray=Color(0xFF667571)
private val Space=Color(0xFFEEEFE9)

class MainActivity:ComponentActivity() {
    private lateinit var tracker: ReadingSessionTracker
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (::tracker.isInitialized &&
            (event.actionMasked == MotionEvent.ACTION_DOWN ||
             event.actionMasked == MotionEvent.ACTION_MOVE)) tracker.interaction()
        return super.dispatchTouchEvent(event)
    }
    override fun onPause() {
        if (::tracker.isInitialized) tracker.pause()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        if (::tracker.isInitialized) tracker.resume()
    }
    override fun onDestroy() {
        if (::tracker.isInitialized) tracker.end()
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility=0
        val store=BookStore(this)
        tracker=ReadingSessionTracker(store)
        @Suppress("DEPRECATION")
        val shared:Uri?=if(intent?.action==Intent.ACTION_SEND) {
            if(Build.VERSION.SDK_INT>=33)
                intent.getParcelableExtra(Intent.EXTRA_STREAM,Uri::class.java)
            else intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } else if(intent?.action==Intent.ACTION_VIEW) intent.data else null
        setContent { LeafApp(store,tracker,shared) }
    }
}

@Composable
private fun LeafApp(store:BookStore,tracker:ReadingSessionTracker,incomingBook:Uri?=null) {
    val context=LocalContext.current
    var books by remember {mutableStateOf(store.books())}
    var selectedPath by rememberSaveable {mutableStateOf<String?>(null)}
    val selected=books.firstOrNull {it.path==selectedPath}
    var search by remember {mutableStateOf("")}
    var filter by remember {mutableStateOf("Hamısı")}
    var menuBook by remember {mutableStateOf<ReaderBook?>(null)}
    var detailsBook by remember {mutableStateOf<ReaderBook?>(null)}
    var showGlobalBookmarks by remember {mutableStateOf(false)}
    var openingMark by remember {mutableStateOf<BookStore.BookMark?>(null)}
    var genreFilter by remember {mutableStateOf(GenreCatalog.UNKNOWN)}
    var collectionFilter by remember {mutableStateOf("")}
    var libraryVersion by remember {mutableIntStateOf(0)}
    fun refreshLibrary() {
        books=store.books()
        libraryVersion++
    }
    var showDashboard by remember {mutableStateOf(false)}
    var showJournal by remember {mutableStateOf(false)}
    var showSettingsHome by remember {mutableStateOf(false)}
    var showAdvancedFilters by remember {mutableStateOf(false)}
    var showSearch by remember {mutableStateOf(false)}
    var layoutMode by remember {
        mutableStateOf(store.setting().getString("libraryLayout","list") ?: "list")
    }
    var groupMode by remember {
        mutableStateOf(store.setting().getString("libraryGroup","none") ?: "none")
    }
    var showGroupMenu by remember {mutableStateOf(false)}
    var selecting by remember {mutableStateOf(false)}
    var selectedBooks by remember {mutableStateOf<Set<String>>(emptySet())}
    var showBulkHideConfirm by remember {mutableStateOf(false)}
    var showBulkCollection by remember {mutableStateOf(false)}
    var bulkCollectionName by remember {mutableStateOf("")}
    var showDuplicates by remember {mutableStateOf(false)}
    var duplicateGroups by remember {mutableStateOf<List<List<String>>>(emptyList())}
    var duplicateScanError by remember {mutableStateOf<String?>(null)}
    var duplicatesScanning by remember {mutableStateOf(false)}
    fun toggleSelection(path:String) {
        selectedBooks=if(path in selectedBooks)selectedBooks-path else selectedBooks+path
    }
    fun stopSelection() {
        selectedBooks=emptySet()
        selecting=false
    }
    var showSortMenu by remember {mutableStateOf(false)}
    var showExitConfirmation by remember {mutableStateOf(false)}
    var deleteBook by remember {mutableStateOf<ReaderBook?>(null)}
    var deleteMode by remember {mutableStateOf("hide")}
    var formatFilter by remember {mutableStateOf("Hamısı")}
    var sortMode by remember {
        mutableStateOf(store.setting().getString("librarySort","Son oxunanlar") ?: "Son oxunanlar")
    }
    var activeReaderTheme by remember {mutableStateOf(store.setting().getString("theme","Sepia") ?: "Sepia")}
    var language by remember {
        mutableStateOf(store.setting().getString("language","az") ?: "az")
    }
    var appTheme by remember {
        mutableStateOf(store.setting().getString("theme","Sepia") ?: "Sepia")
    }
    fun updateAppTheme(next:String) {
        appTheme=next
        store.setting().edit().putString("theme",next).apply()
    }
    val appColors=LibraryPalette.scheme(if(selected!=null)activeReaderTheme else appTheme)
    LaunchedEffect(selectedPath) {
        if(selectedPath!=null) {
            activeReaderTheme=if(store.setting().getBoolean("readerFollowsApp",true))
                store.setting().getString("theme","Sepia") ?: "Sepia"
            else store.setting().getString("readerTheme","Sepia") ?: "Sepia"
        }
    }
    SideEffect {
        @Suppress("DEPRECATION")
        (context as? ComponentActivity)?.window?.decorView?.systemUiVisibility=
            if(appTheme in setOf("Dark","AMOLED","Midnight","Graphite"))0
            else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    BackHandler(enabled=selected==null) {
        when {
            showExitConfirmation ->showExitConfirmation=false
            showSettingsHome ->{showSettingsHome=false;refreshLibrary()}
            showBulkCollection ->showBulkCollection=false
            showBulkHideConfirm ->showBulkHideConfirm=false
            showDuplicates ->showDuplicates=false
            selecting ->stopSelection()
            showAdvancedFilters ->showAdvancedFilters=false
            showGlobalBookmarks ->showGlobalBookmarks=false
            showDashboard ->showDashboard=false
            showJournal ->showJournal=false
            showSearch ->{showSearch=false;search=""}
            else ->showExitConfirmation=true
        }
    }
    var showThemeGallery by remember {mutableStateOf(false)}
    LaunchedEffect(showDuplicates,libraryVersion) {
        if(showDuplicates) {
            duplicatesScanning=true
            duplicateScanError=null
            try {
                duplicateGroups=withContext(Dispatchers.IO) {
                    SmartLibrary.exactDuplicates(context.filesDir,store.books())
                }
            } catch(e:Exception) {
                duplicateScanError=e.message ?: "Dublikatlar yoxlanılmadı"
                duplicateGroups=emptyList()
            } finally {duplicatesScanning=false}
        }
    }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {store.enrichLegacyEpub()}
        refreshLibrary()
    }
    // Some Android file managers report EPUB as application/zip or application/octet-stream.
    // Show all files here; BookStore validates the actual book extension before import.
    LaunchedEffect(incomingBook) {
        if(incomingBook!=null)try {
            val imported=withContext(Dispatchers.IO){store.import(incomingBook)}
            refreshLibrary()
            selectedPath=imported
        }catch(e:Exception) {
            Toast.makeText(context,UiLanguage.translate(e.message ?: "Kitab açıla bilmədi",store.setting().getString("language","az") ?: "az"),Toast.LENGTH_LONG).show()
        }
    }
    val filePicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri->
        if(uri!=null) try {
            store.import(uri)
            books=store.books()
        }catch(e:Exception){Toast.makeText(context,UiLanguage.translate(e.message ?: "Fayl açılmadı",store.setting().getString("language","az") ?: "az"),Toast.LENGTH_LONG).show()}
    }
    val jsonExport=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {uri->
        if(uri!=null)try {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(store.exportMetadata().toByteArray(Charsets.UTF_8))
            } ?:error("Yazma icazəsi yoxdur")
            Toast.makeText(context,UiLanguage.translate("JSON metadata backup saxlanıldı",store.setting().getString("language","az") ?: "az"),Toast.LENGTH_LONG).show()
        }catch(e:Exception){Toast.makeText(context,UiLanguage.translate(e.message ?: "Backup xətası",store.setting().getString("language","az") ?: "az"),Toast.LENGTH_LONG).show()}
    }
    val jsonImport=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri->
        if(uri!=null)try {
            val data=context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {it.readText()}
                ?:error("Backup faylı oxunmadı")
            val report=store.restoreMetadata(data)
            books=store.books()
            Toast.makeText(context,UiLanguage.translate("Bərpa: ${report.imported} kitab. ${report.missingFiles} kitabın faylı cihazda yoxdur.",
                    store.setting().getString("language","az") ?: "az"),Toast.LENGTH_LONG).show()
        }catch(e:Exception){Toast.makeText(context,UiLanguage.translate(e.message ?: "Import xətası",
                    store.setting().getString("language","az") ?: "az"),Toast.LENGTH_LONG).show()}
    }
    val journalMarkdownExport=rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")) {uri->
        if(uri!=null)try {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(ReadingJournal.exportMarkdown(store.records()).toByteArray(Charsets.UTF_8))
            } ?:error("Yazma icazəsi yoxdur")
        }catch(e:Exception){
            Toast.makeText(context,e.message ?: "Export error",Toast.LENGTH_LONG).show()
        }
    }
    val journalJsonExport=rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")) {uri->
        if(uri!=null)try {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(ReadingJournal.exportJson(store.records()).toByteArray(Charsets.UTF_8))
            } ?:error("Yazma icazəsi yoxdur")
        }catch(e:Exception){
            Toast.makeText(context,e.message ?: "Export error",Toast.LENGTH_LONG).show()
        }
    }
    CompositionLocalProvider(LocalUiLanguage provides language) {
    MaterialTheme(colorScheme=appColors,
        shapes=Shapes(medium=RoundedCornerShape(16.dp),large=RoundedCornerShape(24.dp))) {
        if(selected==null && showDashboard) {
            ReadingDashboard(store,onBack={showDashboard=false})
        } else if(selected==null && showJournal) {
            ReadingJournalScreen(store,onBack={showJournal=false;refreshLibrary()},
                onExportMarkdown={journalMarkdownExport.launch("leaf-reader-journal.md")},
                onExportJson={journalJsonExport.launch("leaf-reader-knowledge.json")})
        } else if(selected==null) {
            Scaffold(
                contentWindowInsets=WindowInsets.safeDrawing,
                containerColor=MaterialTheme.colorScheme.background,
                topBar={
                    Column(Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(start=18.dp,end=18.dp,top=10.dp,bottom=4.dp),
                        verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            Text(ui("Leaf Reader"),fontSize=29.sp,fontWeight=FontWeight.Bold,
                                color=MaterialTheme.colorScheme.onBackground,
                                modifier=Modifier.weight(1f))
                            IconButton(onClick={showSearch=!showSearch}) {
                                Icon(Icons.Default.Search,ui("Kitab axtar"))
                            }
                            IconButton(onClick={showSettingsHome=true}) {
                                Icon(Icons.Default.Settings,ui("Settings"))
                            }
                        }
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            IconButton(onClick={
                                layoutMode=if(layoutMode=="list")"grid" else "list"
                                store.setting().edit().putString("libraryLayout",layoutMode).apply()
                            }) {
                                Icon(if(layoutMode=="list")Icons.Default.GridView
                                    else Icons.Default.ViewList,ui("Görünüşü dəyiş"))
                            }
                            Box {
                                TextButton(onClick={showGroupMenu=true}) {
                                    Icon(Icons.Default.AccountTree,null,Modifier.size(17.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(ui(when(groupMode) {
                                        "author"->"Müəllif üzrə"
                                        "series"->"Seriya üzrə"
                                        else->"Qruplaşdırma yoxdur"
                                    }),fontSize=12.sp)
                                }
                                DropdownMenu(expanded=showGroupMenu,onDismissRequest={showGroupMenu=false}) {
                                    listOf("none" to "Qruplaşdırma yoxdur",
                                        "author" to "Müəllif üzrə",
                                        "series" to "Seriya üzrə").forEach { (value,label)->
                                        DropdownMenuItem(text={Text(ui(label))},onClick={
                                            groupMode=value
                                            store.setting().edit().putString("libraryGroup",value).apply()
                                            showGroupMenu=false
                                        })
                                    }
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick={
                                if(selecting) stopSelection() else selecting=true
                            }) {Text(ui(if(selecting)"Ləğv et" else "Seç"))}
                            IconButton(onClick={showDuplicates=true}) {
                                Icon(Icons.Default.ContentCopy,ui("Dublikatlar"))
                            }
                        }
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick={showDashboard=true},
                                modifier=Modifier.weight(1f),
                                contentPadding=PaddingValues(horizontal=5.dp,vertical=6.dp)) {
                                Icon(Icons.Default.BarChart,null,Modifier.size(17.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(ui("Statistika"),fontSize=11.sp)
                            }
                            OutlinedButton(onClick={showJournal=true},
                                modifier=Modifier.weight(1f),
                                contentPadding=PaddingValues(horizontal=5.dp,vertical=6.dp)) {
                                Icon(Icons.Default.EditNote,null,Modifier.size(17.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(ui("Gündəlik"),fontSize=11.sp)
                            }
                            OutlinedButton(onClick={showGlobalBookmarks=true},
                                modifier=Modifier.weight(1f),
                                contentPadding=PaddingValues(horizontal=5.dp,vertical=6.dp)) {
                                Icon(Icons.Default.Bookmarks,null,Modifier.size(17.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(ui("Əlfəcinlər"),fontSize=11.sp)
                            }
                            OutlinedButton(onClick={showAdvancedFilters=true},
                                modifier=Modifier.weight(1f),
                                contentPadding=PaddingValues(horizontal=5.dp,vertical=6.dp)) {
                                Icon(Icons.Default.Tune,null,Modifier.size(17.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(ui("Filtrlər"),fontSize=11.sp)
                            }
                        }
                    }
                },
                floatingActionButton={
                    FloatingActionButton(onClick={
                        filePicker.launch(arrayOf("*/*"))
                    }) {
                        Icon(Icons.Default.LibraryAdd,ui("Kitab əlavə et"))
                    }
                }
            ){ inner->
                Column(Modifier.fillMaxSize().padding(inner).padding(horizontal=18.dp)) {
                    if(showSearch) {
                        OutlinedTextField(
                            search,{search=it},modifier=Modifier.fillMaxWidth(),
                            singleLine=true,shape=RoundedCornerShape(18.dp),
                            placeholder={Text(ui("Kitab, müəllif, seriya, janr, kolleksiya axtar..."))},
                            leadingIcon={Icon(Icons.Default.Search,null)},
                            trailingIcon={
                                IconButton(onClick={search="";showSearch=false}) {
                                    Icon(Icons.Default.Close,ui("Axtarışı bağla"))
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement=Arrangement.spacedBy(7.dp),
                        modifier=Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("Hamısı","Oxunur","Oxunmayıb","Bitirilib").forEach { label->
                            FilterChip(selected=filter==label,onClick={filter=label},label={Text(ui(label))})
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text(ui("Kitabxanam"),color=MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier=Modifier.weight(1f),fontSize=13.sp)
                        Box {
                            TextButton(onClick={showSortMenu=true}) {
                                Icon(Icons.Default.Sort,null,Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(ui(sortMode),fontSize=12.sp)
                            }
                            DropdownMenu(expanded=showSortMenu,
                                onDismissRequest={showSortMenu=false}) {
                                listOf("Son oxunanlar","Əlavə edilmə tarixi",
                                    "A–Z","Z–A","Progress").forEach {option->
                                    DropdownMenuItem(text={Text(ui(option))},onClick={
                                        sortMode=option
                                        store.setting().edit().putString("librarySort",option).apply()
                                        showSortMenu=false
                                    })
                                }
                            }
                        }
                    }
                    val allCollections=remember(libraryVersion) {store.collections()}
                    val recordsByPath=remember(books,libraryVersion) {
                        store.records().associateBy {it.optString("path")}
                    }
                    Spacer(Modifier.height(9.dp))
                    val visible=books.filter { book->
                        val info=recordsByPath[book.path]
                        val genre=info?.optString("genre",GenreCatalog.UNKNOWN)
                        val collections=info?.optJSONArray("collections") ?: org.json.JSONArray()
                        !((info?.optBoolean("hidden",false)) ?:false) &&
                        (filter=="Hamısı" || book.status==filter) &&
                        (formatFilter=="Hamısı" ||
                            (formatFilter=="MD" && book.type in setOf("md","markdown")) ||
                            book.type.equals(formatFilter,true)) &&
                            SmartLibrary.matches(book,info,search) &&
                            (genreFilter==GenreCatalog.UNKNOWN || genre==genreFilter) &&
                            (collectionFilter.isEmpty() ||
                                (0 until collections.length()).any {collections.optString(it)==collectionFilter})
                    }
                    val ordered=store.sortBooks(visible,sortMode)
                    val recent=if(!selecting && groupMode=="none" && filter=="Hamısı" && search.isBlank())
                        ordered.firstOrNull {book->
                            book.status=="Oxunur" &&
                            (recordsByPath[book.path]?.optLong("lastReadTimestamp",0L) ?:0L)>0L
                        } else null
                    if(recent!=null) {
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable {openingMark=null;selectedPath=recent.path}
                                .padding(vertical=8.dp,horizontal=4.dp),
                            verticalAlignment=Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow,null,
                                tint=MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(ui("Oxumağa davam et"),
                                    fontSize=12.sp,color=MaterialTheme.colorScheme.primary)
                                Text(recent.title,maxLines=1,
                                    overflow=TextOverflow.Ellipsis,
                                    color=MaterialTheme.colorScheme.onBackground,
                                    fontWeight=FontWeight.SemiBold)
                            }
                            Text(ui("${recent.progress}%"),fontSize=12.sp,
                                color=MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    val grouped=if(groupMode=="none") listOf("" to ordered)
                        else ordered.groupBy { book->
                            SmartLibrary.groupName(book,recordsByPath[book.path],groupMode)
                        }.toSortedMap(String.CASE_INSENSITIVE_ORDER)
                            .toList()
                    if(selecting) {
                        Surface(color=MaterialTheme.colorScheme.surface,
                            shape=RoundedCornerShape(14.dp)) {
                            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                                Row(verticalAlignment=Alignment.CenterVertically) {
                                    Text(ui("Seçilmiş kitablar")+": ${selectedBooks.size}",
                                        modifier=Modifier.weight(1f),fontSize=13.sp)
                                    TextButton(onClick={
                                        selectedBooks=ordered.map {it.path}.toSet()
                                    }) {Text(ui("Hamısını seç"))}
                                }
                                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(onClick={showBulkCollection=true},
                                        enabled=selectedBooks.isNotEmpty(),modifier=Modifier.weight(1f),
                                        contentPadding=PaddingValues(horizontal=5.dp)) {
                                        Text(ui("Kolleksiyaya əlavə et"),fontSize=12.sp)
                                    }
                                    OutlinedButton(onClick={showBulkHideConfirm=true},
                                        enabled=selectedBooks.isNotEmpty(),modifier=Modifier.weight(1f),
                                        contentPadding=PaddingValues(horizontal=5.dp)) {
                                        Text(ui("Gizlət"),fontSize=12.sp)
                                    }
                                }
                            }
                        }
                    }
                    if(ordered.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(bottom=60.dp),contentAlignment=Alignment.Center) {
                            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AutoStories,null,Modifier.size(64.dp),tint=Accent)
                                Spacer(Modifier.height(16.dp))
                                Text(ui("Burada hələ kitab yoxdur"),fontSize=20.sp,fontWeight=FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Text(ui(if(books.isEmpty())"EPUB, PDF, CBZ və Markdown əlavə et"
                                    else "Axtarış və filtrlərə uyğun kitab tapılmadı"),
                                    color=MaterialTheme.colorScheme.onSurfaceVariant)
                                if(books.isNotEmpty()) {
                                    TextButton(onClick={
                                        search="";filter="Hamısı"
                                        genreFilter=GenreCatalog.UNKNOWN
                                        collectionFilter="";formatFilter="Hamısı"
                                    }) {Text(ui("Filtrləri sıfırla"))}
                                }
                            }
                        }
                    } else if(layoutMode=="grid") {
                        LazyVerticalGrid(columns=GridCells.Fixed(2),
                            contentPadding=PaddingValues(bottom=96.dp),
                            horizontalArrangement=Arrangement.spacedBy(10.dp),
                            verticalArrangement=Arrangement.spacedBy(10.dp)) {
                            grouped.forEach { (heading,groupBooks)->
                                if(groupMode!="none") item(span={GridItemSpan(maxLineSpan)}) {
                                    Text(heading,fontWeight=FontWeight.Bold,fontSize=16.sp,
                                        modifier=Modifier.padding(top=10.dp,bottom=2.dp))
                                }
                                gridItems(groupBooks,key={it.path}) { book->
                                    SmartGridBook(book,store,
                                        selected=book.path in selectedBooks,selecting=selecting,
                                        onOpen={openingMark=null;selectedPath=book.path},
                                        onMore={menuBook=book},
                                        onSelect={toggleSelection(book.path)})
                                }
                            }
                        }
                    } else LazyColumn(contentPadding=PaddingValues(bottom=96.dp),
                        verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        grouped.forEach { (heading,groupBooks)->
                            if(groupMode!="none") item {
                                Text(heading,fontWeight=FontWeight.Bold,fontSize=16.sp,
                                    modifier=Modifier.padding(top=10.dp,bottom=2.dp))
                            }
                            items(groupBooks,key={it.path}) { book->
                                Row(verticalAlignment=Alignment.CenterVertically) {
                                    if(selecting) Checkbox(
                                        checked=book.path in selectedBooks,
                                        onCheckedChange={toggleSelection(book.path)})
                                    Box(Modifier.weight(1f)) {
                                        LibraryCard(book,store,onOpen={
                                            if(selecting)toggleSelection(book.path)
                                            else {openingMark=null;selectedPath=book.path}
                                        },onMore={
                                            if(selecting)toggleSelection(book.path)
                                            else menuBook=book
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val book=selected!!
            ReaderScreen(book,store,tracker,openingMark,onThemeChange={
                activeReaderTheme=it
            },onExit={
                selectedPath=null
                openingMark=null
                refreshLibrary()
            })
        }
        if(showBulkCollection) {
            AlertDialog(
                onDismissRequest={showBulkCollection=false},
                title={Text(ui("Kolleksiyaya əlavə et"))},
                text={
                    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(ui("Seçilmiş kitablar")+": ${selectedBooks.size}")
                        OutlinedTextField(value=bulkCollectionName,onValueChange={bulkCollectionName=it},
                            label={Text(ui("Kolleksiya adı"))},singleLine=true,
                            modifier=Modifier.fillMaxWidth())
                        val collectionOptions=store.collections()
                        if(collectionOptions.isNotEmpty()) {
                            Text(ui("Mövcud kolleksiyalar"),fontSize=12.sp)
                            collectionOptions.take(8).forEach {name->
                                TextButton(onClick={bulkCollectionName=name}) {Text(name)}
                            }
                        }
                    }
                },
                confirmButton={
                    TextButton(enabled=selectedBooks.isNotEmpty() && bulkCollectionName.trim().isNotBlank(),
                        onClick={
                            store.bulkAddToCollection(selectedBooks,bulkCollectionName)
                            showBulkCollection=false
                            bulkCollectionName=""
                            stopSelection()
                            refreshLibrary()
                        }) {Text(ui("Əlavə et"))}
                },
                dismissButton={TextButton(onClick={showBulkCollection=false}) {Text(ui("Ləğv et"))}}
            )
        }
        if(showBulkHideConfirm) {
            AlertDialog(
                onDismissRequest={showBulkHideConfirm=false},
                title={Text(ui("Kitabları gizlət?"))},
                text={Text(ui("Seçilmiş kitablar")+": ${selectedBooks.size}. "+
                    ui("Fayllar silinmir. Kitabları Parametrlərdən bərpa etmək olar."))},
                confirmButton={TextButton(onClick={
                    store.bulkHide(selectedBooks)
                    showBulkHideConfirm=false
                    stopSelection()
                    refreshLibrary()
                }) {Text(ui("Gizlət"))}},
                dismissButton={TextButton(onClick={showBulkHideConfirm=false}) {Text(ui("Ləğv et"))}}
            )
        }
        if(showDuplicates) {
            AlertDialog(
                onDismissRequest={showDuplicates=false},
                title={Text(ui("Dublikat kitablar"))},
                text={
                    Column(Modifier.heightIn(max=430.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        Text(ui("Yalnız eyni fayl məzmunu yoxlanılır."),
                            fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        if(duplicatesScanning) CircularProgressIndicator()
                        else if(duplicateScanError!=null) Text(duplicateScanError.orEmpty())
                        else if(duplicateGroups.isEmpty()) Text(ui("Dublikat tapılmadı"))
                        else duplicateGroups.forEachIndexed {index,paths->
                            Text("${index+1}. "+paths.size+" "+ui("nüsxə"),
                                fontWeight=FontWeight.SemiBold)
                            paths.forEach {path->
                                val match=books.firstOrNull {it.path==path}
                                Text("• "+(match?.title ?:path),
                                    maxLines=2,overflow=TextOverflow.Ellipsis)
                            }
                            HorizontalDivider()
                        }
                    }
                },
                confirmButton={TextButton(onClick={showDuplicates=false}) {Text(ui("Bağla"))}}
            )
        }
        if(showSettingsHome) {
            LibrarySettingsSheet(
                store=store,appTheme=appTheme,onTheme={updateAppTheme(it)},
                language=language,onLanguage={next->
                    language=next
                    store.setting().edit().putString("language",next).apply()
                },
                onExport={jsonExport.launch("leaf-reader-metadata.json")},
                onImport={jsonImport.launch(arrayOf("application/json","text/plain"))},
                onRestore={path->
                    store.hideBook(path,false)
                    refreshLibrary()
                },
                onSort={sortMode=it},
                onClose={
                    showSettingsHome=false
                    sortMode=store.setting().getString("librarySort","Son oxunanlar")
                        ?: "Son oxunanlar"
                    refreshLibrary()
                }
            )
        }
        if(showAdvancedFilters) {
            AdvancedFiltersSheet(
                genres=books.mapNotNull {store.find(it.path)?.optString("genre")}
                    .filter {it.isNotBlank()}.distinct().sorted(),
                collections=store.collections(),
                genre=genreFilter,format=formatFilter,collection=collectionFilter,
                onGenre={genreFilter=it},onFormat={formatFilter=it},
                onCollection={collectionFilter=it},
                onClose={showAdvancedFilters=false}
            )
        }
        if(showExitConfirmation) {
            AlertDialog(
                onDismissRequest={showExitConfirmation=false},
                title={Text(ui("Leaf Reader-dən çıxmaq istəyirsən?"))},
                text={Text(ui("Kitabxanan və oxuma mövqeyin saxlanılacaq."))},
                confirmButton={TextButton(onClick={
                    showExitConfirmation=false
                    (context as? ComponentActivity)?.finish()
                }) {Text(ui("Çıx"))}},
                dismissButton={TextButton(onClick={showExitConfirmation=false}) {Text(ui("Ləğv et"))}}
            )
        }
        detailsBook?.let {book->
            BookDetailsSheet(book,store,
                onClose={detailsBook=null},
                onChanged={refreshLibrary()})
        }
        if(showGlobalBookmarks) {
            BookmarksSheet(store,onOpen={path,mark->
                val target=store.books().firstOrNull {it.path==path}
                if(target!=null) {
                    openingMark=mark
                    selectedPath=target.path
                    showGlobalBookmarks=false
                }
            },onClose={showGlobalBookmarks=false},
                onChanged={refreshLibrary()})
        }
        menuBook?.let {book->
            AlertDialog(
                onDismissRequest={menuBook=null},
                title={Text(book.title,maxLines=2)},
                text={
                    Column {
                        TextButton(onClick={
                            detailsBook=book
                            menuBook=null
                        },modifier=Modifier.fillMaxWidth()) {
                            Text(ui("Kitab detalları və status"))
                        }
                        TextButton(onClick={
                            store.hideBook(book.path,true)
                            refreshLibrary()
                            menuBook=null
                        },modifier=Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.VisibilityOff,null)
                            Spacer(Modifier.width(8.dp))
                            Text(ui("Kitabxanadan gizlət"))
                        }
                        TextButton(onClick={
                            deleteMode="remove";deleteBook=book;menuBook=null
                        },modifier=Modifier.fillMaxWidth()) {
                            Text(ui("Kitabxanadan çıxar (fayl qalsın)"))
                        }
                        TextButton(onClick={
                            deleteMode="delete";deleteBook=book;menuBook=null
                        },modifier=Modifier.fillMaxWidth()) {
                            Text(ui("Kitab və məlumatlarını tamamilə sil"))
                        }
                    }
                },
                confirmButton={TextButton(onClick={menuBook=null}) {Text(ui("Bağla"))}}
            )
        }
        deleteBook?.let {book->
            AlertDialog(
                onDismissRequest={deleteBook=null},
                title={Text(ui(if(deleteMode=="delete")"Kitab həmişəlik silinsin?"
                    else "Kitabxanadan çıxarılsın?"))},
                text={Text(ui(if(deleteMode=="delete")
                    "Kitab faylı, əlfəcinlər, oxuma tarixçəsi və qeydləri silinəcək. "+
                        "Bu əməliyyat geri qaytarıla bilməz."
                    else "Kitab qeydini kitabxanadan çıxaracağıq. Orijinal fayl "+
                        "tətbiqin saxlamasında qalacaq, amma siyahıda görünməyəcək."))},
                confirmButton={TextButton(onClick={
                    try {
                        store.removeBook(book.path,deleteMode=="delete")
                        refreshLibrary()
                        deleteBook=null
                    }catch(e:Exception) {
                        Toast.makeText(context,UiLanguage.translate(e.message ?: "Kitab silinmədi",
                            store.setting().getString("language","az") ?: "az"),Toast.LENGTH_LONG).show()
                    }
                }) {Text(ui(if(deleteMode=="delete")"Həmişəlik sil" else "Çıxar"))}},
                dismissButton={TextButton(onClick={deleteBook=null}) {Text(ui("Ləğv et"))}}
            )
        }
    }
    }
}

@Composable
private fun LibraryCard(book:ReaderBook,store:BookStore,onOpen:()->Unit,onMore:()->Unit) {
    val context=LocalContext.current
    val image=remember(book.cover) {
        if(book.cover.isNotBlank())BitmapFactory.decodeFile(File(context.filesDir,book.cover).absolutePath)
        else null
    }
    Card(onClick=onOpen,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),
        colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),
        elevation=CardDefaults.cardElevation(defaultElevation=1.dp)) {
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(width=78.dp,height=108.dp).clip(RoundedCornerShape(10.dp))
                .background(Accent),contentAlignment=Alignment.Center) {
                if(image!=null)Image(image.asImageBitmap(),null,Modifier.fillMaxSize(),
                    contentScale=ContentScale.Crop)
                else Icon(Icons.Default.MenuBook,null,Modifier.size(44.dp),
                    tint=Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(book.title,fontWeight=FontWeight.SemiBold,fontSize=17.sp,
                    color=MaterialTheme.colorScheme.onSurface,
                    maxLines=2,overflow=TextOverflow.Ellipsis)
                val metadata=store.find(book.path)
                val author=metadata?.optString("author").orEmpty()
                val genre=metadata?.optString("genre",GenreCatalog.UNKNOWN).orEmpty()
                if(author.isNotBlank()) Text(author,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp,
                    maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(ui(book.status+"  •  "+book.type.uppercase()+
                    (if(genre.isNotBlank() && genre!=GenreCatalog.UNKNOWN)"  •  $genre" else "")),
                    color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp,maxLines=1,
                    overflow=TextOverflow.Ellipsis)
                LinearProgressIndicator(progress={book.progress/100f},
                    modifier=Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(8.dp)),
                    color=MaterialTheme.colorScheme.primary,
                    trackColor=MaterialTheme.colorScheme.surfaceVariant)
                Text(ui(book.progress.toString()+"% oxunub"),fontSize=12.sp,
                    color=MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick=onMore) {Icon(Icons.Default.MoreVert,ui("Kitab ayarları"))}
        }
    }
}

@Composable
private fun ReaderScreen(
    book:ReaderBook,store:BookStore,tracker:ReadingSessionTracker,
    initialBookmark:BookStore.BookMark?=null,
    onThemeChange:(String)->Unit={},onExit:()->Unit
) {
    val isTextBook=book.type in setOf("epub","md","markdown")
    BackHandler(onBack=onExit)
    val context=LocalContext.current
    val prefs=remember {store.setting()}
    val recordOptions=remember(book.path) {store.find(book.path)?.optJSONObject("readerOptions")}
    var perBookSettings by remember(book.path) {
        mutableStateOf(recordOptions?.optBoolean("enabled",false) ?:false)
    }
    fun optionInt(key:String, fallback:Int):Int =
        if(perBookSettings) recordOptions?.optInt(key,fallback) ?:fallback else fallback
    fun optionString(key:String, fallback:String):String =
        if(perBookSettings) recordOptions?.optString(key,fallback) ?:fallback else fallback
    fun optionBoolean(key:String, fallback:Boolean):Boolean =
        if(perBookSettings) recordOptions?.optBoolean(key,fallback) ?:fallback else fallback
    fun saveOption(key:String,value:Any) {
        if(perBookSettings) {
            store.change(book.path) {entry->
                val opts=entry.optJSONObject("readerOptions") ?:JSONObject()
                opts.put("enabled",true)
                opts.put(key,value)
                entry.put("readerOptions",opts)
            }
            recordOptions?.put(key,value)
        } else {
            val edit=prefs.edit()
            when(value) {
                is Boolean->edit.putBoolean(key,value)
                is Int->edit.putInt(key,value)
                is String->edit.putString(key,value)
            }
            edit.apply()
        }
    }
    var readerFollowsApp by remember {
        mutableStateOf(prefs.getBoolean("readerFollowsApp",true))
    }
    var theme by remember {
        mutableStateOf(if(perBookSettings)
            optionString("readerTheme",prefs.getString("theme","Sepia") ?: "Sepia")
        else if(readerFollowsApp) prefs.getString("theme","Sepia") ?: "Sepia"
        else prefs.getString("readerTheme","Sepia") ?: "Sepia")
    }
    var rotationMode by remember {
        mutableStateOf(optionString("screenOrientation",prefs.getString("screenOrientation","Auto") ?: "Auto"))
    }
    var focusMode by rememberSaveable(book.path) {mutableStateOf(false)}
    var brightness by remember {mutableFloatStateOf(prefs.getFloat("readerBrightness",-1f))}
    val activity=context as? ComponentActivity
    DisposableEffect(activity,focusMode) {
        val window=activity?.window
        if(focusMode && window!=null) {
            val controller=WindowCompat.getInsetsController(window,window.decorView)
            controller.systemBarsBehavior=
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if(window!=null)WindowCompat.getInsetsController(
                window,window.decorView).show(WindowInsetsCompat.Type.systemBars())
        }
    }
    DisposableEffect(activity,brightness) {
        val window=activity?.window
        val previous=window?.attributes?.screenBrightness ?: -1f
        if(window!=null) {
            val attrs=window.attributes
            attrs.screenBrightness=if(brightness<0f) -1f else brightness.coerceIn(0.05f,1f)
            window.attributes=attrs
        }
        onDispose {
            if(window!=null) {
                val attrs=window.attributes
                attrs.screenBrightness=previous
                window.attributes=attrs
            }
        }
    }
    DisposableEffect(activity,rotationMode) {
        activity?.requestedOrientation=when(rotationMode) {
            "Portrait"->ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "Landscape"->ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else->ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            if(activity!=null)
                activity.requestedOrientation=ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    val landscape=LocalConfiguration.current.orientation==Configuration.ORIENTATION_LANDSCAPE
    var font by remember {mutableFloatStateOf(optionInt("fontSize",prefs.getInt("fontSize",20)).toFloat())}
    var line by remember {mutableFloatStateOf(optionInt("lineSpacing",prefs.getInt("lineSpacing",160)).toFloat())}
    var alignment by remember {
        mutableStateOf(optionString("textAlignment",prefs.getString("textAlignment","left") ?: "left"))
    }
    var mode by remember {mutableStateOf(optionString("epubMode",prefs.getString("epubMode","scroll") ?: "scroll"))}
    var swipe by remember {mutableStateOf(optionBoolean("swipeTurn",prefs.getBoolean("swipeTurn",true)))}
    var arrows by remember {mutableStateOf(optionBoolean("sideButtons",prefs.getBoolean("sideButtons",false)))}
    var fitWidth by remember {mutableStateOf(optionBoolean("fitWidth",prefs.getBoolean("fitWidth",false)))}
    var chrome by remember {mutableStateOf(false)}
    var smoothTurns by remember {
        mutableStateOf(optionBoolean("smoothPageTurns",
            prefs.getBoolean("smoothPageTurns",true)))
    }
    var pageAnimator by remember(book.path) {mutableStateOf<ValueAnimator?>(null)}
    val lifecycleOwner=LocalLifecycleOwner.current
    val readingClock=remember(book.path) {ReadingClock()}
    var elapsedSeconds by remember(book.path) {mutableLongStateOf(0L)}
    DisposableEffect(lifecycleOwner,readingClock) {
        val observer=LifecycleEventObserver { _, event->
            when(event) {
                Lifecycle.Event.ON_RESUME->readingClock.resume(SystemClock.elapsedRealtime())
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP->
                    readingClock.pause(SystemClock.elapsedRealtime())
                else->{}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
            readingClock.resume(SystemClock.elapsedRealtime())
        onDispose {
            readingClock.pause(SystemClock.elapsedRealtime())
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(readingClock) {
        while(true) {
            elapsedSeconds=readingClock.elapsedSeconds(SystemClock.elapsedRealtime())
            delay(1000L)
        }
    }
    var showSettings by remember {mutableStateOf(false)}
    var showChapters by remember {mutableStateOf(false)}
    var showReaderBookmarks by remember {mutableStateOf(false)}
    var showChapterSearch by remember {mutableStateOf(false)}
    var showReadingNotes by remember {mutableStateOf(false)}
    var draftQuote by remember {mutableStateOf("")}
    var draftChapter by remember {mutableIntStateOf(-1)}
    var pendingSearch by remember {mutableStateOf("")}
    var bookmarkRefresh by remember {mutableIntStateOf(0)}
    var readerBookmarkJump by remember {mutableIntStateOf(0)}
    var chapter by rememberSaveable(book.path) {
        mutableIntStateOf(initialBookmark?.chapter ?: book.chapter)
    }
    var liveProgress by remember(book.path) {mutableIntStateOf(book.progress)}
    var fraction by rememberSaveable(book.path) {
        mutableDoubleStateOf(initialBookmark?.fraction ?: book.fraction)
    }
    var chapterFiles by remember(book.path) {mutableStateOf<List<File>>(emptyList())}
    var chapterLabels by remember(book.path) {mutableStateOf<List<String>>(emptyList())}
    var loadError by remember {mutableStateOf<String?>(null)}
    var pageCount by remember {mutableIntStateOf(1)}
    var chapterPages by remember {mutableIntStateOf(1)}
    var positionInChapter by remember {mutableIntStateOf(0)}
    var webView by remember {mutableStateOf<EpubChapterView?>(null)}
    val bookFile=remember(book.path){File(context.filesDir,book.path)}
    LaunchedEffect(theme) {onThemeChange(theme)}
    val textColor=when(theme) {
        "Dark","AMOLED","Midnight","Graphite"->Color(0xFFF0EFE9)
        "Sage"->Color(0xFF304737)
        "Ocean"->Color(0xFF29465D)
        "Warm"->Color(0xFF523C2B)
        else->Color(0xFF352F29)
    }
    val paper=when(theme) {
        "Dark"->Color(0xFF1C2229)
        "AMOLED"->Color.Black
        "Sepia"->Color(0xFFF3E9D8)
        "Paper"->Color(0xFFFCFAF5)
        "Warm"->Color(0xFFF5DFC4)
        "Sage"->Color(0xFFDCE8D9)
        "Ocean"->Color(0xFFDCE9EF)
        "Midnight"->Color(0xFF141F34)
        "Graphite"->Color(0xFF303339)
        else->Color.White
    }
    fun saveProgress(value:Double) {
        fraction=value.coerceIn(0.0,1.0)
        val progress=if(isTextBook&&chapterFiles.isNotEmpty()) {
            val sizes=chapterFiles.map {it.length().coerceAtLeast(1).toDouble()}
            val sum=sizes.sum().coerceAtLeast(1.0)
            ((sizes.take(chapter).sum()+sizes[chapter.coerceIn(0,sizes.lastIndex)]*fraction)/sum*100).roundToInt()
        }else ((chapter+fraction)/pageCount.coerceAtLeast(1)*100).roundToInt()
        liveProgress=progress.coerceIn(0,99)
        store.change(book.path){item->
            item.put("page",chapter)
            item.put("scrollFraction",fraction)
            item.put("progress",progress.coerceIn(0,99))
            if(item.optString("status")=="Oxunmayıb")item.put("status","Oxunur")
        }
    }
    LaunchedEffect(book.path) {
        try {
            if(isTextBook) {
                val markdown=book.type in setOf("md","markdown")
                val extracted=withContext(Dispatchers.IO){
                    if(markdown)store.extractMarkdown(book.path) else store.extractChapters(book.path)
                }
                chapterLabels=if(markdown)store.markdownChapterTitles(book.path)
                    else withContext(Dispatchers.IO){store.chapterTitles(book.path,extracted)}
                chapterFiles=extracted
            }
            else pageCount=withContext(Dispatchers.IO){ReaderMedia.count(bookFile,book.type)}
            chapter=chapter.coerceIn(0,(if(isTextBook)chapterFiles.size else pageCount)-1)
        }catch(e:Exception){loadError=e.message ?: "Kitabı açmaq mümkün olmadı"}
    }
    fun moveTextPage(view:WebView,target:Int) {
        pageAnimator?.cancel()
        if(!smoothTurns) {
            view.scrollTo(0,target)
            return
        }
        pageAnimator=ValueAnimator.ofInt(view.scrollY,target).apply {
            duration=180L
            addUpdateListener {view.scrollTo(0,it.animatedValue as Int)}
            start()
        }
    }
    fun turn(dir:Int) {
        if(isTextBook && mode=="pages") {
            val view=webView
            if(view!=null) {
                val step=(view.height*0.88f).toInt().coerceAtLeast(1)
                val maxScroll=(view.contentHeight*view.scale-view.height).toInt().coerceAtLeast(0)
                val next=(view.scrollY.toFloat()/step).roundToInt()+dir
                val target=(next*step).coerceIn(0,maxScroll)
                if((dir>0 && view.scrollY<maxScroll-8) ||
                    (dir<0 && view.scrollY>8)) {
                    moveTextPage(view,target)
                    positionInChapter=next.coerceAtLeast(0)
                    saveProgress(if(maxScroll==0)0.0 else target.toDouble()/maxScroll)
                    return
                }
            }
        } else if(isTextBook) {
            webView?.let {v->
                val max=(v.contentHeight*v.scale-v.height).toInt().coerceAtLeast(0)
                val target=v.scrollY+dir*(v.height*0.88f).toInt()
                if(target in 1 until max) {
                    moveTextPage(v,target)
                    saveProgress(if(max==0)0.0 else target.toDouble()/max)
                    return
                }
            }
        }
        val actual=if(book.type=="cbz" && prefs.getBoolean("cbzRtl",false)) -dir else dir
        val count=if(isTextBook)chapterFiles.size else pageCount
        if(chapter+actual in 0 until count) {
            chapter+=actual
            fraction=if(dir<0)0.98 else 0.0
            saveProgress(fraction)
        } else if(dir>0) {
            store.change(book.path){item->
                item.put("status","Bitirilib");item.put("progress",100)
                if(item.optString("completedAt").isEmpty())
                    item.put("completedAt",java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US)
                        .format(java.util.Date()))
            }
            Toast.makeText(context,UiLanguage.translate("Kitab bitirildi ✓",
                store.setting().getString("language","az") ?: "az"),Toast.LENGTH_SHORT).show()
        }
    }
    DisposableEffect(book.path) {
        tracker.begin(book.path)
        onDispose {
            tracker.end()
            pageAnimator?.cancel()
            webView?.destroy()
        }
    }
    Scaffold(contentWindowInsets=if(focusMode)WindowInsets(0,0,0,0)
        else WindowInsets.safeDrawing,containerColor=paper) { padding->
        Box(Modifier.fillMaxSize().padding(padding).background(paper)) {
            if(loadError!=null) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                Text(loadError ?: "",color=textColor,modifier=Modifier.padding(24.dp))
            } else if(isTextBook) {
                if(chapterFiles.isEmpty()) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                    CircularProgressIndicator()
                } else key(chapter,mode,font,line,theme,alignment,readerBookmarkJump) {
                    var ready by remember {mutableStateOf(false)}
                    val pageFile=chapterFiles[chapter]
                    AndroidView(factory={ctx->
                        var startX=0f;var startY=0f
                        val view=EpubChapterView(ctx) {v->
                            if(mode=="pages") {
                                val step=(v.height*0.88f).toInt().coerceAtLeast(1)
                                val maxScroll=(v.contentHeight*v.scale-v.height)
                                    .toInt().coerceAtLeast(0)
                                chapterPages=(maxScroll+step-1)/step+1
                                val index=(v.scrollY.toFloat()/step).roundToInt()
                                    .coerceIn(0,chapterPages-1)
                                positionInChapter=index
                                if(ready)saveProgress(if(maxScroll==0)0.0 else
                                    v.scrollY.toDouble()/maxScroll)
                            }else if(ready) {
                                val max=(v.contentHeight*v.scale-v.height).toInt().coerceAtLeast(0)
                                saveProgress(if(max==0)0.0 else v.scrollY.toDouble()/max)
                            }
                        }
                        view.settings.javaScriptEnabled=false
                        view.settings.allowFileAccess=true
                        view.settings.allowFileAccessFromFileURLs=false
                        view.settings.allowUniversalAccessFromFileURLs=false
                        view.settings.allowContentAccess=false
                        view.isVerticalScrollBarEnabled=mode=="scroll"
                        view.isHorizontalScrollBarEnabled=false
                        view.setBackgroundColor(AndroidColor.TRANSPARENT)
                        view.setOnTouchListener {_,event->
                            when(event.actionMasked) {
                                MotionEvent.ACTION_DOWN->{startX=event.x;startY=event.y}
                                MotionEvent.ACTION_UP->{
                                    val dx=event.x-startX;val dy=event.y-startY
                                    if(abs(dx)<18&&abs(dy)<18) {
                                        chrome=!chrome
                                    }else if(swipe && abs(dx)>80&&abs(dx)>abs(dy)*1.4) {
                                        turn(if(dx<0)1 else -1)
                                        return@setOnTouchListener true
                                    }
                                }
                            }
                            false
                        }
                        view.webViewClient=object:WebViewClient() {
                            override fun onPageFinished(v:WebView,url:String?) {
                                v.postDelayed({
                                    val max=(v.contentHeight*v.scale-v.height)
                                        .toInt().coerceAtLeast(0)
                                    if(mode=="pages") {
                                        val step=(v.height*0.88f).toInt().coerceAtLeast(1)
                                        chapterPages=(max+step-1)/step+1
                                        positionInChapter=(fraction*max/step).roundToInt()
                                            .coerceIn(0,chapterPages-1)
                                    }
                                    v.scrollTo(0,(fraction*max).roundToInt())
                                    ready=true
                                    if(pendingSearch.isNotBlank()) {
                                        v.findAllAsync(pendingSearch)
                                    }
                                },350)
                            }
                            override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest)=true
                            override fun shouldInterceptRequest(view:WebView,request:WebResourceRequest):WebResourceResponse? {
                                return if(request.url.scheme in listOf("http","https"))
                                    WebResourceResponse("text/plain","UTF-8",java.io.ByteArrayInputStream(ByteArray(0)))
                                else super.shouldInterceptRequest(view,request)
                            }
                        }
                        val background=when(theme) {
                            "Dark"->"#1C2229";"AMOLED"->"#000000";"Sepia"->"#F3E9D8"
                            "Paper"->"#FCFAF5";"Warm"->"#F5DFC4";"Sage"->"#DCE8D9"
                            "Ocean"->"#DCE9EF";"Midnight"->"#141F34"
                            "Graphite"->"#303339";else->"#FFFFFF"
                        }
                        val foreground=when(theme) {
                            "Dark","AMOLED","Midnight","Graphite"->"#F0EFE9"
                            "Sage"->"#304737";"Ocean"->"#29465D";"Warm"->"#523C2B"
                            else->"#352F29"
                        }
                        // Reliable full-width text flow in both modes. The page mode uses
                        // viewport-sized steps, not WebView multi-column CSS.
                        val columns="html{width:100%!important;height:auto!important;overflow-x:hidden!important;}"+
                            "body{width:100%!important;height:auto!important;min-height:100%!important;"+
                            "-webkit-column-count:initial!important;column-count:initial!important;"+
                            "column-width:auto!important;white-space:normal!important;overflow:visible!important;}"
                        // Many EPUBs set font sizes, line heights and centered text on
                        // nested elements. The reader's explicit preferences override
                        // paragraph-level book styling while headings keep their hierarchy.
                        val textRules="body p,body div,body span,body section,body article,"+
                            "body blockquote,body li,body a,body em,body strong,body b,body i,"+
                            "body small,body td,body th,body font,body center{"+
                            "font-size:inherit!important;line-height:inherit!important;"+
                            "text-align:inherit!important;letter-spacing:normal!important;}"+
                            "body h1,body h2,body h3,body h4,body h5,body h6{"+
                            "text-align:inherit!important;line-height:inherit!important;}"+
                            "body h1{font-size:1.6em!important;}body h2{font-size:1.35em!important;}"+
                            "body h3{font-size:1.15em!important;}"+
                            "body p{white-space:normal!important;overflow:visible!important;}"+
                            "body font{font-size:inherit!important;}"
                        val css="<meta name='viewport' content='width=device-width,initial-scale=1.0' />"+
                            "<style>html{margin:0!important;padding:0!important;}body{"+
                            "background:"+background+"!important;color:"+foreground+"!important;"+
                            "font-size:"+font.toInt()+"px!important;"+
                            "line-height:"+(line/100f)+"!important;"+
                            "text-align:"+alignment+"!important;direction:ltr!important;"+
                            "padding:24px!important;margin:0!important;box-sizing:border-box!important;}"+
                            "img,svg{max-width:100%!important;height:auto!important;}"+
                            columns+textRules+"</style>"
                        val html=pageFile.readText(Charsets.UTF_8)
                        val result=if(html.contains("</head>",true))
                            html.replace("</head>",css+"</head>",true) else css+html
                        view.loadDataWithBaseURL(pageFile.parentFile!!.toURI().toString(),
                            result,"text/html","UTF-8",null)
                        webView=view
                        view
                    },modifier=Modifier.fillMaxSize())
                }
            } else {
                // Keep the previous rendered bitmap on screen until the next page is
                // ready. Do not tear down/recreate the zoom view for each turn.
                var displayedBitmap by remember(book.path) {
                    mutableStateOf<Bitmap?>(null)
                }
                LaunchedEffect(chapter,book.path) {
                    try {
                        val next=withContext(Dispatchers.IO) {
                            ReaderMedia.page(bookFile,book.type,chapter)
                        }
                        displayedBitmap=next
                        loadError=null
                    }catch(e:Exception) {
                        loadError=e.message ?: "Səhifə göstərilmədi"
                    }
                }
                if(displayedBitmap==null) {
                    Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val currentBitmap=displayedBitmap!!
                    val nextTurn by rememberUpdatedState<(Int)->Unit>({dir->turn(dir)})
                    val controls by rememberUpdatedState<()->Unit>({chrome=!chrome})
                    AndroidView(
                        factory={ctx->
                            ZoomPageView(ctx,
                                onNext={if(swipe)nextTurn(1)},
                                onPrevious={if(swipe)nextTurn(-1)},
                                onToggleControls={controls()},
                                onZoomChanged={zoom->prefs.edit().putFloat("zoom",zoom).apply()})
                        },
                        update={view->
                            if(view.tag !== currentBitmap) {
                                view.setPage(currentBitmap,
                                    prefs.getFloat("zoom",1f),
                                    fitWidth)
                                view.tag=currentBitmap
                            } else if(view.fitWidthEnabled()!=fitWidth) {
                                view.setFitWidth(fitWidth)
                            }
                        },
                        modifier=Modifier.fillMaxSize()
                    )
                }
            }
            if(arrows) {
                Row(Modifier.fillMaxWidth().align(Alignment.Center),horizontalArrangement=Arrangement.SpaceBetween,
                    verticalAlignment=Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick={turn(-1)},modifier=Modifier.size(52.dp),
                        colors=IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor=paper.copy(alpha=0.9f))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,ui("Əvvəlki səhifə"),tint=textColor)
                    }
                    FilledTonalIconButton(onClick={turn(1)},modifier=Modifier.size(52.dp),
                        colors=IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor=paper.copy(alpha=0.9f))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward,ui("Növbəti səhifə"),tint=textColor)
                    }
                }
            }
            AnimatedVisibility(chrome,modifier=Modifier.align(Alignment.TopCenter)) {
                Surface(color=paper.copy(alpha=0.98f),shadowElevation=4.dp) {
                    Row(Modifier.fillMaxWidth().height(62.dp).padding(horizontal=10.dp),
                        verticalAlignment=Alignment.CenterVertically) {
                        IconButton(onClick=onExit,modifier=Modifier.size(48.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack,ui("Kitabxanaya qayıt"),tint=textColor)
                        }
                        if(!landscape) {
                            Text(book.title,modifier=Modifier.weight(1f),maxLines=1,
                                overflow=TextOverflow.Ellipsis,color=textColor,
                                fontWeight=FontWeight.Medium)
                        } else Spacer(Modifier.weight(1f))
                        if(isTextBook) IconButton(onClick={showChapterSearch=true},
                            modifier=Modifier.size(40.dp)) {
                            Icon(Icons.Default.Search,ui("Kitabda axtar"),tint=textColor)
                        }
                        IconButton(onClick={draftQuote="";showReadingNotes=true},
                            modifier=Modifier.size(40.dp)) {
                            Icon(Icons.Default.EditNote,ui("Qeydlər və sitatlar"),tint=textColor)
                        }
                        if(isTextBook) IconButton(onClick={showChapters=true},
                            modifier=Modifier.size(40.dp)) {
                            Icon(Icons.Default.List,ui("Fəsillər"),tint=textColor)
                        }
                        IconButton(onClick={
                            val added=store.toggleBookmark(book.path,chapter,
                                if(isTextBook)fraction else 0.0)
                            bookmarkRefresh++
                            Toast.makeText(context,UiLanguage.translate(
                                if(added)"Əlfəcin əlavə edildi" else "Əlfəcin silindi",
                                store.setting().getString("language","az") ?: "az"),
                                Toast.LENGTH_SHORT).show()
                        },modifier=Modifier.size(44.dp)) {
                            val isMarked=remember(bookmarkRefresh,chapter,fraction) {
                                store.hasBookmark(book.path,chapter,
                                    if(isTextBook)fraction else 0.0)
                            }
                            Icon(if(isMarked)Icons.Default.Bookmark else Icons.Default.BookmarkBorder,ui("Əlfəcin əlavə et və ya sil"),tint=textColor)
                        }
                        IconButton(onClick={showReaderBookmarks=true},
                            modifier=Modifier.size(44.dp)) {
                            Icon(Icons.Default.Bookmarks,ui("Əlfəcinlərim"),tint=textColor)
                        }
                        IconButton(onClick={
                            focusMode=!focusMode
                            if(focusMode)chrome=false
                        },modifier=Modifier.size(40.dp)) {
                            Icon(if(focusMode)Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                ui(if(focusMode)"Focus rejimindən çıx" else "Focus rejimi"),
                                tint=textColor)
                        }
                        FilledTonalIconButton(onClick={showSettings=true},modifier=Modifier.size(48.dp)) {
                            Text(ui("Aa"),fontSize=18.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
            if(!chrome) {
                Text(ui(liveProgress.toString()+"%"),modifier=Modifier.align(Alignment.BottomStart)
                    .padding(start=24.dp,bottom=12.dp),fontSize=12.sp,color=textColor.copy(alpha=0.6f))
                Text(ui(if(isTextBook)"Fəsil "+(chapter+1) else (chapter+1).toString()+"/"+pageCount),
                    modifier=Modifier.align(Alignment.BottomEnd).padding(end=24.dp,bottom=12.dp),
                    fontSize=12.sp,color=textColor.copy(alpha=0.6f))
            }
            if(chrome) {
                Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(paper.copy(alpha=0.97f))
                    .padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
                    IconButton(onClick={turn(-1)}) {Icon(Icons.AutoMirrored.Filled.ArrowBack,ui("Əvvəlki"),tint=textColor)}
                    Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(ui(if(isTextBook)"Fəsil "+(chapter+1)+"/"+chapterFiles.size
                            else "Səhifə "+(chapter+1)+"/"+pageCount),
                            color=textColor,fontSize=13.sp)
                        Text("⏱ "+ReadingClock.display(elapsedSeconds),
                            fontSize=11.sp,color=textColor.copy(alpha=0.72f))
                    }
                    IconButton(onClick={turn(1)}) {Icon(Icons.AutoMirrored.Filled.ArrowForward,ui("Növbəti"),tint=textColor)}
                }
            }
        }
    }
    if(showSettings) {
        ModalBottomSheet(onDismissRequest={showSettings=false},containerColor=MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal=24.dp).padding(bottom=24.dp)) {
                Text(ui("Oxuma ayarları"),fontSize=24.sp,fontWeight=FontWeight.Bold)
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("📚  "+ui("Bu kitab üçün ayrıca parametrlər"),
                        modifier=Modifier.weight(1f),fontWeight=FontWeight.SemiBold)
                    Switch(checked=perBookSettings,onCheckedChange={enabled->
                        if(enabled) {
                            val snapshot=JSONObject()
                                .put("enabled",true)
                                .put("readerTheme",theme)
                                .put("fontSize",font.toInt())
                                .put("lineSpacing",line.toInt())
                                .put("textAlignment",alignment)
                                .put("epubMode",mode)
                                .put("screenOrientation",rotationMode)
                                .put("swipeTurn",swipe)
                                .put("sideButtons",arrows)
                                .put("fitWidth",fitWidth)
                                .put("smoothPageTurns",smoothTurns)
                            store.change(book.path) {it.put("readerOptions",snapshot)}
                        } else {
                            store.change(book.path) {entry->
                                entry.optJSONObject("readerOptions")?.put("enabled",false)
                            }
                            font=prefs.getInt("fontSize",20).toFloat()
                            line=prefs.getInt("lineSpacing",160).toFloat()
                            alignment=prefs.getString("textAlignment","left") ?: "left"
                            mode=prefs.getString("epubMode","scroll") ?: "scroll"
                            rotationMode=prefs.getString("screenOrientation","Auto") ?: "Auto"
                            swipe=prefs.getBoolean("swipeTurn",true)
                            arrows=prefs.getBoolean("sideButtons",false)
                            fitWidth=prefs.getBoolean("fitWidth",false)
                            smoothTurns=prefs.getBoolean("smoothPageTurns",true)
                            readerFollowsApp=prefs.getBoolean("readerFollowsApp",true)
                            theme=if(readerFollowsApp)
                                prefs.getString("theme","Sepia") ?: "Sepia"
                            else prefs.getString("readerTheme","Sepia") ?: "Sepia"
                            onThemeChange(theme)
                        }
                        perBookSettings=enabled
                    })
                }
                HorizontalDivider(Modifier.padding(vertical=8.dp))
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("🎯  "+ui("Focus rejimi"),modifier=Modifier.weight(1f))
                    Switch(focusMode,{focusMode=it;chrome=if(it)false else chrome})
                }
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("⏱  "+ui("Oxu taymeri"),modifier=Modifier.weight(1f))
                    Text(ReadingClock.display(elapsedSeconds),fontWeight=FontWeight.Bold)
                    TextButton(onClick={
                        readingClock.reset(SystemClock.elapsedRealtime())
                        elapsedSeconds=0L
                    }) {Text(ui("Sıfırla"))}
                }
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("☀️  "+ui("Parlaqlıq"),modifier=Modifier.weight(1f))
                    Switch(checked=brightness>=0f,onCheckedChange={on->
                        brightness=if(on)0.65f else -1f
                        prefs.edit().putFloat("readerBrightness",brightness).apply()
                    })
                }
                if(brightness>=0f) {
                    Slider(value=brightness.coerceIn(0.05f,1f),onValueChange={next->
                        brightness=next.coerceIn(0.05f,1f)
                        prefs.edit().putFloat("readerBrightness",brightness).apply()
                    },valueRange=0.05f..1f)
                }
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("✨  "+ui("Axıcı səhifə keçidi"),modifier=Modifier.weight(1f))
                    Switch(smoothTurns,{
                        smoothTurns=it
                        saveOption("smoothPageTurns",it)
                    })
                }
                HorizontalDivider(Modifier.padding(vertical=8.dp))
                Text("📱  "+ui("Ekran istiqaməti"),fontWeight=FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    listOf("Auto","Portrait","Landscape").forEach {option->
                        FilterChip(
                            selected=rotationMode==option,
                            onClick={
                                rotationMode=option
                                saveOption("screenOrientation",option)
                            },
                            label={Text(ui(when(option) {
                                "Auto"->"Avtomatik"
                                "Portrait"->"Şaquli"
                                else->"Üfüqi"
                            }))}
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                if(!perBookSettings) Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("🎨  "+ui("Qlobal temanı izlə"),modifier=Modifier.weight(1f),
                        fontWeight=FontWeight.SemiBold)
                    Switch(checked=readerFollowsApp,onCheckedChange={follow->
                        readerFollowsApp=follow
                        prefs.edit().putBoolean("readerFollowsApp",follow).apply()
                        theme=if(follow) prefs.getString("theme","Sepia") ?: "Sepia"
                              else prefs.getString("readerTheme","Sepia") ?: "Sepia"
                        onThemeChange(theme)
                    })
                }
                Text("🌗  "+ui("Oxuma teması · 10 hazır seçim"),fontWeight=FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    ReadingThemes.presets.forEach {t->
                        FilterChip(theme==t.name,{
                            theme=t.name
                            onThemeChange(t.name)
                            if(perBookSettings)saveOption("readerTheme",t.name)
                            else {
                                prefs.edit().putString("readerTheme",t.name)
                                    .putBoolean("readerFollowsApp",false).apply()
                                readerFollowsApp=false
                            }
                        },label={Text(ui(t.name))})
                    }
                }
                Spacer(Modifier.height(16.dp))
                if(isTextBook) {
                    Text("🔠  "+ui("Şrift ölçüsü  "+font.toInt()+" px"),fontWeight=FontWeight.SemiBold)
                    Slider(font,{font=it;saveOption("fontSize",it.toInt())},valueRange=12f..34f)
                    Text("↕️  "+ui("Sətirarası məsafə  "+line.toInt()+"%"),fontWeight=FontWeight.SemiBold)
                    Slider(line,{line=it;saveOption("lineSpacing",it.toInt())},valueRange=110f..210f)
                    Spacer(Modifier.height(12.dp))
                    Text("↔️  "+ui("Mətnin düzülüşü"),fontWeight=FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        listOf("left" to "Soldan", "justify" to "İki kənara",
                            "center" to "Ortadan", "right" to "Sağdan").forEach { (value,label)->
                            FilterChip(
                                selected=alignment==value,
                                onClick={
                                    alignment=value
                                    saveOption("textAlignment",value)
                                },
                                label={Text(ui(label))}
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("📖  "+ui("Oxuma rejimi"),fontWeight=FontWeight.SemiBold)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        FilterChip(mode=="pages",{mode="pages";saveOption("epubMode",mode)},
                            label={Text(ui("Səhifələmə"))})
                        FilterChip(mode=="scroll",{mode="scroll";prefs.edit().putString("epubMode",mode).apply()},
                            label={Text(ui("Continuous scroll"))})
                    }
                }
                HorizontalDivider(Modifier.padding(vertical=12.dp))
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("👆  "+ui("Swipe ilə səhifə çevirmə"),modifier=Modifier.weight(1f))
                    Switch(swipe,{swipe=it;saveOption("swipeTurn",it)})
                }
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("↔️  "+ui("Sağ-sol düymələri göstər"),modifier=Modifier.weight(1f))
                    Switch(arrows,{arrows=it;saveOption("sideButtons",it)})
                }
                if(!isTextBook) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text("📐  "+ui("Fit to Width"),modifier=Modifier.weight(1f))
                        Switch(fitWidth,{fitWidth=it;saveOption("fitWidth",it)})
                    }
                }
                Button(onClick={showSettings=false},modifier=Modifier.fillMaxWidth().height(52.dp)) {
                    Text(ui("Oxumağa davam et"))
                }
            }
        }
    }
    if(showChapterSearch && isTextBook) {
        ChapterSearchSheet(chapters=chapterFiles,labels=chapterLabels,
            onJump={index,query->
                pendingSearch=query
                if(chapter==index) {
                    webView?.findAllAsync(query)
                }else {
                    chapter=index
                    fraction=0.0
                    saveProgress(0.0)
                }
                showChapterSearch=false
            },
            onSaveQuote={index,snippet->
                draftQuote=snippet
                draftChapter=index
                showChapterSearch=false
                showReadingNotes=true
            },
            onDismiss={showChapterSearch=false})
    }
    if(showReadingNotes) {
        ReadingNotesSheet(store=store,bookPath=book.path,
            currentChapter=if(draftChapter>=0)draftChapter else chapter,
            initialQuote=draftQuote,
            onJump={index->
                if(isTextBook) {
                    if(index in chapterFiles.indices) {
                        chapter=index;fraction=0.0;saveProgress(0.0)
                    }
                }else if(index in 0 until pageCount) {
                    chapter=index;fraction=0.0;saveProgress(0.0)
                }
                showReadingNotes=false
                draftQuote=""
            },
            onClose={showReadingNotes=false;draftQuote="";draftChapter=-1})
    }
    if(showReaderBookmarks) {
        BookmarksSheet(store,bookPath=book.path,
            onOpen={_,mark->
                chapter=mark.chapter
                fraction=mark.fraction
                bookmarkRefresh++
                showReaderBookmarks=false
                readerBookmarkJump++
                saveProgress(fraction)
            },
            onClose={showReaderBookmarks=false},
            onChanged={bookmarkRefresh++})
    }
    if(showChapters) {
        ModalBottomSheet(onDismissRequest={showChapters=false},containerColor=MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
                Text(ui("Fəsillər"),fontSize=24.sp,fontWeight=FontWeight.Bold)
                LazyColumn(contentPadding=PaddingValues(bottom=36.dp)) {
                    items(chapterFiles.size) {index->
                        ListItem(
                            headlineContent={Text(chapterLabels.getOrNull(index) ?:
                                chapterFiles[index].nameWithoutExtension.replace('_',' '))},
                            leadingContent={Text((index+1).toString())},
                            modifier=Modifier.clickable {
                                chapter=index;fraction=0.0;saveProgress(0.0);showChapters=false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
