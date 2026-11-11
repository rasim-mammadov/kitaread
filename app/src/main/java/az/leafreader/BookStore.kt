package az.leafreader

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import android.util.Xml
import org.xmlpull.v1.XmlPullParser

data class ReaderBook(
    val path: String,
    val title: String,
    val type: String,
    val status: String,
    val progress: Int,
    val cover: String,
    val chapter: Int,
    val fraction: Double,
    val completedAt: String
)

class BookStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("library", Context.MODE_PRIVATE)
    private val readerSettings = context.getSharedPreferences("leaf_v3_options",Context.MODE_PRIVATE)
    fun setting()=readerSettings

    fun records(): List<JSONObject> {
        val array=try { JSONArray(prefs.getString("items","[]")) } catch(_:Exception){ JSONArray() }
        return (0 until array.length()).mapNotNull { array.optJSONObject(it) }
    }
    fun books():List<ReaderBook> = records().map { b->
        ReaderBook(b.optString("path"), b.optString("title"), b.optString("type"),
            b.optString("status","Oxunmayıb"), b.optInt("progress",0),b.optString("cover"),
            b.optInt("page",0),b.optDouble("scrollFraction",0.0),b.optString("completedAt"))
    }
    fun find(path:String):JSONObject? = records().firstOrNull { it.optString("path")==path }
    fun change(path:String, action:(JSONObject)->Unit) {
        val items=records()
        items.firstOrNull {it.optString("path")==path}?.let(action)
        persist(items)
    }
    private fun persist(items:List<JSONObject>) {
        val arr=JSONArray()
        items.forEach {arr.put(it)}
        prefs.edit().putString("items",arr.toString()).apply()
    }
    fun hideBook(path:String,hidden:Boolean) {
        change(path) {it.put("hidden",hidden)}
    }
    /**
     * Remove only the library record by default, without deleting book bytes.
     * Full deletion requires explicit confirmation in the interface.
     */
    fun removeBook(path:String,deleteFiles:Boolean) {
        val items=records()
        val target=items.firstOrNull {it.optString("path")==path} ?:return
        if(deleteFiles) {
            val root=context.filesDir.canonicalFile
            val main=File(root,target.optString("path")).canonicalFile
            require(main.parentFile==root){"Fayl yolu etibarsızdır"}
            if(main.isFile && !main.delete())
                error("Kitab faylı silinmədi. Kitabxana qeydi qorundu.")
            val cover=target.optString("cover")
            if(cover.isNotBlank()) {
                val coverFile=File(root,cover).canonicalFile
                if(coverFile.parentFile==root && coverFile.isFile)coverFile.delete()
            }
            val cache=File(context.cacheDir,"epub_v3_"+path.substringBeforeLast('.')).canonicalFile
            if(cache.parentFile==context.cacheDir.canonicalFile)cache.deleteRecursively()
        }
        persist(items.filterNot {it.optString("path")==path})
    }
    fun lastReadSortTime(book:JSONObject):Long {
        val exact=book.optLong("lastReadTimestamp",0L)
        if(exact>0)return exact
        val date=book.optString("lastReadAt")
        return try {
            java.time.LocalDate.parse(date).atStartOfDay(
                java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        }catch(_:Exception){0L}
    }
    /** Sorting applies to the entire filtered list; status is a filter, not a sort group. */
    fun sortBooks(values:List<ReaderBook>,kind:String="Son oxunanlar"):List<ReaderBook> {
        val data=records().associateBy {it.optString("path")}
        fun added(b:ReaderBook):Long {
            val item=data[b.path]
            val stored=item?.optLong("addedAt",0L) ?:0L
            if(stored>0L)return stored
            // Legacy imports used book_<timestamp>.ext, which is safe to parse
            // for sorting without altering existing library records.
            return b.path.substringAfter("book_","").substringBefore('.').toLongOrNull() ?:0L
        }
        val byTitle=compareBy<ReaderBook> {it.title.lowercase(java.util.Locale.ROOT)}
            .thenBy {it.path}
        return when(kind) {
            "A–Z" -> values.sortedWith(byTitle)
            "Z–A" -> values.sortedWith(
                compareByDescending<ReaderBook> {it.title.lowercase(java.util.Locale.ROOT)}
                    .thenBy {it.path})
            "Progress" -> values.sortedWith(compareByDescending<ReaderBook> {it.progress}
                .then(byTitle))
            "Əlavə edilmə tarixi" -> values.sortedWith(
                compareByDescending<ReaderBook> {added(it)}.then(byTitle))
            else -> values.sortedWith(
                compareByDescending<ReaderBook> {
                    lastReadSortTime(data[it.path] ?:JSONObject())
                }.thenByDescending {added(it)}.then(byTitle))
        }
    }

    data class RestoreResult(val imported:Int,val existing:Int,val missingFiles:Int)

    /** Metadata only: book files are NOT included in this JSON. */
    fun exportMetadata(): String {
        val data=JSONArray()
        records().forEach { data.put(JSONObject(it.toString())) }
        val settingsJson=JSONObject()
        readerSettings.all.forEach { (key,value)->
            when(value) {
                is Boolean,is Int,is Long,is Float,is String -> settingsJson.put(key,value)
            }
        }
        return JSONObject()
            .put("schemaVersion",3)
            .put("kind","leaf-reader-metadata")
            .put("includesBookFiles",false)
            .put("books",data)
            .put("readerSettings",settingsJson)
            .toString(2)
    }

    /**
     * Non-destructive restore. Existing books and options take precedence.
     * No untrusted paths outside the app's private files directory are accepted.
     */
    fun restoreMetadata(json:String):RestoreResult {
        val backup=JSONObject(json)
        require(backup.optString("kind")=="leaf-reader-metadata") {
            "Bu fayl Leaf Reader metadata backup formatında deyil"
        }
        require(backup.optInt("schemaVersion",0) in 1..3) {
            "Dəstəklənməyən backup versiyası"
        }
        val importedBooks=backup.getJSONArray("books")
        require(importedBooks.length() <= 20000){"Həddindən artıq böyük backup"}
        val current=records().toMutableList()
        val present=current.map {it.optString("path")}.toMutableSet()
        var added=0;var already=0;var missing=0
        val home=context.filesDir.canonicalFile
        for(i in 0 until importedBooks.length()) {
            val item=importedBooks.optJSONObject(i) ?:continue
            val path=item.optString("path")
            if(path in present) {already++;continue}
            val file=File(home,path).canonicalFile
            if(path.isBlank()||file.parentFile!=home||!file.isFile ||
                file.extension.lowercase() !in setOf("epub","pdf","cbz","md","markdown")) {
                missing++
                continue
            }
            current.add(JSONObject(item.toString()))
            present.add(path)
            added++
        }
        val backupSettings=backup.optJSONObject("readerSettings")
        if(backupSettings!=null) {
            val changes=readerSettings.edit()
            val keys=backupSettings.keys()
            while(keys.hasNext()) {
                val key=keys.next()
                if(readerSettings.contains(key))continue
                when(val value=backupSettings.opt(key)) {
                    is Boolean -> changes.putBoolean(key,value)
                    is Int -> changes.putInt(key,value)
                    is Long -> changes.putLong(key,value)
                    is Double -> changes.putFloat(key,value.toFloat())
                    is String -> changes.putString(key,value)
                }
            }
            changes.apply()
        }
        if(added>0)persist(current)
        return RestoreResult(added,already,missing)
    }

    data class BookMark(val chapter:Int,val fraction:Double,val createdAt:Long)

    /** Supports legacy integer bookmarks from v1/v2. */
    fun bookmarks(path:String):List<BookMark> {
        val arr=find(path)?.optJSONArray("bookmarks") ?:return emptyList()
        return (0 until arr.length()).mapNotNull {i->
            when(val item=arr.opt(i)) {
                is JSONObject -> BookMark(item.optInt("chapter",0).coerceAtLeast(0),
                    item.optDouble("fraction",0.0).coerceIn(0.0,1.0),
                    item.optLong("createdAt",0L))
                is Number -> BookMark(item.toInt().coerceAtLeast(0),0.0,0L)
                else -> null
            }
        }
    }
    fun hasBookmark(path:String,chapter:Int,fraction:Double):Boolean =
        bookmarks(path).any {
            it.chapter==chapter && kotlin.math.abs(it.fraction-fraction)<0.015
        }
    fun toggleBookmark(path:String,chapter:Int,fraction:Double):Boolean {
        val point=fraction.coerceIn(0.0,1.0)
        var added=false
        change(path) { book->
            val previous=bookmarks(path)
            val exists=previous.any {it.chapter==chapter &&
                kotlin.math.abs(it.fraction-point)<0.015}
            val changed=if(exists)previous.filterNot {it.chapter==chapter &&
                kotlin.math.abs(it.fraction-point)<0.015}
            else previous+BookMark(chapter,point,System.currentTimeMillis())
            added=!exists
            val arr=JSONArray()
            changed.forEach {arr.put(JSONObject()
                .put("chapter",it.chapter).put("fraction",it.fraction)
                .put("createdAt",it.createdAt))}
            book.put("bookmarks",arr)
        }
        return added
    }
    fun removeBookmark(path:String,mark:BookMark) {
        change(path) { book->
            val arr=JSONArray()
            bookmarks(path).filterNot {it==mark}.forEach {
                arr.put(JSONObject().put("chapter",it.chapter)
                    .put("fraction",it.fraction).put("createdAt",it.createdAt))
            }
            book.put("bookmarks",arr)
        }
    }
    fun collections():List<String> =
        records().flatMap {row->
            val arr=row.optJSONArray("collections") ?:JSONArray()
            (0 until arr.length()).map {arr.optString(it)}
        }.filter {it.isNotBlank()}.distinct().sorted()
    fun addToCollection(path:String,name:String) {
        val label=name.trim().take(50)
        if(label.isBlank())return
        change(path) { row->
            val current=row.optJSONArray("collections") ?:JSONArray()
            if((0 until current.length()).none {current.optString(it)==label})
                current.put(label)
            row.put("collections",current)
        }
    }
    fun removeFromCollection(path:String,name:String) {
        change(path) {row->
            val current=row.optJSONArray("collections") ?:JSONArray()
            val updated=JSONArray()
            (0 until current.length()).map {current.optString(it)}
                .filter {it!=name}.forEach {updated.put(it)}
            row.put("collections",updated)
        }
    }

    private fun epubMetadata(file:File):Triple<String,String,List<String>> {
        return try {
            ZipFile(file).use {zip->
                val container=zip.getEntry("META-INF/container.xml") ?:return@use Triple("","",emptyList())
                val opfPath=zip.getInputStream(container).use {stream->
                    xml(stream).firstOrNull {it.first=="rootfile"}
                        ?.second?.get("full-path")
                } ?:return@use Triple("","",emptyList())
                val entry=zip.getEntry(opfPath) ?:return@use Triple("","",emptyList())
                val parser=Xml.newPullParser()
                zip.getInputStream(entry).use {stream->
                    parser.setInput(stream,"UTF-8")
                    var tag=""
                    var metadata=false
                    var title=""
                    var author=""
                    val subjects=mutableListOf<String>()
                    var event=parser.eventType
                    while(event!=XmlPullParser.END_DOCUMENT) {
                        when(event) {
                            XmlPullParser.START_TAG -> {
                                tag=parser.name.substringAfter(':').lowercase()
                                if(tag=="metadata")metadata=true
                            }
                            XmlPullParser.TEXT -> if(metadata) {
                                val value=parser.text?.trim().orEmpty()
                                if(value.isNotBlank())when(tag) {
                                    "title" -> if(title.isBlank())title=value
                                    "creator" -> if(author.isBlank())author=value
                                    "subject" -> subjects.add(value)
                                }
                            }
                            XmlPullParser.END_TAG -> {
                                val closed=parser.name.substringAfter(':').lowercase()
                                if(closed=="metadata")metadata=false
                                tag=""
                            }
                        }
                        event=parser.next()
                    }
                    Triple(title,author,subjects)
                }
            }
        }catch(_:Exception){Triple("","",emptyList())}
    }

    /**
     * Migrates legacy EPUB metadata without changing user-edited fields.
     * A one-time marker keeps subsequent launches quick and offline.
     */
    fun enrichLegacyEpub():Int {
        val current=records()
        var modified=0
        current.forEach {item->
            if(item.optString("type")!="epub" || item.optBoolean("metadataChecked",false))
                return@forEach
            val path=item.optString("path")
            val file=File(context.filesDir,path).canonicalFile
            val root=context.filesDir.canonicalFile
            if(file.parentFile==root && file.isFile) {
                val (title,author,subjects)=epubMetadata(file)
                if(item.optString("author").isBlank() && author.isNotBlank())
                    item.put("author",author)
                // Respect previously edited titles and user-defined genres.
                if((item.optString("genre").isBlank() ||
                        item.optString("genre")==GenreCatalog.UNKNOWN) &&
                    subjects.isNotEmpty()) {
                    item.put("genre",GenreCatalog.fromSubjects(subjects))
                }
                if(item.optJSONArray("subjects")==null)
                    item.put("subjects",JSONArray(subjects))
            }
            item.put("metadataChecked",true)
            modified++
        }
        if(modified>0)persist(current)
        return modified
    }

    fun import(uri:Uri):String {
        val title=context.contentResolver.query(uri,null,null,null,null)?.use {cursor->
            if(cursor.moveToFirst()) {
                val col=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(col>=0)cursor.getString(col) else null
            }else null
        } ?: "Book"
        val extension=title.substringAfterLast('.',"").lowercase()
        require(extension in listOf("epub","pdf","cbz","md","markdown")){"Yalnız EPUB, PDF, CBZ və Markdown dəstəklənir"}
        val id="book_${System.currentTimeMillis()}.$extension"
        val file=File(context.filesDir,id)
        context.contentResolver.openInputStream(uri)!!.use {input->
            FileOutputStream(file).use {input.copyTo(it)}
        }
        val meta=if(extension=="epub")epubMetadata(file) else Triple("","",emptyList())
        val item=JSONObject().put("addedAt",System.currentTimeMillis())
            .put("title",meta.first.ifBlank {title.substringBeforeLast('.')})
            .put("author",meta.second)
            .put("genre",GenreCatalog.fromSubjects(meta.third))
            .put("subjects",JSONArray(meta.third))
            .put("metadataChecked",true)
            .put("path",id).put("type",extension)
            .put("page",0).put("scrollFraction",0.0).put("progress",0).put("status","Oxunmayıb")
        CoverExtractor.extract(context,file,extension,id)?.let {item.put("cover",it)}
        val books=records()+item
        persist(books)
        return id
    }
    fun extractMarkdown(path:String):List<File> {
        val source=File(context.filesDir,path).canonicalFile
        require(source.parentFile==context.filesDir.canonicalFile &&
            source.extension.lowercase() in setOf("md","markdown"))
        return listOf(MarkdownBooks.materialize(source,context.cacheDir))
    }
    fun markdownChapterTitles(path:String):List<String> =
        listOf(find(path)?.optString("title").orEmpty().ifBlank {"Markdown"})

    fun extractChapters(path:String):List<File> {
        val source=File(context.filesDir,path)
        val dir=File(context.cacheDir,"epub_v3_"+path.substringBeforeLast('.'))
        if(dir.exists())dir.deleteRecursively()
        dir.mkdirs()
        ZipFile(source).use { zip ->
            var count=0
            var size=0L
            zip.entries().asSequence().forEach { entry ->
                if(++count>5000)throw IllegalArgumentException("EPUB həddindən artıq çox fayldan ibarətdir")
                val target=File(dir,entry.name).canonicalFile
                require(target.path.startsWith(dir.canonicalPath+File.separator)){"Unsafe EPUB archive path"}
                if(entry.isDirectory)target.mkdirs() else {
                    target.parentFile?.mkdirs()
                    zip.getInputStream(entry).use {input->
                        FileOutputStream(target).use { output ->
                            val buffer=ByteArray(16384)
                            while(true) {
                                val n=input.read(buffer)
                                if(n<0)break
                                size+=n
                                if(size>200L*1024*1024)throw IllegalArgumentException("EPUB çox böyükdür")
                                output.write(buffer,0,n)
                            }
                        }
                    }
                }
            }
        }
        val container=File(dir,"META-INF/container.xml")
        val opfRelative=container.inputStream().use { xml(it) }
            .firstOrNull {it.first=="rootfile"}?.second?.get("full-path")
            ?: throw IllegalArgumentException("EPUB manifest tapılmadı")
        val opf=File(dir,opfRelative).canonicalFile
        require(opf.path.startsWith(dir.canonicalPath+File.separator))
        val tags=opf.inputStream().use {xml(it)}
        val manifest=tags.filter {it.first=="item"}
            .associate {it.second["id"].orEmpty() to it.second}
        val spine=tags.filter {it.first=="itemref"}
        val pages=spine.mapNotNull { row->
            val info=manifest[row.second["idref"]] ?:return@mapNotNull null
            val href=info["href"] ?:return@mapNotNull null
            val properties=info["properties"].orEmpty().split(' ')
            val normalized=java.net.URLDecoder.decode(href.substringBefore('#'),"UTF-8")
            val filename=normalized.substringAfterLast('/').lowercase()
            // EPUB 3 nav and EPUB 2 NCX are navigation resources, not reading chapters.
            if("nav" in properties || "ncx" in properties ||
                filename in setOf("nav.xhtml","nav.html","toc.xhtml","toc.html",
                    "contents.xhtml","contents.html","tableofcontents.xhtml"))return@mapNotNull null
            if(row.second["linear"]=="no")return@mapNotNull null
            val page=File(opf.parentFile,normalized).canonicalFile
            if(page.path.startsWith(dir.canonicalPath+File.separator) && page.exists() &&
                page.extension.lowercase() in listOf("xhtml","html","htm")) page else null
        }
        // Some EPUBs consist only of navigation-like documents. Preserve a fallback
        // rather than treating the book as empty.
        return if(pages.isNotEmpty())pages else spine.mapNotNull {row->
            val href=manifest[row.second["idref"]]?.get("href") ?:return@mapNotNull null
            val file=File(opf.parentFile,java.net.URLDecoder.decode(href.substringBefore('#'),"UTF-8")).canonicalFile
            if(file.exists() && file.path.startsWith(dir.canonicalPath+File.separator) &&
                file.extension.lowercase() in listOf("xhtml","html","htm"))file else null
        }
    }
    /** Resolve EPUB3 nav/EPUB2 NCX labels against the actual XHTML spine file paths.
     * The HTML <title> is a document title, often the same book title for each chapter.
     */
    fun chapterTitles(bookPath:String,chapters:List<File>):List<String> {
        if(chapters.isEmpty())return emptyList()
        val root=File(context.cacheDir,"epub_v3_"+bookPath.substringBeforeLast('.')).canonicalFile
        val labels=mutableMapOf<String,String>()
        val navigation=root.walkTopDown().filter {
            it.isFile && it.extension.lowercase() in setOf("xhtml","html","htm","ncx") &&
                it.name.lowercase() in setOf("nav.xhtml","nav.html","toc.xhtml","toc.html",
                    "contents.xhtml","contents.html","toc.ncx")
        }.toList()

        // EPUB3 <nav> links: match the referenced file, even if nav and chapters
        // reside in different subfolders.
        navigation.filter {it.extension.lowercase()!="ncx"}.forEach {nav->
            try {
                val html=nav.readText(Charsets.UTF_8)
                val anchors=Regex("""<a\b[^>]*href\s*=\s*["']([^"']+)["'][^>]*>(.*?)</a>""",
                    setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL))
                anchors.findAll(html).forEach {match->
                    val href=java.net.URLDecoder.decode(match.groupValues[1].substringBefore('#'),"UTF-8")
                    val target=File(nav.parentFile,href).canonicalFile
                    val label=match.groupValues[2].replace(Regex("<[^>]+>"),"")
                        .replace("&amp;","&").replace("&nbsp;"," ")
                        .replace("&#39;","'").replace("&quot;","\"")
                        .trim().replace(Regex("\\s+")," ")
                    if(target.path.startsWith(root.path+File.separator) && label.isNotBlank())
                        labels.putIfAbsent(target.path,label.take(100))
                }
            }catch(_:Exception){}
        }

        // EPUB2 NCX normally supplies navLabel + content src separately.
        navigation.filter {it.extension.lowercase()=="ncx"}.forEach {ncx->
            try {
                val source=ncx.readText(Charsets.UTF_8)
                val points=Regex("""<navPoint\b[^>]*>(.*?)</navPoint>""",
                    setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL))
                points.findAll(source).forEach {match->
                    val block=match.groupValues[1]
                    val name=Regex("""<text\b[^>]*>(.*?)</text>""",
                        setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL))
                        .find(block)?.groupValues?.get(1)?.replace(Regex("<[^>]+>"),"")?.trim()
                    val link=Regex("""<content\b[^>]*src\s*=\s*["']([^"']+)["']""",
                        RegexOption.IGNORE_CASE).find(block)?.groupValues?.get(1)
                    if(!name.isNullOrBlank() && !link.isNullOrBlank()) {
                        val href=java.net.URLDecoder.decode(link.substringBefore('#'),"UTF-8")
                        val target=File(ncx.parentFile,href).canonicalFile
                        if(target.path.startsWith(root.path+File.separator))
                            labels.putIfAbsent(target.path,name.take(100))
                    }
                }
            }catch(_:Exception){}
        }

        return chapters.mapIndexed {i,file->
            labels[file.canonicalPath] ?:run {
                // Prefer an actual visible heading, not the repeated HTML <title>.
                val heading=try {
                    val html=file.readText(Charsets.UTF_8).take(30000)
                    Regex("""<(?:h1|h2|h3)\b[^>]*>(.*?)</(?:h1|h2|h3)>""",
                        setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL))
                        .find(html)?.groupValues?.get(1)?.replace(Regex("<[^>]+>"),"")
                        ?.trim()?.replace(Regex("\\s+")," ")?.take(100)
                }catch(_:Exception){null}
                heading?.takeIf {it.isNotBlank()} ?: "Fəsil ${i+1}"
            }
        }
    }
    private fun xml(stream:java.io.InputStream):List<Pair<String,Map<String,String>>> {
        val parser=Xml.newPullParser().apply {setInput(stream,"UTF-8")}
        val items=mutableListOf<Pair<String,Map<String,String>>>()
        var event=parser.eventType
        while(event!=XmlPullParser.END_DOCUMENT) {
            if(event==XmlPullParser.START_TAG) {
                val attrs=(0 until parser.attributeCount)
                    .associate { parser.getAttributeName(it) to parser.getAttributeValue(it) }
                items.add(parser.name.substringAfter(':') to attrs)
            }
            event=parser.next()
        }
        return items
    }
}
