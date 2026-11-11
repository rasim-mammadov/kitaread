package az.leafreader

import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.Locale

/** Read-only indexing for library search, group headings and exact duplicate inspection. */
object SmartLibrary {
    fun normalized(value:String):String =
        java.text.Normalizer.normalize(value,java.text.Normalizer.Form.NFKC)
            .lowercase(Locale.ROOT).trim().replace(Regex("\\s+")," ")

    fun matches(book:ReaderBook,record:JSONObject?,query:String):Boolean {
        val terms=normalized(query).split(' ').filter {it.isNotBlank()}
        if(terms.isEmpty())return true
        val collection=record?.optJSONArray("collections")
        val collectionNames=if(collection==null)"" else
            (0 until collection.length()).joinToString(" ") {collection.optString(it)}
        val haystack=normalized(listOf(book.title,record?.optString("author").orEmpty(),
            record?.optString("series").orEmpty(),record?.optString("genre").orEmpty(),
            collectionNames,book.type).joinToString(" "))
        return terms.all {it in haystack}
    }

    fun groupName(book:ReaderBook,record:JSONObject?,groupBy:String):String {
        val label=when(groupBy) {
            "author"->record?.optString("author").orEmpty()
            "series"->record?.optString("series").orEmpty()
            else->""
        }
        return label.trim().ifBlank {"—"}
    }

    /** Only true binary equality is reported; equal names are NOT duplicates. */
    fun exactDuplicates(directory:File,books:List<ReaderBook>):List<List<String>> {
        val root=directory.canonicalFile
        val groupedByLength=books.mapNotNull {book->
            val file=File(root,book.path).canonicalFile
            if(file.parentFile==root && file.isFile) book.path to file else null
        }.groupBy {it.second.length()}.filterValues {it.size>1}
        val hashes=mutableMapOf<String,MutableList<String>>()
        for(group in groupedByLength.values) for((path,file) in group) {
            val digest=MessageDigest.getInstance("SHA-256")
            file.inputStream().buffered().use {input->
                val bytes=ByteArray(65536)
                while(true) {
                    val read=input.read(bytes)
                    if(read<0)break
                    digest.update(bytes,0,read)
                }
            }
            val hex=digest.digest().joinToString("") {"%02x".format(it)}
            hashes.getOrPut(hex) {mutableListOf()}.add(path)
        }
        return hashes.values.filter {it.size>1}
    }
}
