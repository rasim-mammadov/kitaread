@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package az.leafreader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.MotionEvent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
        setContent { LeafApp(store,tracker) }
    }
}

@Composable
private fun LeafApp(store:BookStore,tracker:ReadingSessionTracker) {
    val context=LocalContext.current
    var books by remember {mutableStateOf(store.books())}
    var selected by remember {mutableStateOf<ReaderBook?>(null)}
    var search by remember {mutableStateOf("")}
    var filter by remember {mutableStateOf("Hamısı")}
    var menuBook by remember {mutableStateOf<ReaderBook?>(null)}
    var showDashboard by remember {mutableStateOf(false)}
    val filePicker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri->
        if(uri!=null) try {
            store.import(uri)
            books=store.books()
        }catch(e:Exception){Toast.makeText(context,e.message ?: "Fayl açılmadı",Toast.LENGTH_LONG).show()}
    }
    val jsonExport=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {uri->
        if(uri!=null)try {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(store.exportMetadata().toByteArray(Charsets.UTF_8))
            } ?:error("Yazma icazəsi yoxdur")
            Toast.makeText(context,"JSON metadata backup saxlanıldı",Toast.LENGTH_LONG).show()
        }catch(e:Exception){Toast.makeText(context,e.message ?: "Backup xətası",Toast.LENGTH_LONG).show()}
    }
    val jsonImport=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri->
        if(uri!=null)try {
            val data=context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {it.readText()}
                ?:error("Backup faylı oxunmadı")
            val report=store.restoreMetadata(data)
            books=store.books()
            Toast.makeText(context,
                "Bərpa: ${report.imported} kitab. ${report.missingFiles} kitabın faylı cihazda yoxdur.",
                Toast.LENGTH_LONG).show()
        }catch(e:Exception){Toast.makeText(context,e.message ?: "Import xətası",Toast.LENGTH_LONG).show()}
    }
    MaterialTheme(colorScheme=lightColorScheme(
        primary=Accent,secondary=Ink,background=Cream,surface=Color.White,
        onBackground=Ink,onSurface=Ink
    ),shapes=Shapes(medium=RoundedCornerShape(16.dp),large=RoundedCornerShape(24.dp))) {
        if(selected==null && showDashboard) {
            ReadingDashboard(store,onBack={showDashboard=false})
        } else if(selected==null) {
            Scaffold(
                contentWindowInsets=WindowInsets.safeDrawing,
                containerColor=Cream,
                topBar={
                    Column(Modifier.fillMaxWidth().padding(start=22.dp,end=22.dp,top=20.dp,bottom=8.dp)) {
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Leaf Reader",fontSize=30.sp,fontWeight=FontWeight.Bold,color=Ink)
                                Text("Sənin şəxsi kitabxanan",fontSize=14.sp,color=Gray)
                            }
                            FilledTonalIconButton(onClick={showDashboard=true},
                                modifier=Modifier.size(50.dp)) {
                                Icon(Icons.Default.BarChart,"Oxu statistikası")
                            }
                        }
                    }
                },
                floatingActionButton={
                    ExtendedFloatingActionButton(onClick={
                        filePicker.launch(arrayOf("application/pdf","application/epub+zip","application/zip","application/octet-stream"))
                    },icon={Icon(Icons.Default.Add,null)},text={Text("Kitab əlavə et")})
                }
            ){ inner->
                Column(Modifier.fillMaxSize().padding(inner).padding(horizontal=18.dp)) {
                    OutlinedTextField(
                        search,{search=it},modifier=Modifier.fillMaxWidth(),
                        singleLine=true,shape=RoundedCornerShape(18.dp),
                        placeholder={Text("Kitab axtar...")},
                        leadingIcon={Icon(Icons.Default.Search,null)}
                    )
                    Row(Modifier.fillMaxWidth().padding(top=5.dp),
                        horizontalArrangement=Arrangement.spacedBy(8.dp),
                        verticalAlignment=Alignment.CenterVertically) {
                        Text("Məlumatların ehtiyat nüsxəsi",fontSize=12.sp,color=Gray,
                            modifier=Modifier.weight(1f))
                        TextButton(onClick={jsonExport.launch("leaf-reader-metadata.json")}) {
                            Text("Export",fontSize=12.sp)
                        }
                        TextButton(onClick={jsonImport.launch(arrayOf("application/json","text/plain"))}) {
                            Text("Import",fontSize=12.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement=Arrangement.spacedBy(7.dp),
                        modifier=Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("Hamısı","Oxunur","Oxunmayıb","Bitirilib").forEach { label->
                            FilterChip(selected=filter==label,onClick={filter=label},label={Text(label)})
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val visible=books.filter {
                        (filter=="Hamısı" || it.status==filter) && it.title.contains(search,true)
                    }
                    if(visible.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(bottom=60.dp),contentAlignment=Alignment.Center) {
                            Column(horizontalAlignment=Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AutoStories,null,Modifier.size(64.dp),tint=Accent)
                                Spacer(Modifier.height(16.dp))
                                Text("Burada hələ kitab yoxdur",fontSize=20.sp,fontWeight=FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Text("EPUB, PDF və ya CBZ əlavə et",color=Gray)
                            }
                        }
                    } else LazyColumn(contentPadding=PaddingValues(bottom=96.dp),
                        verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        items(visible,key={it.path}) { book->
                            LibraryCard(book,store,onOpen={selected=book},
                                onMore={menuBook=book})
                        }
                    }
                }
            }
        } else {
            val book=selected!!
            ReaderScreen(book,store,tracker,onExit={
                selected=null
                books=store.books()
            })
        }
        menuBook?.let { book->
            AlertDialog(onDismissRequest={menuBook=null},
                title={Text(book.title,maxLines=2)},
                text={
                    Column {
                        listOf("Oxunmayıb","Oxunur","Bitirilib","Yarımçıq saxlanılıb").forEach { status->
                            TextButton(onClick={
                                store.change(book.path) { row->
                                    row.put("status",status)
                                    if(status=="Bitirilib") {
                                        row.put("progress",100)
                                        if(row.optString("completedAt").isEmpty())
                                            row.put("completedAt",java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US)
                                                .format(java.util.Date()))
                                    }
                                }
                                books=store.books()
                                menuBook=null
                            },modifier=Modifier.fillMaxWidth()) {Text(status)}
                        }
                    }
                },
                confirmButton={TextButton(onClick={menuBook=null}) {Text("Bağla")}}
            )
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
        colors=CardDefaults.cardColors(containerColor=Color.White),
        elevation=CardDefaults.cardElevation(defaultElevation=1.dp)) {
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(width=78.dp,height=108.dp).clip(RoundedCornerShape(10.dp))
                .background(Accent),contentAlignment=Alignment.Center) {
                if(image!=null)Image(image.asImageBitmap(),null,Modifier.fillMaxSize(),
                    contentScale=ContentScale.Crop)
                else Icon(Icons.Default.MenuBook,null,Modifier.size(44.dp),tint=Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(book.title,fontWeight=FontWeight.SemiBold,fontSize=17.sp,
                    maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(book.status+"  •  "+book.type.uppercase(),
                    color=Gray,fontSize=12.sp)
                LinearProgressIndicator(progress={book.progress/100f},
                    modifier=Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(8.dp)),
                    color=Accent,trackColor=Space)
                Text(book.progress.toString()+"% oxunub",fontSize=12.sp,color=Accent)
            }
            IconButton(onClick=onMore) {Icon(Icons.Default.MoreVert,"Kitab ayarları")}
        }
    }
}

@Composable
private fun ReaderScreen(book:ReaderBook,store:BookStore,tracker:ReadingSessionTracker,onExit:()->Unit) {
    BackHandler(onBack=onExit)
    val context=LocalContext.current
    val prefs=remember {store.setting()}
    var theme by remember {mutableStateOf(prefs.getString("theme","Sepia") ?: "Sepia")}
    var font by remember {mutableFloatStateOf(prefs.getInt("fontSize",20).toFloat())}
    var line by remember {mutableFloatStateOf(prefs.getInt("lineSpacing",160).toFloat())}
    var alignment by remember {
        mutableStateOf(prefs.getString("textAlignment","left") ?: "left")
    }
    var mode by remember {mutableStateOf(prefs.getString("epubMode","scroll") ?: "scroll")}
    var swipe by remember {mutableStateOf(prefs.getBoolean("swipeTurn",true))}
    var arrows by remember {mutableStateOf(prefs.getBoolean("sideButtons",false))}
    var chrome by remember {mutableStateOf(false)}
    var showSettings by remember {mutableStateOf(false)}
    var showChapters by remember {mutableStateOf(false)}
    var chapter by remember(book.path) {mutableIntStateOf(book.chapter)}
    var liveProgress by remember(book.path) {mutableIntStateOf(book.progress)}
    var fraction by remember(book.path) {mutableDoubleStateOf(book.fraction)}
    var chapterFiles by remember(book.path) {mutableStateOf<List<File>>(emptyList())}
    var chapterLabels by remember(book.path) {mutableStateOf<List<String>>(emptyList())}
    var loadError by remember {mutableStateOf<String?>(null)}
    var pageCount by remember {mutableIntStateOf(1)}
    var chapterPages by remember {mutableIntStateOf(1)}
    var positionInChapter by remember {mutableIntStateOf(0)}
    var webView by remember {mutableStateOf<EpubChapterView?>(null)}
    val bookFile=remember(book.path){File(context.filesDir,book.path)}
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
        val progress=if(book.type=="epub"&&chapterFiles.isNotEmpty()) {
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
            if(book.type=="epub") {
                val extracted=withContext(Dispatchers.IO){store.extractChapters(book.path)}
                chapterLabels=withContext(Dispatchers.IO){store.chapterTitles(book.path,extracted)}
                chapterFiles=extracted
            }
            else pageCount=withContext(Dispatchers.IO){ReaderMedia.count(bookFile,book.type)}
            chapter=chapter.coerceIn(0,(if(book.type=="epub")chapterFiles.size else pageCount)-1)
        }catch(e:Exception){loadError=e.message ?: "Kitabı açmaq mümkün olmadı"}
    }
    fun turn(dir:Int) {
        if(book.type=="epub" && mode=="pages") {
            val view=webView
            if(view!=null) {
                val step=(view.height*0.88f).toInt().coerceAtLeast(1)
                val maxScroll=(view.contentHeight*view.scale-view.height).toInt().coerceAtLeast(0)
                val next=(view.scrollY.toFloat()/step).roundToInt()+dir
                val target=(next*step).coerceIn(0,maxScroll)
                if((dir>0 && view.scrollY<maxScroll-8) ||
                    (dir<0 && view.scrollY>8)) {
                    view.scrollTo(0,target)
                    positionInChapter=next.coerceAtLeast(0)
                    saveProgress(if(maxScroll==0)0.0 else target.toDouble()/maxScroll)
                    return
                }
            }
        } else if(book.type=="epub") {
            webView?.let {v->
                val max=(v.contentHeight*v.scale-v.height).toInt().coerceAtLeast(0)
                val target=v.scrollY+dir*(v.height*0.88f).toInt()
                if(target in 1 until max) {
                    v.scrollTo(0,target)
                    saveProgress(if(max==0)0.0 else target.toDouble()/max)
                    return
                }
            }
        }
        val actual=if(book.type=="cbz" && prefs.getBoolean("cbzRtl",false)) -dir else dir
        val count=if(book.type=="epub")chapterFiles.size else pageCount
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
            Toast.makeText(context,"Kitab bitirildi ✓",Toast.LENGTH_SHORT).show()
        }
    }
    DisposableEffect(book.path) {
        tracker.begin(book.path)
        onDispose {
            tracker.end()
            webView?.destroy()
        }
    }
    Scaffold(contentWindowInsets=WindowInsets.safeDrawing,containerColor=paper) { padding->
        Box(Modifier.fillMaxSize().padding(padding).background(paper)) {
            if(loadError!=null) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                Text(loadError ?: "",color=textColor,modifier=Modifier.padding(24.dp))
            } else if(book.type=="epub") {
                if(chapterFiles.isEmpty()) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                    CircularProgressIndicator()
                } else key(chapter,mode,font,line,theme,alignment) {
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
                var bitmap by remember(chapter,book.path) {mutableStateOf<Bitmap?>(null)}
                LaunchedEffect(chapter,book.path) {
                    bitmap=try {withContext(Dispatchers.IO) {
                        ReaderMedia.page(bookFile,book.type,chapter)
                    }}catch(e:Exception){loadError=e.message;null}
                }
                if(bitmap==null) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                    CircularProgressIndicator()
                } else key(chapter) {
                    AndroidView(factory={ctx->
                        ZoomPageView(ctx,onNext={if(swipe)turn(1)},
                            onPrevious={if(swipe)turn(-1)},
                            onToggleControls={chrome=!chrome},
                            onZoomChanged={zoom->prefs.edit().putFloat("zoom",zoom).apply()})
                            .apply {setPage(bitmap!!,prefs.getFloat("zoom",1f),prefs.getBoolean("fitWidth",false))}
                    },modifier=Modifier.fillMaxSize())
                }
            }
            if(arrows) {
                Row(Modifier.fillMaxWidth().align(Alignment.Center),horizontalArrangement=Arrangement.SpaceBetween,
                    verticalAlignment=Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick={turn(-1)},modifier=Modifier.size(52.dp),
                        colors=IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor=paper.copy(alpha=0.9f))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,"Əvvəlki səhifə",tint=textColor)
                    }
                    FilledTonalIconButton(onClick={turn(1)},modifier=Modifier.size(52.dp),
                        colors=IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor=paper.copy(alpha=0.9f))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward,"Növbəti səhifə",tint=textColor)
                    }
                }
            }
            AnimatedVisibility(chrome,modifier=Modifier.align(Alignment.TopCenter)) {
                Surface(color=paper.copy(alpha=0.98f),shadowElevation=4.dp) {
                    Row(Modifier.fillMaxWidth().height(62.dp).padding(horizontal=10.dp),
                        verticalAlignment=Alignment.CenterVertically) {
                        IconButton(onClick=onExit,modifier=Modifier.size(48.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack,"Kitabxanaya qayıt",tint=textColor)
                        }
                        Text(book.title,modifier=Modifier.weight(1f),maxLines=1,
                            overflow=TextOverflow.Ellipsis,color=textColor,fontWeight=FontWeight.Medium)
                        if(book.type=="epub") IconButton(onClick={showChapters=true},
                            modifier=Modifier.size(48.dp)) {
                            Icon(Icons.Default.List,"Fəsillər",tint=textColor)
                        }
                        FilledTonalIconButton(onClick={showSettings=true},modifier=Modifier.size(48.dp)) {
                            Text("Aa",fontSize=18.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
            if(!chrome) {
                Text(liveProgress.toString()+"%",modifier=Modifier.align(Alignment.BottomStart)
                    .padding(start=24.dp,bottom=12.dp),fontSize=12.sp,color=textColor.copy(alpha=0.6f))
                Text(if(book.type=="epub")"Fəsil "+(chapter+1) else (chapter+1).toString()+"/"+pageCount,
                    modifier=Modifier.align(Alignment.BottomEnd).padding(end=24.dp,bottom=12.dp),
                    fontSize=12.sp,color=textColor.copy(alpha=0.6f))
            }
            if(chrome) {
                Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(paper.copy(alpha=0.97f))
                    .padding(horizontal=16.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically) {
                    IconButton(onClick={turn(-1)}) {Icon(Icons.AutoMirrored.Filled.ArrowBack,"Əvvəlki",tint=textColor)}
                    Text(if(book.type=="epub")"Fəsil "+(chapter+1)+"/"+chapterFiles.size
                        else "Səhifə "+(chapter+1)+"/"+pageCount,
                        modifier=Modifier.weight(1f),color=textColor,fontSize=13.sp)
                    IconButton(onClick={turn(1)}) {Icon(Icons.AutoMirrored.Filled.ArrowForward,"Növbəti",tint=textColor)}
                }
            }
        }
    }
    if(showSettings) {
        ModalBottomSheet(onDismissRequest={showSettings=false},containerColor=Cream) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal=24.dp).padding(bottom=24.dp)) {
                Text("Oxuma ayarları",fontSize=24.sp,fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                if(book.type=="epub") {
                    Text("Şrift ölçüsü  "+font.toInt()+" px",fontWeight=FontWeight.SemiBold)
                    Slider(font,{font=it; prefs.edit().putInt("fontSize",it.toInt()).apply()},valueRange=12f..34f)
                    Text("Sətirarası məsafə  "+line.toInt()+"%",fontWeight=FontWeight.SemiBold)
                    Slider(line,{line=it; prefs.edit().putInt("lineSpacing",it.toInt()).apply()},valueRange=110f..210f)
                    Spacer(Modifier.height(12.dp))
                    Text("Mətnin düzülüşü",fontWeight=FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        listOf("left" to "Soldan", "justify" to "İki kənara",
                            "center" to "Ortadan", "right" to "Sağdan").forEach { (value,label)->
                            FilterChip(
                                selected=alignment==value,
                                onClick={
                                    alignment=value
                                    prefs.edit().putString("textAlignment",value).apply()
                                },
                                label={Text(label)}
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Oxuma rejimi",fontWeight=FontWeight.SemiBold)
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        FilterChip(mode=="pages",{mode="pages";prefs.edit().putString("epubMode",mode).apply()},
                            label={Text("Səhifələmə")})
                        FilterChip(mode=="scroll",{mode="scroll";prefs.edit().putString("epubMode",mode).apply()},
                            label={Text("Continuous scroll")})
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Tema",fontWeight=FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    listOf("Light","Sepia","Dark","AMOLED","Paper","Warm",
                        "Sage","Ocean","Midnight","Graphite").forEach {t->
                        FilterChip(theme==t,{theme=t;prefs.edit().putString("theme",t).apply()},
                            label={Text(t,fontSize=11.sp)})
                    }
                }
                HorizontalDivider(Modifier.padding(vertical=12.dp))
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("Swipe ilə səhifə çevirmə",modifier=Modifier.weight(1f))
                    Switch(swipe,{swipe=it;prefs.edit().putBoolean("swipeTurn",it).apply()})
                }
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("Sağ-sol düymələri göstər",modifier=Modifier.weight(1f))
                    Switch(arrows,{arrows=it;prefs.edit().putBoolean("sideButtons",it).apply()})
                }
                if(book.type!="epub") {
                    var fit by remember {mutableStateOf(prefs.getBoolean("fitWidth",false))}
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        Text("Fit to Width",modifier=Modifier.weight(1f))
                        Switch(fit,{fit=it;prefs.edit().putBoolean("fitWidth",it).apply()})
                    }
                }
                Button(onClick={showSettings=false},modifier=Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Oxumağa davam et")
                }
            }
        }
    }
    if(showChapters) {
        ModalBottomSheet(onDismissRequest={showChapters=false},containerColor=Cream) {
            Column(Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
                Text("Fəsillər",fontSize=24.sp,fontWeight=FontWeight.Bold)
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
