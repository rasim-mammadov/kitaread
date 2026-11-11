package az.leafreader

import android.content.Context
import android.webkit.WebView

/**
 * Viewport-based, CSS multi-column EPUB pagination.
 * No third-party EPUB scripting is enabled; only layout/scroll metrics are read.
 */
class EpubChapterView(
    context: Context,
    private val changed: (EpubChapterView) -> Unit
) : WebView(context) {
    fun maxHorizontalScroll(): Int =
        (computeHorizontalScrollRange() - width).coerceAtLeast(0)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        changed(this)
    }
}
