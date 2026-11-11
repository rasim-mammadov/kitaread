package az.leafreader

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ReadingJournalTest {
    private fun book():JSONObject=JSONObject()
        .put("path","a.epub").put("title","Book A").put("author","Author A")
        .put("journalEntries",JSONArray().put(JSONObject()
            .put("id","journal-1").put("date","2026-11-11")
            .put("body","My reading reflection").put("createdAt",123L)))
        .put("annotations",JSONArray().put(JSONObject()
            .put("id","quote-1").put("chapter",2).put("quote","A quotation")
            .put("note","My note").put("createdAt",124L)))
        .put("personalReview","Good book")
    @Test fun journalAndExistingAnnotationsShareCentralIndex() {
        val data=listOf(book())
        assertEquals("My reading reflection",ReadingJournal.entries(data).single().body)
        assertEquals("A quotation",ReadingJournal.annotations(data).single().quote)
        assertEquals(2,ReadingJournal.annotations(data).single().chapter)
    }
    @Test fun jsonExportContainsReviewJournalAndLegacyAnnotations() {
        val json=JSONObject(ReadingJournal.exportJson(listOf(book())))
        val item=json.getJSONArray("books").getJSONObject(0)
        assertEquals("leaf-reader-knowledge-export",json.getString("kind"))
        assertEquals("Good book",item.getString("review"))
        assertEquals(1,item.getJSONArray("journalEntries").length())
        assertEquals(1,item.getJSONArray("annotations").length())
    }
    @Test fun markdownExportIncludesAllKnowledgeTypes() {
        val markdown=ReadingJournal.exportMarkdown(listOf(book()))
        assertTrue(markdown.contains("## Book A"))
        assertTrue(markdown.contains("Good book"))
        assertTrue(markdown.contains("My reading reflection"))
        assertTrue(markdown.contains("> A quotation"))
        assertTrue(markdown.contains("My note"))
    }
    @Test fun emptyLibraryExportIsValid() {
        assertEquals(0,JSONObject(ReadingJournal.exportJson(emptyList()))
            .getJSONArray("books").length())
        assertTrue(ReadingJournal.entries(emptyList()).isEmpty())
    }
}
