package az.leafreader

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class LibraryPersonalizationTest {
    private fun book(id:String)=ReaderBook(id,id,"epub","Oxunmayıb",0,"",0,0.0,"")
    @Test fun compactCyclesAfterGrid() {
        assertEquals("grid",LibraryPersonalization.nextLayout("list"))
        assertEquals("compact",LibraryPersonalization.nextLayout("grid"))
        assertEquals("list",LibraryPersonalization.nextLayout("compact"))
        assertEquals("list",LibraryPersonalization.normalizeLayout("unknown"))
    }
    @Test fun pinnedFirstPreservesOrderWithinEachGroup() {
        val books=listOf(book("a"),book("b"),book("c"),book("d"))
        val info=mapOf("a" to JSONObject().put("pinned",false),
            "b" to JSONObject().put("pinned",true),
            "c" to JSONObject().put("pinned",true))
        assertEquals(listOf("b","c","a","d"),
            LibraryPersonalization.pinnedFirst(books,info).map {it.path})
    }
    @Test fun typographyClampsToValidRange() {
        assertEquals(11,LibraryPersonalization.fontSize(-8))
        assertEquals(22,LibraryPersonalization.fontSize(100))
        assertEquals(0,LibraryPersonalization.density(-1))
        assertEquals(2,LibraryPersonalization.density(5))
    }
}
