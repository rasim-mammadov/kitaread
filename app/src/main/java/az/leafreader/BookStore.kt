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
    fun import(uri:Uri):String {
        val title=context.contentResolver.query(uri,null,null,null,null)?.use {cursor->
            if(cursor.moveToFirst()) {
                val col=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(col>=0)cursor.getString(col) else null
            }else null
        } ?: "Book"
        val extension=title.substringAfterLast('.',"").lowercase()
        require(extension in listOf("epub","pdf","cbz")){"Yalnız EPUB, PDF və CBZ dəstəklənir"}
        val id="book_${System.currentTimeMillis()}.$extension"
        val file=File(context.filesDir,id)
        context.contentResolver.openInputStream(uri)!!.use {input->
            FileOutputStream(file).use {input.copyTo(it)}
        }
        val item=JSONObject().put("title",title).put("path",id).put("type",extension)
            .put("page",0).put("scrollFraction",0.0).put("progress",0).put("status","Oxunmayıb")
        CoverExtractor.extract(context,file,extension,id)?.let {item.put("cover",it)}
        val books=records()+item
        persist(books)
        return id
    }
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
            .associate {it.second["id"].orEmpty() to it.second["href"].orEmpty()}
        return tags.filter {it.first=="itemref"}.mapNotNull { row->
            val href=manifest[row.second["idref"]] ?:return@mapNotNull null
            val page=File(opf.parentFile,java.net.URLDecoder.decode(href.substringBefore('#'),"UTF-8")).canonicalFile
            if(page.path.startsWith(dir.canonicalPath+File.separator) && page.exists() &&
                page.extension.lowercase() in listOf("xhtml","html","htm")) page else null
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
