package az.leafreader

import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth

/** Offline analytics based exclusively on recorded reading sessions. */
object ReadingInsights {
    fun minutesBetween(daily:List<DayReading>,start:LocalDate,end:LocalDate):Long =
        daily.filter { !it.day.isBefore(start) && !it.day.isAfter(end) }
            .sumOf {it.minutes.coerceAtLeast(0L)}

    fun heatmapDays(today:LocalDate,weeks:Int=13):List<LocalDate> {
        val count=weeks.coerceIn(1,53)*7
        return (count-1 downTo 0).map {today.minusDays(it.toLong())}
    }

    fun completedSince(books:List<JSONObject>,start:LocalDate,end:LocalDate):Int =
        books.count { book ->
            if(book.optString("status")!="Bitirilib")false else {
                val date=runCatching {LocalDate.parse(book.optString("completedAt"))}.getOrNull()
                date!=null && !date.isBefore(start) && !date.isAfter(end)
            }
        }

    fun genreProgress(books:List<JSONObject>):List<Pair<String,Int>> =
        books.filter {it.optString("status")=="Bitirilib"}
            .map {it.optString("genre").trim()}
            .filter {it.isNotBlank() && it!=GenreCatalog.UNKNOWN}
            .groupingBy {it}.eachCount().toList()
            .sortedWith(compareByDescending<Pair<String,Int>> {it.second}.thenBy {it.first})

    fun monthStart(today:LocalDate):LocalDate = YearMonth.from(today).atDay(1)

    data class Award(val key:String,val earned:Boolean)
    fun awards(overview:ReadingOverview):List<Award> = listOf(
        Award("İlk kitab",overview.finishedBooks>=1),
        Award("Kitab qurdu",overview.finishedBooks>=10),
        Award("7 günlük zəncir",overview.longestStreak>=7),
        Award("30 günlük zəncir",overview.longestStreak>=30),
        Award("10 saat oxu",overview.readingMinutes>=600),
        Award("100 saat oxu",overview.readingMinutes>=6000)
    )
}
