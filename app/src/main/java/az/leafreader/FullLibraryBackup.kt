package az.leafreader

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Self-contained offline ZIP backup containing metadata AND original book bytes.
 * Restores as new uniquely named files, never overwriting existing books.
 */
class FullLibraryBackup(private val context:Context,private val store:BookStore) {
    data class Result(val restoredBooks:Int,val restoredCovers:Int)
    private val allowed=setOf("epub","pdf","cbz","md","markdown")
    private val filename=Regex("[A-Za-z0-9_.-]{1,180}")
    private val maxBytes=500L*1024*1024
    private val maxEntryBytes=200L*1024*1024

    private fun safeName(name:String):Boolean =
        filename.matches(name) && name!="." && name!=".." &&
            !name.startsWith(".") && !name.contains("..")

    fun exportZip(output:OutputStream) {
        val books=store.records()
        val json=store.exportMetadata().toByteArray(Charsets.UTF_8)
        require(json.size<=20*1024*1024){"Metadata çox böyükdür"}
        val root=context.filesDir.canonicalFile
        ZipOutputStream(output.buffered()).use {zip->
            zip.putNextEntry(ZipEntry("metadata.json"))
            zip.write(json)
            zip.closeEntry()
            val archived=mutableSetOf<String>()
            var total=json.size.toLong()
            for(book in books) {
                val path=book.optString("path")
                require(safeName(path) && path.substringAfterLast('.').lowercase() in allowed) {
                    "Kitab yolu etibarsızdır"
                }
                val file=File(root,path).canonicalFile
                require(file.parentFile==root && file.isFile){"Kitab faylı tapılmadı: $path"}
                val cover=book.optString("cover")
                val sources=listOf("books/$path" to file)+
                    if(cover.isNotBlank()) {
                        val coverName=cover.removePrefix("covers/")
                        require(safeName(coverName) &&
                            (cover==coverName || cover=="covers/$coverName")) {
                            "Üz qabığı yolu etibarsızdır"
                        }
                        val coverFile=File(root,cover).canonicalFile
                        val coverRoot=File(root,"covers").canonicalFile
                        if(coverFile.isFile &&
                            coverFile.parentFile in setOf(root,coverRoot))
                            listOf("covers/$coverName" to coverFile) else emptyList()
                    } else emptyList()
                for((entry,source) in sources) {
                    if(!archived.add(entry))continue
                    require(source.length()<=maxEntryBytes){"Fayl çox böyükdür"}
                    total+=source.length()
                    require(total<=maxBytes){"Backup həcmi 500 MB-dan çoxdur"}
                    zip.putNextEntry(ZipEntry(entry))
                    source.inputStream().buffered().use {it.copyTo(zip)}
                    zip.closeEntry()
                }
            }
        }
    }

    fun restoreZip(input:InputStream):Result {
        val staging=File(context.cacheDir,"leaf_restore_"+UUID.randomUUID()).apply {mkdirs()}
        val staged=mutableMapOf<String,File>()
        val inserted=mutableListOf<File>()
        try {
            var total=0L
            ZipInputStream(input.buffered()).use {zip->
                while(true) {
                    val entry=zip.nextEntry ?:break
                    require(!entry.isDirectory){"Gözlənilməz qovluq"}
                    val key=entry.name
                    require(key=="metadata.json" ||
                        ((key.startsWith("books/") || key.startsWith("covers/")) &&
                            safeName(key.substringAfter('/')) &&
                            key.count {it=='/'}==1)){"Backup daxilində etibarsız yol"}
                    require(key !in staged){"Təkrarlanan backup faylı"}
                    require(staged.size<40001){"Həddindən artıq çox fayl"}
                    val target=File(staging,UUID.randomUUID().toString())
                    var length=0L
                    target.outputStream().buffered().use {out->
                        val buffer=ByteArray(65536)
                        while(true) {
                            val n=zip.read(buffer)
                            if(n<0)break
                            length+=n
                            total+=n
                            require(length<=maxEntryBytes && total<=maxBytes) {
                                "Backup ölçü limitini keçir"
                            }
                            out.write(buffer,0,n)
                        }
                    }
                    staged[key]=target
                    zip.closeEntry()
                }
            }
            val meta=staged["metadata.json"] ?:error("Metadata tapılmadı")
            require(meta.length()<=20*1024*1024){"Metadata çox böyükdür"}
            val data=JSONObject(meta.readText(Charsets.UTF_8))
            require(data.optString("kind")=="leaf-reader-metadata" &&
                data.optInt("schemaVersion") in 1..3){"Dəstəklənməyən backup"}
            val sourceBooks=data.getJSONArray("books")
            require(sourceBooks.length()<=20000){"Həddindən artıq çox kitab"}
            val existing=store.records()
            val merged=existing.toMutableList()
            val root=context.filesDir.canonicalFile
            val planned=mutableListOf<Triple<String,File,File>>()
            val used=mutableSetOf<String>()
            var covers=0
            for(i in 0 until sourceBooks.length()){
                val old=sourceBooks.optJSONObject(i) ?:error("Kitab metadata-sı pozulub")
                val path=old.optString("path")
                require(safeName(path) && path.substringAfterLast('.').lowercase() in allowed) {
                    "Kitab yolu etibarsızdır"
                }
                val source=staged["books/$path"] ?:error("Kitab faylı çatışmır: $path")
                val newPath="restored_"+UUID.randomUUID()+"."+path.substringAfterLast('.').lowercase()
                val destination=File(root,newPath).canonicalFile
                require(destination.parentFile==root && !destination.exists())
                planned.add(Triple(newPath,source,destination))
                val copy=JSONObject(old.toString()).put("path",newPath)
                val cover=old.optString("cover")
                if(cover.isNotBlank()) {
                    val coverName=cover.removePrefix("covers/")
                    require(safeName(coverName) &&
                        (cover==coverName || cover=="covers/$coverName")) {
                        "Üz qabığı yolu etibarsızdır"
                    }
                    val coverSource=staged["covers/$coverName"]
                    if(coverSource!=null) {
                        val extension=cover.substringAfterLast('.',"jpg").lowercase()
                        val safeExt=if(extension in setOf("jpg","jpeg","png","webp"))extension else "jpg"
                        val coverPath="restored_cover_"+UUID.randomUUID()+".$safeExt"
                        val target=File(root,coverPath).canonicalFile
                        planned.add(Triple(coverPath,coverSource,target))
                        copy.put("cover",coverPath)
                        covers++
                    } else copy.remove("cover")
                }
                merged.add(copy)
                used.add("books/$path")
            }
            // Reject orphan book files: a tampered archive cannot silently add untracked bytes.
            require(staged.keys.filter {it.startsWith("books/")}.all {it in used}) {
                "Metadata-sız kitab faylı"
            }
            try {
                for((_,source,destination) in planned) {
                    require(!destination.exists()){"Fayl toqquşması"}
                    source.copyTo(destination,overwrite=false)
                    inserted.add(destination)
                }
                val resultArray=JSONArray()
                merged.forEach {resultArray.put(it)}
                val prefs=context.getSharedPreferences("library",Context.MODE_PRIVATE)
                check(prefs.edit().putString("items",resultArray.toString()).commit()) {
                    "Kitabxana bərpa edilə bilmədi"
                }
            }catch(e:Exception){
                inserted.forEach {it.delete()}
                throw e
            }
            val settings=data.optJSONObject("readerSettings")
            if(settings!=null){
                val preferences=store.setting()
                val edit=preferences.edit()
                val keys=settings.keys()
                while(keys.hasNext()){
                    val key=keys.next()
                    if(preferences.contains(key))continue
                    when(val v=settings.opt(key)){
                        is Boolean->edit.putBoolean(key,v)
                        is Int->edit.putInt(key,v)
                        is Long->edit.putLong(key,v)
                        is Number->edit.putFloat(key,v.toFloat())
                        is String->edit.putString(key,v)
                    }
                }
                edit.apply()
            }
            return Result(sourceBooks.length(),covers)
        }finally {
            staging.deleteRecursively()
        }
    }
}
