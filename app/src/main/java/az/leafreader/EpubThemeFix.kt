package az.leafreader

/**
 * EPUB CSS can contain fixed inline black text and dark backgrounds.
 * The reader theme must override both when switching from a custom book theme
 * back to the global one; do not touch images and SVG artwork.
 */
object EpubThemeFix {
    fun overrideCss(background:String,foreground:String):String = """
        <style id="leaf-reader-theme-override">
        html,body{background-color:$background!important;color:$foreground!important;
            -webkit-text-fill-color:$foreground!important;color-scheme:normal!important;}
        body *:not(img):not(svg):not(image):not(video):not(canvas){
            color:$foreground!important;-webkit-text-fill-color:$foreground!important;
            background-color:transparent!important;
        }
        body a,body a:visited{color:$foreground!important;-webkit-text-fill-color:$foreground!important;}
        body img,body svg,body video,body canvas{max-width:100%!important;}
        </style>
    """.trimIndent()

    fun insert(html:String,css:String):String {
        val closing=Regex("</body\\s*>",RegexOption.IGNORE_CASE)
        return if(closing.containsMatchIn(html))html.replaceFirst(closing,css+"</body>")
            else html+css
    }
}
