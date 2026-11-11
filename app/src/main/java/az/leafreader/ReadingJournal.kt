package az.leafreader

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class JournalEntry(
    val id:String, val bookPath:String, val title:String, val author:String,
    val date:String, val body:String, val createdAt:Long
)

data class LibraryAnnotation(
    val bookPath:String, val bookTitle:String, val chapter:Int,
    val quote:String, val note:String, val createdAt:Long
)

/** Append-only journal entries. Legacy annotations are read without migration. */
object ReadingJournal {
    fun entries(books:List<JSONObject>):List<JournalEntry> =
        books.flatMap {book->
            val arr=book.optJSONArray("journalEntries") ?: JSONArray()
            (0 until arr.length()).mapNotNull {index->
                val item=arr.optJSONObject(index) ?:return@mapNotNull null
                JournalEntry(item.optString("id"),book.optString("path"),
                    book.optString("title"),book.optString("author"),
                    item.optString("date"),item.optString("body"),
                    item.optLong("createdAt"))
            }
        }.sortedWith(compareByDescending<JournalEntry>{it.date}.thenByDescending {it.createdAt})

    fun annotations(books:List<JSONObject>):List<LibraryAnnotation> =
        books.flatMap {book->
            val arr=book.optJSONArray("annotations") ?: JSONArray()
            (0 until arr.length()).mapNotNull {index->
                val item=arr.optJSONObject(index) ?:return@mapNotNull null
                LibraryAnnotation(book.optString("path"),book.optString("title"),
                    item.optInt("chapter",0),item.optString("quote"),
                    item.optString("note"),item.optLong("createdAt"))
            }
        }.sortedByDescending {it.createdAt}

    fun save(store:BookStore,path:String,body:String,date:String=LocalDate.now().toString()):String {
        val normalized=body.trim().take(10000)
        require(normalized.isNotBlank()){"Boş gündəlik qeydi saxlanılmır"}
        LocalDate.parse(date)
        require(store.find(path)!=null){"Kitab tapılmadı"}
        val id=UUID.randomUUID().toString()
        store.change(path) {book->
            val arr=book.optJSONArray("journalEntries") ?: JSONArray()
            arr.put(JSONObject().put("id",id).put("date",date)
                .put("body",normalized).put("createdAt",System.currentTimeMillis()))
            book.put("journalEntries",arr)
        }
        return id
    }

    fun review(store:BookStore,path:String,text:String) {
        require(store.find(path)!=null){"Kitab tapılmadı"}
        store.change(path){it.put("personalReview",text.trim().take(10000))}
    }

    fun delete(store:BookStore,path:String,id:String) {
        store.change(path){book->
            val arr=book.optJSONArray("journalEntries") ?:return@change
            val filtered=JSONArray()
            for(i in 0 until arr.length()){
                val item=arr.optJSONObject(i) ?:continue
                if(item.optString("id")!=id)filtered.put(item)
            }
            book.put("journalEntries",filtered)
        }
    }

    fun exportJson(books:List<JSONObject>):String {
        val data=JSONArray()
        books.forEach {book->
            data.put(JSONObject()
                .put("title",book.optString("title"))
                .put("author",book.optString("author"))
                .put("genre",book.optString("genre"))
                .put("rating",book.optInt("rating",0))
                .put("review",book.optString("personalReview"))
                .put("journalEntries",book.optJSONArray("journalEntries") ?:JSONArray())
                .put("annotations",book.optJSONArray("annotations") ?:JSONArray()))
        }
        return JSONObject().put("schemaVersion",1)
            .put("kind","leaf-reader-knowledge-export")
            .put("books",data).toString(2)
    }

    private fun escapeMarkdown(value:String):String =
        value.replace("\\","\\\\").replace("**","\\*\\*")
    fun exportMarkdown(books:List<JSONObject>):String = buildString {
        appendLine("# Leaf Reader — Reading Journal & Knowledge")
        books.forEach {book->
            val notes=book.optJSONArray("annotations") ?: JSONArray()
            val journal=book.optJSONArray("journalEntries") ?: JSONArray()
            val review=book.optString("personalReview")
            if(review.isBlank() && journal.length()==0 && notes.length()==0)return@forEach
            appendLine()
            appendLine("## "+escapeMarkdown(book.optString("title")))
            val author=book.optString("author")
            if(author.isNotBlank())appendLine("**Author:** "+escapeMarkdown(author))
            if(review.isNotBlank()){
                appendLine()
                appendLine("### Review")
                appendLine(review)
            }
            if(journal.length()>0){
                appendLine()
                appendLine("### Journal")
                for(i in 0 until journal.length()){
                    val entry=journal.optJSONObject(i) ?:continue
                    appendLine("#### "+entry.optString("date"))
                    appendLine(entry.optString("body"))
                    appendLine()
                }
            }
            if(notes.length()>0){
                appendLine("### Highlights & Notes")
                for(i in 0 until notes.length()){
                    val item=notes.optJSONObject(i) ?:continue
                    appendLine("**Chapter "+(item.optInt("chapter",0)+1)+"**")
                    val quote=item.optString("quote")
                    if(quote.isNotBlank())quote.lines().forEach {appendLine("> "+it)}
                    val note=item.optString("note")
                    if(note.isNotBlank())appendLine(note)
                    appendLine()
                }
            }
        }
    }
}
