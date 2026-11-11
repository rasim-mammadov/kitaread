package az.leafreader

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SmartLibraryTest {
    private fun book(path:String,title:String)=ReaderBook(
        path,title,"epub","Oxunmayıb",0,"",0,0.0,"")

    @Test fun searchMatchesAllTokensAcrossAuthorSeriesAndCollection() {
        val data=JSONObject()
            .put("author","Isaac Asimov")
            .put("series","Foundation")
            .put("collections",org.json.JSONArray().put("Favorites"))
        assertTrue(SmartLibrary.matches(book("a.epub","Prelude"),data,"asimov foundation"))
        assertTrue(SmartLibrary.matches(book("a.epub","Prelude"),data,"foundation favorites"))
        assertFalse(SmartLibrary.matches(book("a.epub","Prelude"),data,"foundation dune"))
        assertTrue(SmartLibrary.matches(book("a.epub","Prelude"),data,""))
    }

    @Test fun groupingUsesMetadataWithFallback() {
        val data=JSONObject().put("author","Author").put("series","Series 1")
        assertEquals("Author",SmartLibrary.groupName(book("a.epub","A"),data,"author"))
        assertEquals("Series 1",SmartLibrary.groupName(book("a.epub","A"),data,"series"))
        assertEquals("—",SmartLibrary.groupName(book("a.epub","A"),data,"none"))
    }

    @Test fun duplicatesAreContentBasedNotNameBased() {
        val dir=Files.createTempDirectory("leaf-dupes").toFile()
        try {
            File(dir,"1.epub").writeText("same bytes")
            File(dir,"2.epub").writeText("same bytes")
            File(dir,"3.epub").writeText("different content")
            val duplicates=SmartLibrary.exactDuplicates(dir,
                listOf(book("1.epub","X"),book("2.epub","Y"),book("3.epub","X")))
            assertEquals(1,duplicates.size)
            assertEquals(setOf("1.epub","2.epub"),duplicates.first().toSet())
        } finally {dir.deleteRecursively()}
    }
}
