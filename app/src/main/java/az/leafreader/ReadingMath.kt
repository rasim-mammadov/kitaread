package az.leafreader

import kotlin.math.roundToInt

/** Pure functions used by reading telemetry and progress persistence. */
object ReadingMath {
    const val MAX_ACTIVE_GAP_MS = 300_000L

    fun creditedInterval(previousElapsedMs:Long,nowElapsedMs:Long):Long =
        (nowElapsedMs-previousElapsedMs).coerceIn(0L,MAX_ACTIVE_GAP_MS)

    fun scrollFraction(scroll:Int,maxScroll:Int):Double =
        if(maxScroll<=0)0.0 else scroll.toDouble().div(maxScroll).coerceIn(0.0,1.0)

    fun fixedPageProgress(page:Int,totalPages:Int,fraction:Double=0.0):Int {
        if(totalPages<=0)return 0
        val clampedPage=page.coerceIn(0,totalPages-1)
        return ((clampedPage+fraction.coerceIn(0.0,1.0))/totalPages*100)
            .roundToInt().coerceIn(0,99)
    }
}
