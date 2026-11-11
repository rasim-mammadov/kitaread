package az.leafreader

import org.json.JSONArray
import org.json.JSONObject

data class ReaderAnnotation(
    val id:String,
    val chapter:Int,
    val quote:String,
    val note:String,
    val createdAt:Long
)

fun BookStore.annotations(path:String):List<ReaderAnnotation> {
    val array=find(path)?.optJSONArray("annotations") ?:return emptyList()
    return (0 until array.length()).mapNotNull {i->
        val item=array.optJSONObject(i) ?:return@mapNotNull null
        ReaderAnnotation(item.optString("id"),item.optInt("chapter",0),
            item.optString("quote"),item.optString("note"),
            item.optLong("createdAt",0))
    }.sortedByDescending {it.createdAt}
}

fun BookStore.saveAnnotation(path:String,chapter:Int,quote:String,note:String,id:String?=null):String {
    val safeQuote=quote.trim().take(400)
    val safeNote=note.trim().take(4000)
    require(safeQuote.isNotEmpty() || safeNote.isNotEmpty()){"Boş qeyd saxlanılmır"}
    val key=id ?: java.util.UUID.randomUUID().toString()
    change(path){row->
        val current=row.optJSONArray("annotations") ?:JSONArray()
        val updated=JSONArray()
        for(i in 0 until current.length()) {
            val item=current.optJSONObject(i) ?:continue
            if(item.optString("id")!=key)updated.put(item)
        }
        updated.put(JSONObject().put("id",key).put("chapter",chapter.coerceAtLeast(0))
            .put("quote",safeQuote).put("note",safeNote)
            .put("createdAt",System.currentTimeMillis()))
        row.put("annotations",updated)
    }
    return key
}

fun BookStore.deleteAnnotation(path:String,id:String) {
    change(path){row->
        val old=row.optJSONArray("annotations") ?:return@change
        val remaining=JSONArray()
        for(i in 0 until old.length()) {
            val item=old.optJSONObject(i) ?:continue
            if(item.optString("id")!=id)remaining.put(item)
        }
        row.put("annotations",remaining)
    }
}

/** Pure search normalization and capped context-window extraction. */
object ReaderSearch {
    fun normalize(query:String):String=query.trim().replace(Regex("\\s+")," ")
    fun snippets(text:String,query:String,limit:Int=6):List<String> {
        val needle=normalize(query)
        if(needle.length<2)return emptyList()
        val normalized=text.replace(Regex("\\s+")," ")
        val results=mutableListOf<String>()
        var position=0
        while(results.size<limit) {
            val index=normalized.indexOf(needle,position,ignoreCase=true)
            if(index<0)break
            val start=(index-65).coerceAtLeast(0)
            val end=(index+needle.length+90).coerceAtMost(normalized.length)
            results.add((if(start>0)"… " else "")+
                normalized.substring(start,end).trim()+
                (if(end<normalized.length)" …" else ""))
            position=index+needle.length
        }
        return results
    }
}
