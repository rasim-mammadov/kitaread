package az.leafreader

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import java.io.File

/** Read-only Markdown rendering. Original .md bytes remain untouched. */
object MarkdownBooks {
    private val extensions=listOf(TablesExtension.create(),StrikethroughExtension.create())
    private val parser=Parser.builder().extensions(extensions).build()
    private val renderer=HtmlRenderer.builder().extensions(extensions)
        .escapeHtml(true)
        .sanitizeUrls(true)
        .build()

    fun render(source:String):String {
        val safe=source.removePrefix("\uFEFF")
        val body=renderer.render(parser.parse(safe))
        return """<!DOCTYPE html><html><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
            body{overflow-wrap:anywhere}
            pre{overflow-x:auto;white-space:pre;max-width:100%;padding:12px;border-radius:8px;background:rgba(128,128,128,.13)}
            code{font-family:monospace}
            pre code{font-size:.85em!important}
            table{border-collapse:collapse;display:block;overflow-x:auto;max-width:100%}
            th,td{padding:7px;border:1px solid currentColor}
            blockquote{border-left:3px solid currentColor;padding-left:12px;margin-left:0;opacity:.85}
            img{max-width:100%;height:auto}
            </style></head><body>$body</body></html>"""
    }

    fun materialize(original:File,cacheDir:File):File {
        require(original.isFile && original.length()<=10L*1024*1024) {
            "Markdown faylı 10 MB həddini aşır"
        }
        val directory=File(cacheDir,"markdown_reader")
        directory.mkdirs()
        val page=File(directory,original.nameWithoutExtension+".html")
        page.writeText(render(original.readText(Charsets.UTF_8)),Charsets.UTF_8)
        return page
    }
}
