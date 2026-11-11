package az.leafreader

import org.json.JSONObject

/** Library-only options; reader typography is intentionally unaffected. */
object LibraryPersonalization {
    const val COMPACT="compact"
    fun normalizeLayout(value:String):String=if(value in setOf("list","grid",COMPACT))value else "list"
    fun nextLayout(current:String):String=when(current) {
        "list"->"grid";"grid"->COMPACT;else->"list"
    }
    fun fontSize(value:Int):Int=value.coerceIn(11,22)
    fun density(value:Int):Int=value.coerceIn(0,2)
    fun pinned(record:JSONObject?):Boolean=record?.optBoolean("pinned",false)==true

    /** Stable, non-destructive sort: pinned first, existing user sort maintained within groups. */
    fun pinnedFirst(sorted:List<ReaderBook>,records:Map<String,JSONObject>):List<ReaderBook> =
        sorted.filter {pinned(records[it.path])}+
        sorted.filterNot {pinned(records[it.path])}
}
