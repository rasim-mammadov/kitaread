package az.leafreader

import android.os.SystemClock
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Device-local reading-time ledger.
 *
 * Counts time between foreground reader interactions, capped at 5 minutes per
 * interval so a forgotten open book is not treated as hours of reading.
 * Flushed on activity background and whenever the book is closed.
 */
class ReadingSessionTracker(private val store: BookStore) {
    private var bookPath: String? = null
    private var active = false
    private var lastEventAt = 0L
    private var accumulatedMs = 0L
    private var startedAt = 0L

    fun begin(path: String) {
        if (bookPath != path) {
            end()
            bookPath = path
            startedAt = System.currentTimeMillis()
            accumulatedMs = 0L
        }
        resume()
    }

    fun resume() {
        if (bookPath == null || active) return
        active = true
        lastEventAt = SystemClock.elapsedRealtime()
    }

    fun interaction() {
        if (!active) return
        accrue()
    }

    fun pause() {
        if (!active) return
        accrue()
        active = false
        flush()
    }

    fun end() {
        pause()
        bookPath = null
        active = false
        accumulatedMs = 0L
    }

    private fun accrue() {
        val now = SystemClock.elapsedRealtime()
        if (lastEventAt > 0L) {
            val interval = ReadingMath.creditedInterval(lastEventAt, now)
            accumulatedMs += interval
        }
        lastEventAt = now
    }

    private fun flush() {
        val path = bookPath ?: return
        val elapsed = accumulatedMs
        if (elapsed < 1000L) return
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val start = startedAt
        store.change(path) { book ->
            book.put("readingMs", book.optLong("readingMs", 0L) + elapsed)
            val sessions = book.optJSONArray("readingSessions") ?: JSONArray()
            sessions.put(JSONObject()
                .put("date", date)
                .put("startedAt", start)
                .put("endedAt", System.currentTimeMillis())
                .put("durationMs", elapsed))
            book.put("readingSessions", sessions)
            if (book.optString("startedAt").isEmpty())book.put("startedAt", date)
            book.put("lastReadAt", date)
        }
        accumulatedMs = 0L
        startedAt = System.currentTimeMillis()
    }
}
