package az.leafreader

import org.junit.Assert.*
import org.junit.Test

class ComicPageOrderTest {
    @Test fun digitsSortNumerically() {
        val input=listOf("page10.jpg","page2.jpg","page1.jpg","page09.jpg")
        assertEquals(listOf("page1.jpg","page2.jpg","page09.jpg","page10.jpg"),
            input.sortedWith(ComicPageOrder.comparator))
    }
    @Test fun folderNamesRemainOrdered() {
        val pages=listOf("chapter2/1.png","chapter10/1.png","chapter1/4.png")
        assertEquals(listOf("chapter1/4.png","chapter2/1.png","chapter10/1.png"),
            pages.sortedWith(ComicPageOrder.comparator))
    }
}
