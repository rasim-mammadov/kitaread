package az.leafreader

import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DayReading(val day: LocalDate, val minutes: Long)
data class ReadingOverview(
    val totalBooks: Int,
    val finishedBooks: Int,
    val readingMinutes: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val activeDays: Int,
    val daily: List<DayReading>,
    val epubBooks: Int,
    val pdfBooks: Int,
    val comicBooks: Int,
    val markdownBooks: Int,
    val completedMonthly: List<Pair<String,Int>>
)

object ReadingStats {
    /**
     * Sessions are stored per book. Group by ISO day, no online data or permissions.
     * A streak day needs at least 5 recorded minutes of reading (default).
     */
    fun calculate(items: List<JSONObject>, today: LocalDate, minDailyMinutes: Long = 5): ReadingOverview {
        val minutes = sortedMapOf<LocalDate,Long>()
        for (book in items) {
            val sessions=book.optJSONArray("readingSessions") ?:continue
            for (i in 0 until sessions.length()) {
                val record=sessions.optJSONObject(i) ?:continue
                val day=try {LocalDate.parse(record.optString("date"))}catch(_:Exception){continue}
                if(day.isAfter(today))continue
                val ms=record.optLong("durationMs",0L).coerceAtLeast(0L)
                minutes[day]=(minutes[day] ?: 0L)+ms
            }
        }
        val qualifying=minutes.filterValues {it>=minDailyMinutes*60_000L}.keys
        // Today may still be incomplete; do not break yesterday's streak.
        var cursor=if(today in qualifying) today else today.minusDays(1)
        var current=0
        while(cursor in qualifying) {
            current++
            cursor=cursor.minusDays(1)
        }
        var longest=0
        var running=0
        var last:LocalDate?=null
        qualifying.sorted().forEach {day->
            running=if(last!=null && last!!.plusDays(1)==day)running+1 else 1
            if(running>longest)longest=running
            last=day
        }
        val totalMs=items.sumOf {it.optLong("readingMs",0L).coerceAtLeast(0L)}
        val daily=minutes.map {DayReading(it.key,it.value/60_000L)}
        val monthStart=today.withDayOfMonth(1)
        val months=(5 downTo 0).map {monthStart.minusMonths(it.toLong())}
        val completedMonthly=months.map {month->
            val count=items.count {book->
                if(book.optString("status")!="Bitirilib")false else {
                    val date=try {LocalDate.parse(book.optString("completedAt"))}
                        catch(_:Exception){null}
                    date!=null && !date.isAfter(today) &&
                        date.year==month.year && date.monthValue==month.monthValue
                }
            }
            month.format(DateTimeFormatter.ofPattern("MM/yy")) to count
        }
        return ReadingOverview(
            totalBooks=items.size,
            finishedBooks=items.count {it.optString("status")=="Bitirilib"},
            readingMinutes=totalMs/60_000L,
            currentStreak=current,
            longestStreak=longest,
            activeDays=qualifying.size,
            daily=daily,
            epubBooks=items.count {it.optString("type")=="epub"},
            pdfBooks=items.count {it.optString("type")=="pdf"},
            comicBooks=items.count {it.optString("type")=="cbz"},
            markdownBooks=items.count {it.optString("type") in setOf("md","markdown")},
            completedMonthly=completedMonthly
        )
    }
}
