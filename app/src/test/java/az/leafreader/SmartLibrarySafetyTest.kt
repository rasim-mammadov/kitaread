package az.leafreader

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

/** Regression checks for offline Smart Library safety and deterministic search. */
class SmartLibrarySafetyTest {
    private fun book(path: String, title: String = path) = ReaderBook(
        path, title, "epub", "Oxunmayıb", 0, "", 0, 0.0, ""
    )

    @Test fun normalizationIsCaseWhitespaceAndUnicodeWidthInsensitive() {
        assertEquals("foundation volume 1",
            SmartLibrary.normalized("  ＦＯＵＮＤＡＴＩＯＮ   Volume\t1  "))
    }

    @Test fun emptyOrWhitespaceOnlyQueryMatchesBooksWithoutMetadata() {
        assertTrue(SmartLibrary.matches(book("first.epub", "Book"), null, "  \t "))
    }

    @Test fun identicalTitlesButDifferentBytesAreNotDuplicates() {
        val root = Files.createTempDirectory("leaf-safety").toFile()
        try {
            File(root, "a.epub").writeText("first file")
            File(root, "b.epub").writeText("other file")
            assertTrue(SmartLibrary.exactDuplicates(
                root, listOf(book("a.epub", "Same"), book("b.epub", "Same"))
            ).isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun sameBytesAreDetectedEvenWithDifferentNames() {
        val root = Files.createTempDirectory("leaf-safety").toFile()
        try {
            File(root, "a.epub").writeText("same content")
            File(root, "b.epub").writeText("same content")
            val groups = SmartLibrary.exactDuplicates(
                root, listOf(book("a.epub", "First"), book("b.epub", "Second"))
            )
            assertEquals(1, groups.size)
            assertEquals(setOf("a.epub", "b.epub"), groups.single().toSet())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test fun outsideRootAndMissingFilesNeverEnterDuplicateScan() {
        val root = Files.createTempDirectory("leaf-safety").toFile()
        try {
            File(root, "inside.epub").writeText("same content")
            val groups = SmartLibrary.exactDuplicates(root, listOf(
                book("inside.epub"), book("../outside.epub"), book("missing.epub")
            ))
            assertTrue(groups.isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }
}
