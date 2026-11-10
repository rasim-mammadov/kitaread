package az.leafreader

import android.app.*
import android.os.*
import android.content.*
import android.graphics.*
import android.net.Uri
import android.provider.OpenableColumns
import android.view.*
import android.webkit.*
import android.widget.*
import android.util.Xml
import org.json.*
import org.xmlpull.v1.XmlPullParser
import java.io.*
import java.util.zip.ZipFile
import kotlin.math.*

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("library", MODE_PRIVATE) }
    private val books = mutableListOf<JSONObject>()
    private lateinit var root: LinearLayout
    private var current: JSONObject? = null
    private var pdf: android.graphics.pdf.PdfRenderer? = null
    private var pdfDescriptor: ParcelFileDescriptor? = null
    private var page = 0
    private var total = 1
    private var web: WebView? = null
    private var epubChapters = listOf<File>()
    private var imageEntries = listOf<String>()
    private var zip: ZipFile? = null
    private var controls: TextView? = null
    private var readerContainer: FrameLayout? = null
    private var readerBitmap: Bitmap? = null
    private val teal = Color.rgb(26, 107, 104)
    private val saveHandler = Handler(Looper.getMainLooper())
    private var pendingSave: Runnable? = null
    private var restoringPosition = false
    private var activeReaderView: View? = null
    private var headerView: View? = null
    private var footerView: View? = null
    private var readerBarsVisible = true
    private val settings by lazy { getSharedPreferences("reader_settings", MODE_PRIVATE) }
    private fun setFullScreen(enabled: Boolean) {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = if(enabled)
            (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            else 0
    }
    private fun toggleBars() {
        readerBarsVisible = !readerBarsVisible
        headerView?.visibility = if(readerBarsVisible) View.VISIBLE else View.GONE
        footerView?.visibility = if(readerBarsVisible) View.VISIBLE else View.GONE
    }
    private fun markFinished(book: JSONObject) {
        book.put("status", "Bitirilib")
        book.put("progress", 100)
        if(book.optString("completedAt").isEmpty()) book.put("completedAt",
            java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(java.util.Date()))
    }
    private fun scrollFraction(view: View, maxScroll: Int): Double =
        if (maxScroll <= 0) 1.0 else (view.scrollY.toDouble() / maxScroll).coerceIn(0.0, 1.0)

    private fun recordPosition(fraction: Double, immediate: Boolean = false) {
        val book = current ?: return
        if (restoringPosition) return
        val clamped = fraction.coerceIn(0.0, 1.0)
        book.put("page", page)
        book.put("scrollFraction", clamped)
        val raw = ((page + clamped) / total * 100.0).roundToInt()
        val progress = if (book.optString("status") == "Bitirilib") 100 else raw.coerceIn(0, 99)
        book.put("progress", progress)
        if (book.optString("status") == "Oxunmayıb") book.put("status", "Oxunur")
        controls?.text = "${page + 1} / $total   •   $progress%"
        pendingSave?.let { saveHandler.removeCallbacks(it) }
        val task = Runnable { save(); pendingSave = null }
        if (immediate) task.run() else { pendingSave = task; saveHandler.postDelayed(task, 350) }
    }

    private fun flushPosition() {
        pendingSave?.let { saveHandler.removeCallbacks(it); pendingSave = null }
        save()
    }

    private fun restoreFraction(view: View, maxScroll: () -> Int, fraction: Double) {
        restoringPosition = true
        intArrayOf(60, 250, 600).forEachIndexed { idx, delay ->
            view.postDelayed({
                if (activeReaderView !== view) return@postDelayed
                view.scrollTo(0, (maxScroll().coerceAtLeast(0) * fraction.coerceIn(0.0, 1.0)).roundToInt())
                if (idx == 2) {
                    restoringPosition = false
                    recordPosition(scrollFraction(view, maxScroll()), true)
                }
            }, delay.toLong())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); load(); shelf() }
    private fun load() {
        books.clear()
        val arr = JSONArray(prefs.getString("items", "[]"))
        for (i in 0 until arr.length()) books.add(arr.getJSONObject(i))
    }
    private fun save() {
        val arr = JSONArray()
        books.forEach { arr.put(it) }
        prefs.edit().putString("items", arr.toString()).apply()
    }
    private fun bar(title: String, back: Boolean = false) {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(248,249,247)) }
        setContentView(root)
        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(18,20,18,20); setBackgroundColor(teal) }
        if(back) head.addView(action("‹") { closeReader(); shelf() })
        head.addView(TextView(this).apply {
            text = title; textSize = 22f; setTextColor(Color.WHITE); setTypeface(null,1)
            layoutParams = LinearLayout.LayoutParams(0,-2,1f)
        })
        root.addView(head)
        headerView=head
    }
    private fun action(s: String, onClick: () -> Unit): TextView = TextView(this).apply {
        text=s; textSize=17f; setTextColor(teal); setPadding(14,12,14,12); setOnClickListener { onClick() }
    }
    private fun shelf() {
        closeReader(); setFullScreen(false); bar("Leaf Reader")
        val tools = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        tools.addView(action("＋ Fayl əlavə et") {
            val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE); type="*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf","application/epub+zip","application/zip","application/x-cbz","application/octet-stream"))
            }
            startActivityForResult(i,42)
        })
        root.addView(tools)
        val scroll=ScrollView(this)
        val list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(16,4,16,20) }
        scroll.addView(list); root.addView(scroll)
        if(books.isEmpty()) list.addView(TextView(this).apply {
            text="Hələ kitab yoxdur. EPUB, PDF və ya CBZ faylı əlavə et."; textSize=17f; setPadding(10,36,10,20)
        })
        for(b in books.reversed()) {
            val status=b.optString("status","Oxunmayıb"); val progress=b.optInt("progress",0)
            val card=LinearLayout(this).apply {
                orientation=LinearLayout.VERTICAL; setPadding(18,14,18,14); setBackgroundColor(Color.WHITE)
            }
            val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
            val name=TextView(this).apply {
                text="${if(b.optString("type")=="pdf") "📄" else if(b.optString("type")=="cbz") "🗯️" else "📖"}  ${b.optString("title")}"
                textSize=17f; setTextColor(Color.DKGRAY); setTypeface(null,1)
                layoutParams=LinearLayout.LayoutParams(0,-2,1f)
            }
            row.addView(name); row.addView(action("⋮") { options(b) }); card.addView(row)
            card.addView(TextView(this).apply {
                text="$status • $progress%" + if(b.optString("completedAt").isNotEmpty()) " • ✓ "+b.optString("completedAt") else ""; setTextColor(teal); textSize=14f; setPadding(4,8,0,8)
            })
            card.addView(ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply {
                max=100; this.progress=progress
            })
            card.setOnClickListener { openBook(b) }
            list.addView(card, LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=12 })
        }
    }
    private fun options(b: JSONObject) {
        val choices=arrayOf("Oxunmayıb","Oxunur","Bitirilib","Yarımçıq saxlanılıb","Sil")
        AlertDialog.Builder(this).setTitle(b.optString("title")).setItems(choices) { _,n ->
            if(n==4) AlertDialog.Builder(this).setMessage("Kitab və oxu qeydi silinsin?")
                .setPositiveButton("Sil") { _,_ ->
                    File(filesDir,b.getString("path")).delete(); books.remove(b); save(); shelf()
                }.setNegativeButton("Ləğv et",null).show()
            else { if(n==2)markFinished(b) else b.put("status",choices[n]);save();shelf() }
        }.show()
    }
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(requestCode!=42 || resultCode!=RESULT_OK) return
        val uri=data?.data ?: return
        val title=contentResolver.query(uri,null,null,null,null)?.use { c ->
            if(c.moveToFirst()) {
                val col=c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(col>=0)c.getString(col) else null
            } else null
        } ?: "Book"
        val ext=title.substringAfterLast('.').lowercase()
        if(ext !in listOf("epub","pdf","cbz")) {
            Toast.makeText(this,"Yalnız EPUB, PDF və CBZ dəstəklənir",Toast.LENGTH_LONG).show(); return
        }
        try {
            val id="book_${System.currentTimeMillis()}.$ext"
            contentResolver.openInputStream(uri)!!.use { input ->
                FileOutputStream(File(filesDir,id)).use { input.copyTo(it) }
            }
            books.add(JSONObject().put("title",title).put("path",id).put("type",ext)
                .put("page",0).put("scrollFraction",0.0).put("progress",0).put("status","Oxunmayıb"))
            save(); shelf()
        } catch(e:Exception) { Toast.makeText(this,"İdxal xətası: ${e.message}",Toast.LENGTH_LONG).show() }
    }
    private fun openBook(b:JSONObject) {
        try {
            current=b; page=b.optInt("page",0)
            when(b.getString("type")) {
                "pdf" -> {
                    pdfDescriptor=ParcelFileDescriptor.open(File(filesDir,b.getString("path")),ParcelFileDescriptor.MODE_READ_ONLY)
                    pdf=android.graphics.pdf.PdfRenderer(pdfDescriptor!!); total=pdf!!.pageCount
                }
                "cbz" -> {
                    zip=ZipFile(File(filesDir,b.getString("path")))
                    imageEntries=zip!!.entries().asSequence().filter {
                        !it.isDirectory && it.name.substringAfterLast('.').lowercase() in listOf("png","jpg","jpeg","webp")
                    }.map { it.name }.sortedWith(compareBy { it.lowercase() }).toList()
                    total=imageEntries.size
                }
                "epub" -> {
                    epubChapters=extractEpub(File(filesDir,b.getString("path")),b.getString("path").substringBefore('.'))
                    total=epubChapters.size
                }
            }
            if(total==0) throw IllegalArgumentException("Oxuna bilən səhifə yoxdur")
            page=page.coerceIn(0,total-1)
            b.put("pages",total)
            if(b.optString("status")!="Bitirilib")b.put("status","Oxunur")
            bar(b.getString("title"),true)
            setFullScreen(true)
            readerContainer=FrameLayout(this); root.addView(readerContainer,LinearLayout.LayoutParams(-1,0,1f))
            val foot=LinearLayout(this).apply {
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(8,4,8,8)
            }
            foot.addView(action("◀") { navigate(-1) })
            controls=TextView(this).apply {
                gravity=Gravity.CENTER; setTextColor(Color.DKGRAY)
                layoutParams=LinearLayout.LayoutParams(0,-2,1f)
            }
            foot.addView(controls)
            foot.addView(action("▶") { navigate(1) }); root.addView(foot)
            footerView=foot
            readerBarsVisible=true
            drawPage()
            toggleBars()
        } catch(e:Exception) {
            Toast.makeText(this,"Açılmadı: ${e.message}",Toast.LENGTH_LONG).show(); closeReader(); shelf()
        }
    }
    private fun navigate(delta:Int) {
        if (current == null) return
        if (page + delta in 0 until total) {
            flushPosition()
            page += delta
            current?.put("page", page)
            current?.put("scrollFraction", if (delta < 0) 1.0 else 0.0)
            drawPage()
        } else if (delta > 0 && page == total - 1) {
            current?.let { markFinished(it) }
            flushPosition()
            controls?.text = "${page + 1} / $total   •   100%"
            Toast.makeText(this, "Bitirildi ✓", Toast.LENGTH_SHORT).show()
        }
    }
    private fun drawPage() {
        val book = current ?: return
        activeReaderView = null
        readerContainer?.removeAllViews()
        web?.destroy(); web = null
        readerBitmap?.recycle(); readerBitmap = null
        restoringPosition = true
        val fraction = book.optDouble("scrollFraction", 0.0)
        when (book.optString("type")) {
            "epub" -> {
                val w = object : WebView(this) {
                    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
                        super.onScrollChanged(l, t, oldl, oldt)
                        if (activeReaderView === this) {
                            val max = (contentHeight * scale - height).roundToInt().coerceAtLeast(0)
                            recordPosition(scrollFraction(this, max))
                        }
                    }
                }.apply {
                    settings.javaScriptEnabled = false
                    settings.allowFileAccess = true
                    settings.allowContentAccess = false
                    settings.builtInZoomControls = false
                    setBackgroundColor(Color.WHITE)
                }
                web = w; activeReaderView = w; readerContainer?.addView(w)
                val file = epubChapters[page]
                w.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        if (activeReaderView === view) restoreFraction(view, {
                            (view.contentHeight * view.scale - view.height).roundToInt()
                        }, fraction)
                    }
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = true
                }
                w.loadDataWithBaseURL(file.parentFile!!.toURI().toString(),
                    file.readText(Charsets.UTF_8), "application/xhtml+xml", "UTF-8", null)
            }
            else -> {
                val bitmap = if (book.optString("type") == "pdf") {
                    val p = pdf!!.openPage(page)
                    try {
                        val scale = min(2.5f, 1800f / p.width)
                        val output = Bitmap.createBitmap((p.width * scale).toInt().coerceAtLeast(1),
                            (p.height * scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
                        output.eraseColor(Color.WHITE)
                        p.render(output, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        output
                    } finally { p.close() }
                } else {
                    zip!!.getInputStream(zip!!.getEntry(imageEntries[page])).use { BitmapFactory.decodeStream(it) }
                        ?: throw IOException("Şəkil oxunmadı")
                }
                readerBitmap = bitmap
                val scroll = ScrollView(this).apply {
                    isFillViewport = true; isVerticalScrollBarEnabled = true
                }
                val image = ImageView(this).apply {
                    setImageBitmap(bitmap); adjustViewBounds = true; scaleType = ImageView.ScaleType.FIT_CENTER
                }
                scroll.addView(image, ViewGroup.LayoutParams(-1, -2))
                activeReaderView = scroll
                readerContainer?.addView(scroll, FrameLayout.LayoutParams(-1, -1))
                scroll.viewTreeObserver.addOnScrollChangedListener {
                    if (activeReaderView === scroll) {
                        recordPosition(scrollFraction(scroll, (scroll.getChildAt(0)?.height ?: 0) - scroll.height))
                    }
                }
                restoreFraction(scroll, { (scroll.getChildAt(0)?.height ?: 0) - scroll.height }, fraction)
            }
        }
        book.put("page", page)
        controls?.text = "${page + 1} / $total   •   ${book.optInt("progress", 0)}%"
    }
    private fun closeReader() {
        if(current==null)return
        flushPosition()
        setFullScreen(false)
        headerView=null;footerView=null
        activeReaderView = null
        web?.destroy(); web = null
        readerBitmap?.recycle(); readerBitmap = null
        pdf?.close(); pdf = null
        pdfDescriptor?.close(); pdfDescriptor = null
        zip?.close(); zip = null
        current = null
    }
    override fun onPause() { if (current != null) flushPosition(); super.onPause() }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { if(current!=null) { closeReader(); shelf() } else super.onBackPressed() }
    private fun extractEpub(source:File,id:String): List<File> {
        val dir=File(cacheDir,"epub_$id")
        if(dir.exists())dir.deleteRecursively()
        dir.mkdirs()
        ZipFile(source).use { archive ->
            archive.entries().asSequence().forEach { entry ->
                val output=File(dir,entry.name).canonicalFile
                if(!output.path.startsWith(dir.canonicalPath+File.separator)) throw IOException("Unsafe EPUB path")
                if(entry.isDirectory)output.mkdirs()
                else {
                    output.parentFile?.mkdirs()
                    archive.getInputStream(entry).use { input -> output.outputStream().use { input.copyTo(it) } }
                }
            }
        }
        val container=File(dir,"META-INF/container.xml")
        val containerXml=container.inputStream().use { parseXml(it) }
        val opfRelative=containerXml.firstOrNull { it.first=="rootfile" }?.second?.get("full-path")
            ?: throw IOException("EPUB OPF yoxdur")
        val opf=File(dir,opfRelative).canonicalFile
        if(!opf.path.startsWith(dir.canonicalPath+File.separator))throw IOException("Unsafe OPF path")
        val info=opf.inputStream().use { parseXml(it) }
        val manifest=info.filter { it.first=="item" }
            .associate { it.second["id"].orEmpty() to it.second["href"].orEmpty() }
        return info.filter { it.first=="itemref" }.mapNotNull { row ->
            val href=manifest[row.second["idref"]] ?: return@mapNotNull null
            val f=File(opf.parentFile,java.net.URLDecoder.decode(href.substringBefore('#'),"UTF-8")).canonicalFile
            if(f.path.startsWith(dir.canonicalPath+File.separator) && f.exists()
                && f.extension.lowercase() in listOf("xhtml","html","htm")) f else null
        }
    }
    private fun parseXml(input:InputStream): List<Pair<String,Map<String,String>>> {
        val parser=Xml.newPullParser()
        parser.setInput(input,"UTF-8")
        val result=mutableListOf<Pair<String,Map<String,String>>>()
        var event=parser.eventType
        while(event!=XmlPullParser.END_DOCUMENT) {
            if(event==XmlPullParser.START_TAG) {
                val attrs=mutableMapOf<String,String>()
                for(i in 0 until parser.attributeCount)
                    attrs[parser.getAttributeName(i)]=parser.getAttributeValue(i)
                result.add(parser.name.substringAfter(':') to attrs)
            }
            event=parser.next()
        }
        return result
    }
}
