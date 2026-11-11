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
    private var sideButtons: View? = null
    private var readerBarsVisible = true
    private var epubPageCount = 1
    private var epubPageIndex = 0
    private var epubReady = false
    private fun pagedEpub() = settings.getString("epubMode", "pages") == "pages"
    private var readingSince = 0L
    private fun accountReadingTime() {
        val book=current ?: return
        if(readingSince<=0L)return
        val elapsed=(android.os.SystemClock.elapsedRealtime()-readingSince).coerceAtLeast(0L)
        if(elapsed>0L && elapsed<12L*60*60*1000) {
            book.put("readingMs",book.optLong("readingMs",0L)+elapsed)
        }
        readingSince=0L
        save()
    }
    private val settings by lazy { getSharedPreferences("reader_settings", MODE_PRIVATE) }
    private fun setFullScreen(enabled: Boolean) {
        // Respect Android status and gesture bar safe areas even inside a book.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0
        if (::root.isInitialized && Build.VERSION.SDK_INT >= 35) root.requestApplyInsets()
    }
    private fun toggleBars() {
        readerBarsVisible = !readerBarsVisible
        headerView?.visibility = if(readerBarsVisible) View.VISIBLE else View.GONE
        footerView?.visibility = if(readerBarsVisible) View.VISIBLE else View.GONE
        sideButtons?.visibility = if(settings.getBoolean("sideButtons",true)) View.VISIBLE else View.GONE
    }
    private fun markFinished(book: JSONObject) {
        book.put("status", "Bitirilib")
        book.put("progress", 100)
        if(book.optString("completedAt").isEmpty()) book.put("completedAt",
            java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(java.util.Date()))
    }
    private fun scrollFraction(view: View, maxScroll: Int): Double =
        if (maxScroll <= 0) 0.0 else (view.scrollY.toDouble() / maxScroll).coerceIn(0.0, 1.0)

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
        updateReaderProgress()
        pendingSave?.let { saveHandler.removeCallbacks(it) }
        val task = Runnable { save(); pendingSave = null }
        if (immediate) task.run() else { pendingSave = task; saveHandler.postDelayed(task, 350) }
    }

    private fun updateReaderProgress() {
        val book=current ?:return
        val progress=book.optInt("progress",0)
        controls?.text=if(book.optString("type")=="epub" && pagedEpub())
            "Fəsil ${page+1}/$total · ${epubPageIndex+1}/$epubPageCount · $progress%"
            else "${page+1}/$total · $progress%"
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
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg())
            // Android 15 enforces edge-to-edge for targetSdk 35. Keep app navigation
            // above the system gesture/navigation area; reader stays immersive.
            if (Build.VERSION.SDK_INT >= 35) {
                setOnApplyWindowInsetsListener { view, insets ->
                    val safe = insets.getInsets(WindowInsets.Type.systemBars())
                    view.setPadding(safe.left, safe.top, safe.right, safe.bottom)
                    insets
                }
            }
        }
        setContentView(root)
        if (Build.VERSION.SDK_INT >= 35) root.requestApplyInsets()
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
    private fun isDark(): Boolean = settings.getString("theme","Light") in listOf("Dark","AMOLED")
    private fun bg():Int = when(settings.getString("theme","Light")) {
        "Dark" -> Color.rgb(24,28,35)
        "AMOLED" -> Color.BLACK
        "Sepia" -> Color.rgb(242,231,210)
        else -> Color.rgb(248,250,249)
    }
    private fun fg():Int = if(isDark()) Color.WHITE else Color.rgb(32,49,49)
    private fun panel():Int = if(isDark()) Color.rgb(42,48,56) else Color.WHITE
    private fun dp(value:Int) = (value*resources.displayMetrics.density).toInt()
    private fun addNav(active:String) {
        val nav=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER;setPadding(dp(6),dp(5),dp(6),dp(5));setBackgroundColor(panel()) }
        val entries=listOf("Kitabxana","Statistika","Ayarlar")
        entries.forEach { label ->
            val item=TextView(this).apply {
                text=if(active==label)"● "+label else label
                textSize=13f;gravity=Gravity.CENTER;setTextColor(if(active==label)teal else fg())
                setPadding(dp(4),dp(12),dp(4),dp(12))
                setOnClickListener { when(label) {
                    "Kitabxana" -> shelf()
                    "Statistika" -> statistics()
                    else -> settingsScreen()
                }}
            }
            nav.addView(item,LinearLayout.LayoutParams(0,-2,1f))
        }
        root.addView(nav)
    }
    private fun shelf() {
        closeReader()
        setFullScreen(false)
        bar("Leaf Reader")
        val addButton=action("＋ Kitab əlavə et") {
            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE);type="*/*"
                putExtra(Intent.EXTRA_MIME_TYPES,arrayOf("application/pdf","application/epub+zip","application/zip","application/x-cbz","application/octet-stream"))
            }
            startActivityForResult(intent,42)
        }
        root.addView(addButton)
        val search=EditText(this).apply {
            hint="Kitab axtar..."
            textSize=15f;setSingleLine(true);setTextColor(fg())
            setHintTextColor(Color.GRAY);setBackgroundColor(panel())
            setPadding(dp(14),dp(10),dp(14),dp(10))
        }
        root.addView(search,LinearLayout.LayoutParams(-1,-2).apply { setMargins(dp(12),0,dp(12),dp(8)) })
        val filters=Spinner(this)
        val opts=listOf("Hamısı","Oxunur","Oxunmayıb","Bitirilib","Yarımçıq saxlanılıb")
        filters.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,opts)
        root.addView(filters)
        val scroll=ScrollView(this)
        val list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(8),dp(12),dp(20)) }
        scroll.addView(list)
        root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        fun render() {
            list.removeAllViews()
            val filter=opts[filters.selectedItemPosition.coerceAtLeast(0)]
            val visible=books.asReversed().filter {
                it.optString("title").contains(search.text.toString(),ignoreCase=true) &&
                    (filter=="Hamısı" || it.optString("status","Oxunmayıb")==filter)
            }
            if(visible.isEmpty()) list.addView(TextView(this).apply {
                text="Uyğun kitab tapılmadı";textSize=17f;setTextColor(fg());setPadding(dp(8),dp(28),dp(8),dp(28))
            })
            visible.forEach { b ->
                val card=LinearLayout(this).apply {
                    orientation=LinearLayout.HORIZONTAL;setPadding(dp(12),dp(12),dp(12),dp(12))
                    setBackgroundColor(panel());elevation=dp(2).toFloat()
                }
                val cover=TextView(this).apply {
                    text="▤";textSize=42f;gravity=Gravity.CENTER
                    setTextColor(Color.WHITE);setBackgroundColor(teal)
                }
                val coverPath=b.optString("cover")
                val coverFile=File(filesDir,coverPath)
                if(coverPath.isNotBlank() && coverFile.exists()) {
                    val bitmap=BitmapFactory.decodeFile(coverFile.absolutePath)
                    val coverImage=ImageView(this).apply {
                        setImageBitmap(bitmap);scaleType=ImageView.ScaleType.CENTER_CROP
                    }
                    card.addView(coverImage,LinearLayout.LayoutParams(dp(68),dp(94)))
                } else card.addView(cover,LinearLayout.LayoutParams(dp(68),dp(94)))
                val detail=LinearLayout(this).apply {
                    orientation=LinearLayout.VERTICAL;setPadding(dp(14),0,dp(4),0)
                }
                val title=TextView(this).apply {
                    text=b.optString("title");textSize=16f;setTypeface(null,1)
                    setTextColor(fg());maxLines=2
                }
                detail.addView(title)
                detail.addView(TextView(this).apply {
                    val date=b.optString("completedAt")
                    text=b.optString("status","Oxunmayıb") + " · " + b.optInt("progress",0).toString() + "%" +
                        if(date.isNotBlank()) " · ✓ "+date else ""
                    textSize=12f;setTextColor(teal);setPadding(0,dp(6),0,dp(8))
                })
                detail.addView(ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply {
                    max=100;progress=b.optInt("progress",0)
                })
                card.addView(detail,LinearLayout.LayoutParams(0,-2,1f))
                card.addView(action("⋮") { options(b) })
                card.setOnClickListener { openBook(b) }
                list.addView(card,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(12) })
            }
        }
        search.addTextChangedListener(object:android.text.TextWatcher {
            override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
            override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){render()}
            override fun afterTextChanged(s:android.text.Editable?){}
        })
        filters.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent:android.widget.AdapterView<*>?){}
            override fun onItemSelected(parent:android.widget.AdapterView<*>?,view:View?,position:Int,id:Long){render()}
        }
        render()
        addNav("Kitabxana")
    }
    private fun statistics() {
        closeReader();setFullScreen(false);bar("Statistika")
        val completedBooks=books.filter { it.optString("status")=="Bitirilib" }
        val scroll=ScrollView(this)
        val content=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(14),dp(20),dp(20))
        }
        scroll.addView(content);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        listOf("Ümumi kitab sayı" to books.size.toString(),
            "Bitirilmiş kitablar" to completedBooks.size.toString(),
            "Hazırda oxunur" to books.count { it.optString("status")=="Oxunur" }.toString(),
            "Bitirilmiş kitabların səhifələri" to completedBooks.sumOf {it.optInt("pages",0)}.toString(),
            "Oxuma müddəti (dəqiqə)" to (books.sumOf {it.optLong("readingMs",0L)}/60000L).toString()).forEach { (label,value) ->
            val card=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(14));setBackgroundColor(panel()) }
            card.addView(TextView(this).apply {text=label;textSize=13f;setTextColor(fg())})
            card.addView(TextView(this).apply {text=value;textSize=28f;setTextColor(teal);setTypeface(null,1)})
            content.addView(card,LinearLayout.LayoutParams(-1,-2).apply {bottomMargin=dp(10)})
        }
        val goal=settings.getInt("annualGoal",24)
        val currentYear=java.text.SimpleDateFormat("yyyy",java.util.Locale.US).format(java.util.Date())
        val yearDone=completedBooks.count {it.optString("completedAt").startsWith(currentYear)}
        content.addView(TextView(this).apply {
            text="İllik hədəf: "+yearDone+" / "+goal+" kitab"
            textSize=18f;setTextColor(fg());setPadding(0,dp(10),0,dp(10))
        })
        content.addView(ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply {max=goal;progress=yearDone.coerceAtMost(goal)})
        content.addView(TextView(this).apply {text="Aylıq bitirilən kitablar";textSize=20f;setTextColor(fg());setPadding(0,dp(18),0,dp(12))})
        val months=completedBooks.filter {it.optString("completedAt").length>=7}
            .groupBy {it.optString("completedAt").substring(0,7)}.toSortedMap()
        if(months.isEmpty())content.addView(TextView(this).apply {text="Hələ tarixçə yoxdur";setTextColor(fg())})
        months.forEach { (month,entries) ->
            content.addView(TextView(this).apply {text=month+"  ·  "+entries.size+" kitab";textSize=16f;setTextColor(fg());setPadding(0,dp(8),0,dp(5))})
            content.addView(ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal).apply {max=books.size.coerceAtLeast(1);progress=entries.size})
        }
        addNav("Statistika")
    }
    private fun settingsScreen() {
        closeReader();setFullScreen(false);bar("Ayarlar")
        val content=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(20),dp(18),dp(20))}
        root.addView(content,LinearLayout.LayoutParams(-1,0,1f))
        content.addView(TextView(this).apply {text="Oxu teması";textSize=19f;setTextColor(fg())})
        val themes=listOf("Light","Dark","Sepia","AMOLED")
        val spinner=Spinner(this).apply {adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,themes);setSelection(themes.indexOf(settings.getString("theme","Light")))}
        content.addView(spinner)
        content.addView(action("Temanı tətbiq et") {settings.edit().putString("theme",themes[spinner.selectedItemPosition]).apply();settingsScreen()})
        content.addView(TextView(this).apply {text="EPUB şrift ölçüsü";textSize=18f;setTextColor(fg());setPadding(0,dp(14),0,dp(8))})
        val fontSlider=SeekBar(this).apply {
            max=18;progress=(settings.getInt("fontSize",18)-14).coerceIn(0,18)
            setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar:SeekBar?,progress:Int,fromUser:Boolean) {
                    if(fromUser)settings.edit().putInt("fontSize",progress+14).apply()
                }
                override fun onStartTrackingTouch(bar:SeekBar?) {}
                override fun onStopTrackingTouch(bar:SeekBar?) {}
            })
        }
        content.addView(fontSlider)
        content.addView(CheckBox(this).apply {
            text="PDF/CBZ: Fit to Width";setTextColor(fg())
            isChecked=settings.getBoolean("fitWidth",false)
            setOnCheckedChangeListener {_,value->settings.edit().putBoolean("fitWidth",value).apply()}
        })
        content.addView(TextView(this).apply {text="İllik kitab hədəfi";textSize=18f;setTextColor(fg());setPadding(0,dp(18),0,0)})
        val goal=EditText(this).apply {
            inputType=android.text.InputType.TYPE_CLASS_NUMBER
            setText(settings.getInt("annualGoal",24).toString());setTextColor(fg())
        }
        content.addView(goal)
        content.addView(action("Hədəfi saxla") {
            settings.edit().putInt("annualGoal",goal.text.toString().toIntOrNull()?.coerceIn(1,999) ?:24).apply()
            Toast.makeText(this,"Hədəf saxlanıldı",Toast.LENGTH_SHORT).show()
        })
        content.addView(CheckBox(this).apply {
            text="CBZ: Sağdan-sola səhifə çevirmə"
            setTextColor(fg());isChecked=settings.getBoolean("cbzRtl",false)
            setOnCheckedChangeListener {_,checked->settings.edit().putBoolean("cbzRtl",checked).apply()}
        })
        content.addView(action("JSON məlumatları export et") {jsonExport()})
        content.addView(action("JSON məlumatları import et") {jsonImport()})
        addNav("Ayarlar")
    }
    private fun jsonExport() {
        startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE);type="application/json"
            putExtra(Intent.EXTRA_TITLE,"leaf-reader-backup.json")
        },81)
    }
    private fun jsonImport() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE);type="application/json"
        },82)
    }
    private fun options(b: JSONObject) {
        val choices=arrayOf("Oxunmayıb","Oxunur","Bitirilib","Yarımçıq saxlanılıb","Şəxsi qeyd","Oxuma tarixçəsi","Sil")
        AlertDialog.Builder(this).setTitle(b.optString("title")).setItems(choices) { _,n ->
            if(n==6) AlertDialog.Builder(this).setMessage("Kitab və oxu qeydi silinsin?")
                .setPositiveButton("Sil") { _,_ ->
                    File(filesDir,b.getString("path")).delete(); books.remove(b); save(); shelf()
                }.setNegativeButton("Ləğv et",null).show()
            else if(n==4)editNote(b)
            else if(n==5)showHistory(b)
            else { if(n==2)markFinished(b) else b.put("status",choices[n]);save();shelf() }
        }.show()
    }
    private fun showHistory(book:JSONObject) {
        val history=book.optJSONArray("readingHistory") ?: JSONArray()
        val items=(0 until history.length()).map { index ->
            val entry=history.optJSONObject(index)
            (entry?.optString("date") ?: "")+"  ·  səhifə "+((entry?.optInt("page") ?: 0)+1)
        }.takeLast(30).reversed().toTypedArray()
        AlertDialog.Builder(this).setTitle("Oxuma tarixçəsi")
            .setItems(if(items.isEmpty())arrayOf("Hələ tarixçə yoxdur") else items) {_,_->}
            .setPositiveButton("Bağla",null).show()
    }
    private fun editNote(book:JSONObject) {
        val editor=EditText(this).apply {
            minLines=4;setText(book.optString("note"));hint="Bu kitab haqqında qeydlərin"
        }
        AlertDialog.Builder(this).setTitle("Şəxsi qeyd")
            .setView(editor)
            .setPositiveButton("Saxla") {_,_->
                book.put("note",editor.text.toString());save()
            }.setNegativeButton("Ləğv et",null).show()
    }
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(resultCode!=RESULT_OK)return
        if(requestCode==81) {
            data?.data?.let { uri ->
                try {
                    contentResolver.openOutputStream(uri)?.use { stream ->
                        val arr=JSONArray()
                        books.forEach {arr.put(it)}
                        stream.write(JSONObject().put("schemaVersion",2).put("books",arr).toString(2).toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(this,"JSON backup yaradıldı",Toast.LENGTH_LONG).show()
                }catch(e:Exception){Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()}
            }
            return
        }
        if(requestCode==82) {
            data?.data?.let { uri ->
                try {
                    val json=contentResolver.openInputStream(uri)!!.bufferedReader().use {it.readText()}
                    val arr=JSONObject(json).getJSONArray("books")
                    val known=books.map {it.optString("path")}.toMutableSet()
                    for(i in 0 until arr.length()) {
                        val book=arr.getJSONObject(i)
                        val id=book.optString("path")
                        if(id !in known && File(filesDir,id).exists()) {
                            books.add(book);known.add(id)
                        }
                    }
                    save();shelf()
                    Toast.makeText(this,"Qeydlər import edildi. Fayl özü ayrıca tələb olunur.",Toast.LENGTH_LONG).show()
                }catch(e:Exception){Toast.makeText(this,"JSON xətası: "+e.message,Toast.LENGTH_LONG).show()}
            }
            return
        }
        if(requestCode!=42)return
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
            val item=JSONObject().put("title",title).put("path",id).put("type",ext)
                .put("page",0).put("scrollFraction",0.0).put("progress",0).put("status","Oxunmayıb")
            CoverExtractor.extract(this,File(filesDir,id),ext,id)?.let {item.put("cover",it)}
            books.add(item)
            save(); shelf()
        } catch(e:Exception) { Toast.makeText(this,"İdxal xətası: ${e.message}",Toast.LENGTH_LONG).show() }
    }
    private fun openBook(b:JSONObject) {
        try {
            current=b; page=b.optInt("page",0)
            readingSince=android.os.SystemClock.elapsedRealtime()
            if(b.optString("startedAt").isEmpty()) b.put("startedAt",
                java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(java.util.Date()))
            b.put("lastOpenedAt",java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",java.util.Locale.US).format(java.util.Date()))
            val history=b.optJSONArray("readingHistory") ?: JSONArray()
            history.put(JSONObject().put("date",b.optString("lastOpenedAt")).put("page",page))
            b.put("readingHistory",history)
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
            (headerView as? LinearLayout)?.addView(action("Aa ⚙") { readerOptions() })
            readerContainer=FrameLayout(this); root.addView(readerContainer,LinearLayout.LayoutParams(-1,0,1f))
            val foot=LinearLayout(this).apply {
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(8,4,8,8)
            }
            foot.addView(action("◀") { readerTurn(-1) })
            foot.addView(action("☆") { bookmark() })
            controls=TextView(this).apply {
                gravity=Gravity.CENTER; setTextColor(Color.DKGRAY)
                layoutParams=LinearLayout.LayoutParams(0,-2,1f)
            }
            foot.addView(controls)
            foot.addView(action("☷") { bookmarkList() })
            foot.addView(action("▶") { readerTurn(1) }); root.addView(foot)
            footerView=foot
            readerBarsVisible=true
            drawPage()
            toggleBars()
        } catch(e:Exception) {
            Toast.makeText(this,"Açılmadı: ${e.message}",Toast.LENGTH_LONG).show(); closeReader(); shelf()
        }
    }
    private fun attachSideButtons() {
        val host=readerContainer ?:return
        val overlay=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
        }
        fun edge(label:String,dir:Int)=TextView(this).apply {
            text=label;textSize=24f;gravity=Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(155,26,63,66))
            setOnClickListener { readerTurn(dir) }
        }
        overlay.addView(edge("‹",-1),LinearLayout.LayoutParams(dp(42),dp(72)))
        overlay.addView(View(this),LinearLayout.LayoutParams(0,1,1f))
        overlay.addView(edge("›",1),LinearLayout.LayoutParams(dp(42),dp(72)))
        host.addView(overlay,FrameLayout.LayoutParams(-1,-2,Gravity.CENTER_VERTICAL))
        sideButtons=overlay
        overlay.visibility=if(settings.getBoolean("sideButtons",true))View.VISIBLE else View.GONE
    }
    private fun readerTurn(direction:Int) {
        if(current?.optString("type")=="epub") {
            web?.let { view ->
                if(!epubReady)return
                if(pagedEpub()) {
                    val max=epubMaxX(view)
                    val step=view.width.coerceAtLeast(1)
                    val currentPage=(view.scrollX.toFloat()/step).roundToInt()
                    if(direction>0 && currentPage<epubPageCount-1) {
                        val target=((currentPage+1)*step).coerceAtMost(max)
                        view.scrollTo(target,0)
                        onEpubScrolled(view)
                        return
                    }
                    if(direction<0 && currentPage>0) {
                        view.scrollTo(((currentPage-1)*step).coerceAtLeast(0),0)
                        onEpubScrolled(view)
                        return
                    }
                } else {
                    val max=(view.contentHeight*view.scale-view.height).toInt().coerceAtLeast(0)
                    val step=(view.height*0.87f).toInt().coerceAtLeast(1)
                    if(direction>0 && view.scrollY<max-8) {
                        view.scrollTo(0,(view.scrollY+step).coerceAtMost(max))
                        return
                    }
                    if(direction<0 && view.scrollY>8) {
                        view.scrollTo(0,(view.scrollY-step).coerceAtLeast(0))
                        return
                    }
                }
            }
        }
        val rtl=current?.optString("type")=="cbz" && settings.getBoolean("cbzRtl",false)
        navigate(if(rtl)-direction else direction)
    }
    private fun epubMaxX(view:WebView):Int =
        if(view is EpubChapterView) view.maxHorizontalScroll() else 0
    private fun onEpubScrolled(view:WebView) {
        if(!pagedEpub())return
        val step=view.width.coerceAtLeast(1)
        epubPageCount=(epubMaxX(view)/step)+1
        epubPageIndex=(view.scrollX.toFloat()/step).roundToInt().coerceIn(0,epubPageCount-1)
        val fraction=if(epubPageCount<=1)0.0
            else epubPageIndex.toDouble()/(epubPageCount-1)
        recordPosition(fraction)
        updateReaderProgress()
    }
    private fun readerOptions() {
        val book=current ?:return
        val content=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            setPadding(dp(18),dp(8),dp(18),dp(8))
        }
        val themeChoices=listOf("Light","Dark","Sepia","AMOLED")
        content.addView(TextView(this).apply {text="Oxuma teması";textSize=17f})
        val picker=Spinner(this).apply {
            adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,themeChoices)
            setSelection(themeChoices.indexOf(settings.getString("theme","Light")).coerceAtLeast(0))
        }
        content.addView(picker)
        var fontValue=settings.getInt("fontSize",18)
        var lineValue=settings.getInt("lineSpacing",160)
        if(book.optString("type")=="epub") {
            val fontLabel=TextView(this).apply {text="Şrift ölçüsü: $fontValue px";setPadding(0,dp(12),0,0)}
            content.addView(fontLabel)
            content.addView(SeekBar(this).apply {
                max=22;progress=(fontValue-12).coerceIn(0,22)
                setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(bar:SeekBar?,progress:Int,fromUser:Boolean) {
                        fontValue=12+progress;fontLabel.text="Şrift ölçüsü: $fontValue px"
                    }
                    override fun onStartTrackingTouch(bar:SeekBar?) {}
                    override fun onStopTrackingTouch(bar:SeekBar?) {}
                })
            })
            val lineLabel=TextView(this).apply {text="Sətirarası məsafə: $lineValue%";setPadding(0,dp(12),0,0)}
            content.addView(lineLabel)
            content.addView(SeekBar(this).apply {
                max=100;progress=(lineValue-110).coerceIn(0,100)
                setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(bar:SeekBar?,progress:Int,fromUser:Boolean) {
                        lineValue=110+progress;lineLabel.text="Sətirarası məsafə: $lineValue%"
                    }
                    override fun onStartTrackingTouch(bar:SeekBar?) {}
                    override fun onStopTrackingTouch(bar:SeekBar?) {}
                })
            })
        }
        val arrows=CheckBox(this).apply {
            text="Ekranın sağ və solunda vərəqləmə düymələri"
            isChecked=settings.getBoolean("sideButtons",true)
        }
        content.addView(arrows)
        val swipe=CheckBox(this).apply {
            text="Swipe ilə səhifə çevirmə"
            isChecked=settings.getBoolean("swipeTurn",false)
        }
        content.addView(swipe)
        val fit=CheckBox(this).apply {
            text="PDF/CBZ: Fit to Width"
            isChecked=settings.getBoolean("fitWidth",false)
            visibility=if(book.optString("type")=="epub")View.GONE else View.VISIBLE
        }
        content.addView(fit)
        val dialog=AlertDialog.Builder(this).setTitle("Oxuyucu ayarları").setView(content)
            .setPositiveButton("Tətbiq et") {_,_->
                settings.edit()
                    .putString("theme",themeChoices[picker.selectedItemPosition])
                    .putInt("fontSize",fontValue)
                    .putInt("lineSpacing",lineValue)
                    .putBoolean("sideButtons",arrows.isChecked)
                    .putBoolean("swipeTurn",swipe.isChecked)
                    .putBoolean("fitWidth",fit.isChecked).apply()
                if(book.optString("type")=="epub") {
                    web?.let { view ->
                        val max=(view.contentHeight*view.scale-view.height).toInt().coerceAtLeast(0)
                        book.put("scrollFraction",scrollFraction(view,max))
                    }
                }
                root.setBackgroundColor(bg())
                drawPage()
                sideButtons?.visibility=if(arrows.isChecked)View.VISIBLE else View.GONE
                if(!readerBarsVisible)toggleBars()
            }.setNegativeButton("Bağla",null).create()
        dialog.show()
    }
    private fun bookmark() {
        val b=current ?:return
        val marks=b.optJSONArray("bookmarks") ?: JSONArray()
        if((0 until marks.length()).none { marks.optInt(it)==page })marks.put(page)
        b.put("bookmarks",marks);save()
        Toast.makeText(this,"Əlfəcin saxlanıldı",Toast.LENGTH_SHORT).show()
    }
    private fun bookmarkList() {
        val marks=current?.optJSONArray("bookmarks") ?: JSONArray()
        if(marks.length()==0) {
            Toast.makeText(this,"Əlfəcin yoxdur",Toast.LENGTH_SHORT).show()
            return
        }
        val items=(0 until marks.length()).map {"Səhifə "+(marks.optInt(it)+1)}.toTypedArray()
        AlertDialog.Builder(this).setTitle("Əlfəcinlər").setItems(items) {_,selected->
            page=marks.optInt(selected)
            current?.put("scrollFraction",0.0)
            drawPage()
            recordPosition(0.0,true)
        }.setNegativeButton("Bağla",null).show()
    }
    private fun navigate(delta:Int) {
        if (current == null) return
        if (page + delta in 0 until total) {
            flushPosition()
            page += delta
            current?.put("page", page)
            current?.put("scrollFraction", if (delta < 0) 1.0 else 0.0)
            drawPage()
            recordPosition(0.0,true)
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
                epubReady=false
                epubPageCount=1
                epubPageIndex=0
                val w=EpubChapterView(this) { view ->
                    if(activeReaderView === view && !restoringPosition && epubReady) {
                        if(pagedEpub())onEpubScrolled(view)
                        else {
                            val max=(view.contentHeight*view.scale-view.height).roundToInt().coerceAtLeast(0)
                            recordPosition(scrollFraction(view,max))
                        }
                    }
                }.apply {
                    settings.javaScriptEnabled=false
                    settings.allowFileAccess=true
                    settings.allowFileAccessFromFileURLs=false
                    settings.allowUniversalAccessFromFileURLs=false
                    settings.allowContentAccess=false
                    settings.builtInZoomControls=false
                    isHorizontalScrollBarEnabled=false
                    isVerticalScrollBarEnabled=!pagedEpub()
                    setBackgroundColor(Color.WHITE)
                }
                web=w;activeReaderView=w
                readerContainer?.addView(w)
                val file=epubChapters[page]
                w.webViewClient=object:WebViewClient() {
                    override fun onPageFinished(view:WebView,url:String?) {
                        if(activeReaderView!==view)return
                        // Recalculate after fonts and images settle; preserve chapter ratio.
                        intArrayOf(220,650).forEachIndexed { index,delay ->
                            view.postDelayed({
                                if(activeReaderView!==view)return@postDelayed
                                if(pagedEpub()) {
                                    val width=view.width.coerceAtLeast(1)
                                    val max=epubMaxX(view)
                                    epubPageCount=max(1,(max+width-1)/width+1)
                                    epubPageIndex=(fraction*(epubPageCount-1)).roundToInt()
                                        .coerceIn(0,epubPageCount-1)
                                    view.scrollTo((epubPageIndex*width).coerceAtMost(max),0)
                                    if(index==1) {
                                        restoringPosition=false;epubReady=true
                                        onEpubScrolled(view)
                                        recordPosition(if(epubPageCount<=1)0.0 else
                                            epubPageIndex.toDouble()/(epubPageCount-1),true)
                                    }
                                } else if(index==1) {
                                    val maxScroll=(view.contentHeight*view.scale-view.height)
                                        .roundToInt().coerceAtLeast(0)
                                    view.scrollTo(0,(fraction*maxScroll).roundToInt())
                                    restoringPosition=false;epubReady=true
                                    recordPosition(scrollFraction(view,maxScroll),true)
                                }
                                updateReaderProgress()
                            },delay.toLong())
                        }
                    }
                    override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean=true
                    override fun shouldInterceptRequest(view:WebView,request:WebResourceRequest):WebResourceResponse? {
                        val scheme=request.url.scheme?.lowercase()
                        return if(scheme=="http"||scheme=="https")
                            WebResourceResponse("text/plain","UTF-8",java.io.ByteArrayInputStream(ByteArray(0)))
                        else super.shouldInterceptRequest(view,request)
                    }
                }
                var touchDownX=0f;var touchDownY=0f
                w.setOnTouchListener { _,event ->
                    when(event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {touchDownX=event.x;touchDownY=event.y}
                        MotionEvent.ACTION_UP -> {
                            val dx=event.x-touchDownX
                            val dy=event.y-touchDownY
                            if(settings.getBoolean("swipeTurn",false) &&
                                abs(dx)>dp(75) && abs(dx)>abs(dy)*1.4f) {
                                readerTurn(if(dx<0)1 else -1)
                                return@setOnTouchListener true
                            }
                            if(abs(dx)<dp(8) && abs(dy)<dp(8))toggleBars()
                        }
                    }
                    false
                }
                val theme=settings.getString("theme","Light") ?: "Light"
                val background=when(theme) {
                    "Dark"->"#171B22";"AMOLED"->"#000000";"Sepia"->"#F2E6CF"
                    else->"#FFFFFF"
                }
                val foreground=if(theme in listOf("Dark","AMOLED"))"#F5F5F5" else "#292F30"
                val fontSize=settings.getInt("fontSize",18)
                val lineHeight=settings.getInt("lineSpacing",160)/100.0
                val columns=if(pagedEpub()) {
                    "html{height:100%!important;width:100%!important;overflow-x:auto!important;overflow-y:hidden!important;}"+
                    "body{width:100vw!important;height:100vh!important;column-width:calc(100vw - 36px)!important;"+
                    "column-gap:36px!important;column-fill:auto!important;overflow:visible!important;}"
                } else "html{overflow-x:hidden!important;overflow-y:auto!important;}body{width:100%!important;}"
                val css="<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,user-scalable=no\"/>"+
                    "<style>html{margin:0!important;padding:0!important;}"+
                    "body{background:"+background+"!important;color:"+foreground+"!important;"+
                    "font-size:"+fontSize+"px!important;line-height:"+lineHeight+"!important;"+
                    "margin:0!important;padding:18px!important;box-sizing:border-box!important;}"+
                    "img,svg{max-width:100%!important;max-height:85vh!important;height:auto!important;}"+
                    columns+"</style>"
                val original=file.readText(Charsets.UTF_8)
                val decorated=if(original.contains("</head>",true))
                    original.replace("</head>",css+"</head>",ignoreCase=true)
                else css+original
                w.setBackgroundColor(Color.parseColor(background))
                w.loadDataWithBaseURL(file.parentFile!!.toURI().toString(),
                    decorated,"application/xhtml+xml","UTF-8",null)
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
                val viewer = ZoomPageView(this,
                    onNext={if(settings.getBoolean("swipeTurn",false))readerTurn(1)},
                    onPrevious={if(settings.getBoolean("swipeTurn",false))readerTurn(-1)},
                    onToggleControls={toggleBars()},
                    onZoomChanged={ value -> book.put("zoom",value.toDouble());save() })
                activeReaderView = viewer
                readerContainer?.addView(viewer,FrameLayout.LayoutParams(-1,-1))
                viewer.setPage(bitmap,book.optDouble("zoom",1.0).toFloat(),settings.getBoolean("fitWidth",false))
                restoringPosition = false
            }
        }
        attachSideButtons()
        book.put("page", page)
        controls?.text = "${page + 1} / $total   •   ${book.optInt("progress", 0)}%"
    }
    private fun closeReader() {
        if(current==null)return
        accountReadingTime()
        flushPosition()
        setFullScreen(false)
        headerView=null;footerView=null;sideButtons=null
        activeReaderView = null
        web?.destroy(); web = null
        readerBitmap?.recycle(); readerBitmap = null
        pdf?.close(); pdf = null
        pdfDescriptor?.close(); pdfDescriptor = null
        zip?.close(); zip = null
        current = null
    }
    override fun onPause() {
        if(current!=null) { accountReadingTime();flushPosition() }
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        if(current!=null && readingSince==0L)readingSince=android.os.SystemClock.elapsedRealtime()
    }
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
