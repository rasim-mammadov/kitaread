package az.leafreader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderSearchTest {
    @Test fun normalizesMultiSpaceQuery() {
        assertEquals("black hole",ReaderSearch.normalize("  black  \n  hole "))
    }
    @Test fun snippetsMatchIgnoringCase() {
        val text="Introduction. The distant SPACE station is quiet. Later, another space station appears."
        val found=ReaderSearch.snippets(text,"space station")
        assertEquals(2,found.size)
        assertTrue(found[0].contains("SPACE station"))
    }
    @Test fun shortQueryReturnsNoResults() {
        assertTrue(ReaderSearch.snippets("abcdef","a").isEmpty())
    }
    @Test fun resultLimitIsRespected() {
        assertEquals(2,ReaderSearch.snippets("star star star star","star",2).size)
    }
}
