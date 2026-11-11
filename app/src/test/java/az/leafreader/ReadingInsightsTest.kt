package az.leafreader

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import java.time.LocalDate

class ReadingInsightsTest {
    private val today=LocalDate.of(2026,11,11)
    @Test fun heatmapContainsExactlyThirteenWeeksEndingToday() {
        val dates=ReadingInsights.heatmapDays(today)
        assertEquals(91,dates.size)
        assertEquals(today.minusDays(90),dates.first())
        assertEquals(today,dates.last())
    }
    @Test fun dateWindowIncludesBothBoundariesButNotOutside() {
        val days=listOf(DayReading(today.minusDays(3),5),
            DayReading(today.minusDays(2),10),
            DayReading(today.minusDays(1),15),DayReading(today,20))
        assertEquals(45L,ReadingInsights.minutesBetween(days,today.minusDays(2),today))
    }
    @Test fun awardsAreEarnedOnlyWhenThresholdIsReached() {
        val base=ReadingOverview(1,1,599,0,6,1,emptyList(),1,0,0,0,emptyList())
        val awards=ReadingInsights.awards(base).associate {it.key to it.earned}
        assertEquals(true,awards["İlk kitab"])
        assertEquals(false,awards["10 saat oxu"])
        assertEquals(false,awards["7 günlük zəncir"])
        val upgraded=base.copy(readingMinutes=600,longestStreak=7)
        assertEquals(true,ReadingInsights.awards(upgraded).first {it.key=="10 saat oxu"}.earned)
        assertEquals(true,ReadingInsights.awards(upgraded).first {it.key=="7 günlük zəncir"}.earned)
    }
    @Test fun monthlyFinishCountsOnlyCompletedBooksWithRealDates() {
        val valid=JSONObject().put("status","Bitirilib").put("completedAt","2026-11-09")
        val earlier=JSONObject().put("status","Bitirilib").put("completedAt","2026-10-30")
        val invalid=JSONObject().put("status","Oxunur").put("completedAt","2026-11-09")
        assertEquals(1,ReadingInsights.completedSince(listOf(valid,earlier,invalid),
            today.withDayOfMonth(1),today))
    }
    @Test fun genresCountOnlyFinishedBooks() {
        val finished=JSONObject().put("status","Bitirilib").put("genre","Fantasy")
        val active=JSONObject().put("status","Oxunur").put("genre","Fantasy")
        assertEquals(listOf("Fantasy" to 1),ReadingInsights.genreProgress(listOf(finished,active)))
    }
}
