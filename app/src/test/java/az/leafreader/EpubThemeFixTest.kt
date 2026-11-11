package az.leafreader

import org.junit.Assert.*
import org.junit.Test

class EpubThemeFixTest {
    @Test fun darkThemeOverrideForcesLightTextAndTransparentNestedBackground() {
        val css=EpubThemeFix.overrideCss("#1C2229","#F0EFE9")
        assertTrue(css.contains("color:#F0EFE9!important"))
        assertTrue(css.contains("-webkit-text-fill-color:#F0EFE9!important"))
        assertTrue(css.contains("background-color:transparent!important"))
        assertTrue(css.contains(":not(img)"))
    }
    @Test fun finalThemeOverrideIsInsertedAfterExistingBodyStyles() {
        val original="<html><head><style>p{color:black!important}</style></head><body><p>Text</p></body></html>"
        val html=EpubThemeFix.insert(original,"<style>p{color:white!important}</style>")
        assertTrue(html.indexOf("color:white")>html.indexOf("color:black"))
        assertTrue(html.contains("</body>"))
        assertEquals(1,Regex("</body>").findAll(html).count())
    }
    @Test fun missingBodyTagStillReceivesTheme() {
        assertTrue(EpubThemeFix.insert("<p>test</p>","<style>body{color:white}</style>")
            .endsWith("</style>"))
    }
}
