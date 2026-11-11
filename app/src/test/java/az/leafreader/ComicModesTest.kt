package az.leafreader

import org.junit.Assert.*
import org.junit.Test

class ComicModesTest {
    @Test fun doublePageGroupsConsecutivePages() {
        assertEquals(listOf(0,1),ComicModes.pageIndices(1,9,ComicModes.DOUBLE))
        assertEquals(listOf(8),ComicModes.pageIndices(8,9,ComicModes.DOUBLE))
        assertEquals(listOf(6,7),ComicModes.pageIndices(7,9,ComicModes.DOUBLE))
    }
    @Test fun nextSpreadMovesTwoPages() {
        assertEquals(2,ComicModes.advance(0,10,1,ComicModes.DOUBLE))
        assertEquals(4,ComicModes.advance(2,10,1,ComicModes.DOUBLE))
        assertEquals(0,ComicModes.advance(2,10,-1,ComicModes.DOUBLE))
    }
    @Test fun invalidValuesFallBackToSingle() {
        assertEquals(ComicModes.SINGLE,ComicModes.valid("invalid"))
        assertEquals(5,ComicModes.firstPage(5,ComicModes.SINGLE))
        assertEquals(emptyList<Int>(),ComicModes.pageIndices(0,0,ComicModes.DOUBLE))
    }
}
