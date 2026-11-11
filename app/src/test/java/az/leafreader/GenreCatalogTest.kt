package az.leafreader

import org.junit.Assert.assertEquals
import org.junit.Test

class GenreCatalogTest {
    @Test fun subjectMetadataMapsToOfflineGenres() {
        assertEquals("Elmi fantastika",GenreCatalog.fromSubjects(listOf("Science Fiction")))
        assertEquals("Fantastika",GenreCatalog.fromSubjects(listOf("Fantasy")))
        assertEquals("Tarix",GenreCatalog.fromSubjects(listOf("History")))
        assertEquals("Komiks",GenreCatalog.fromSubjects(listOf("Graphic Novel")))
    }
    @Test fun unknownSubjectsRemainUnknownWithoutAIInvention() {
        assertEquals(GenreCatalog.UNKNOWN,GenreCatalog.fromSubjects(emptyList()))
        assertEquals(GenreCatalog.UNKNOWN,GenreCatalog.fromSubjects(listOf("XYZ Catalog 143")))
    }
    @Test fun titleLevelsAndNextMilestones() {
        assertEquals("I · Kəşfiyyatçı",GenreCatalog.level(1))
        assertEquals("II · Səyyah",GenreCatalog.level(5))
        assertEquals("III · Ustad",GenreCatalog.level(15))
        assertEquals(5,GenreCatalog.nextMilestone(3))
        assertEquals(15,GenreCatalog.nextMilestone(5))
        assertEquals(null,GenreCatalog.nextMilestone(15))
    }
    @Test fun genreHasReaderTitle() {
        assertEquals("Kosmos Səyyahı",GenreCatalog.title("Elmi fantastika"))
        assertEquals("Cəngavər",GenreCatalog.title("Fantastika"))
        assertEquals("Kitab Kəşfiyyatçısı",GenreCatalog.title(GenreCatalog.UNKNOWN))
    }
}
