package az.leafreader

/**
 * Foreground-only elapsed session timer. Not used in reading analytics:
 * ReadingSessionTracker remains the source of truth for credited reading time.
 */
class ReadingClock {
    private var sinceMs:Long?=null
    private var accumulatedMs=0L

    fun resume(nowMs:Long) {
        if(sinceMs==null)sinceMs=nowMs
    }

    fun pause(nowMs:Long) {
        val since=sinceMs ?:return
        accumulatedMs+=(nowMs-since).coerceAtLeast(0L)
        sinceMs=null
    }

    fun elapsedSeconds(nowMs:Long):Long {
        val active=sinceMs?.let {(nowMs-it).coerceAtLeast(0L)} ?:0L
        return (accumulatedMs+active)/1000L
    }

    fun reset(nowMs:Long) {
        accumulatedMs=0L
        if(sinceMs!=null)sinceMs=nowMs
    }

    companion object {
        fun display(seconds:Long):String {
            val hours=seconds.coerceAtLeast(0L)/3600L
            val minutes=(seconds.coerceAtLeast(0L)%3600L)/60L
            val secs=seconds.coerceAtLeast(0L)%60L
            return if(hours>0L) "%d:%02d:%02d".format(java.util.Locale.US,hours,minutes,secs)
                else "%02d:%02d".format(java.util.Locale.US,minutes,secs)
        }
    }
}
