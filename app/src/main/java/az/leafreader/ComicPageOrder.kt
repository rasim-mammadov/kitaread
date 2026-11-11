package az.leafreader

/** Natural page order: page_2.png precedes page_10.png. */
object ComicPageOrder {
    private val segments=Regex("\\d+|\\D+")
    val comparator=Comparator<String> {left,right->
        val a=segments.findAll(left.lowercase()).map {it.value}.toList()
        val b=segments.findAll(right.lowercase()).map {it.value}.toList()
        var result=0
        val max=minOf(a.size,b.size)
        for(i in 0 until max) {
            val x=a[i];val y=b[i]
            val digit=x.firstOrNull()?.isDigit()==true &&
                y.firstOrNull()?.isDigit()==true
            result=if(digit) {
                val xx=x.trimStart('0').ifEmpty {"0"}
                val yy=y.trimStart('0').ifEmpty {"0"}
                xx.length.compareTo(yy.length).takeIf {it!=0}
                    ?:xx.compareTo(yy).takeIf {it!=0}
                    ?:x.length.compareTo(y.length)
            }else x.compareTo(y)
            if(result!=0)break
        }
        if(result==0)result=a.size.compareTo(b.size)
        if(result==0)result=left.compareTo(right)
        result
    }
}
