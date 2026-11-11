package az.leafreader

/**
 * Comic presentation only. The underlying page index stays in natural CBZ order
 * regardless of left-to-right or right-to-left touch/navigation direction.
 */
object ComicModes {
    const val SINGLE="single"
    const val DOUBLE="double"
    const val WEBTOON="webtoon"
    val all=setOf(SINGLE,DOUBLE,WEBTOON)
    fun valid(value:String):String=if(value in all)value else SINGLE
    fun firstPage(index:Int,mode:String):Int =
        if(mode==DOUBLE)(index.coerceAtLeast(0)/2)*2 else index.coerceAtLeast(0)
    fun pageIndices(index:Int,count:Int,mode:String):List<Int> {
        if(count<=0)return emptyList()
        val first=firstPage(index,mode).coerceIn(0,count-1)
        return if(mode==DOUBLE)listOf(first,first+1).filter {it<count} else listOf(first)
    }
    fun advance(index:Int,count:Int,delta:Int,mode:String):Int {
        if(count<=0)return 0
        val step=if(mode==DOUBLE)2 else 1
        return (firstPage(index,mode)+delta*step).coerceIn(0,count-1)
            .let {firstPage(it,mode)}
    }
}
