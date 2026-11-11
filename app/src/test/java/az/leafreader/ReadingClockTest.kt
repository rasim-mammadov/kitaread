package az.leafreader

import org.junit.Assert.*
import org.junit.Test

class ReadingClockTest {
    @Test fun onlyForegroundIntervalsAccumulate() {
        val clock=ReadingClock()
        clock.resume(100L)
        assertEquals(3L,clock.elapsedSeconds(3600L))
        clock.pause(5100L)
        assertEquals(5L,clock.elapsedSeconds(100_000L))
        clock.resume(105_000L)
        assertEquals(6L,clock.elapsedSeconds(106_900L))
    }

    @Test fun resetAndFormattingAreStable() {
        val clock=ReadingClock()
        clock.resume(0)
        clock.reset(8_000)
        assertEquals(0L,clock.elapsedSeconds(8_000))
        assertEquals("00:59",ReadingClock.display(59))
        assertEquals("01:01",ReadingClock.display(61))
        assertEquals("1:01:01",ReadingClock.display(3661))
    }
}
