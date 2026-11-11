package az.leafreader

import org.junit.Assert.*
import org.junit.Test

class MarkdownAndLanguageTest {
    @Test fun markdownRendersHeadingsTablesAndCode() {
        val html=MarkdownBooks.render("# Başlıq\n\n| A | B |\n|---|---|\n| 1 | 2 |\n\n```kotlin\nval x=1\n```")
        assertTrue(html.contains("<h1>Başlıq</h1>"))
        assertTrue(html.contains("<table>"))
        assertTrue(html.contains("val x=1"))
        assertTrue(html.contains("<pre>"))
    }

    @Test fun markdownEscapesInlineHtmlAndBlocksUnsafeUrls() {
        val html=MarkdownBooks.render("<script>alert('x')</script>\n\n[click](javascript:alert(1))")
        assertFalse(html.contains("<script>"))
        assertFalse(html.contains("href=\"javascript:"))
    }

    @Test fun languageSwitchDoesNotChangeOriginalTextOrStatusIdentifiers() {
        assertEquals("Oxunur",UiLanguage.translate("Oxunur","az"))
        assertEquals("Читаю",UiLanguage.translate("Oxunur","ru"))
        assertEquals("Reading",UiLanguage.translate("Oxunur","en"))
        assertEquals("Глава 3",UiLanguage.translate("Fəsil 3","ru"))
        assertEquals("Chapter 3",UiLanguage.translate("Fəsil 3","en"))
        assertEquals("10 saved bookmarks",
            UiLanguage.translate("10 yadda saxlanmış mövqe","en"))
    }
}
