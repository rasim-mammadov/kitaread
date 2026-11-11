package az.leafreader

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingMathTest {
    @Test fun activeIntervalCapsLongIdleTime() {
        assertEquals(300_000L,ReadingMath.creditedInterval(0L,600_000L))
        assertEquals(45_000L,ReadingMath.creditedInterval(20_000L,65_000L))
        assertEquals(0L,ReadingMath.creditedInterval(70_000L,65_000L))
    }
    @Test fun fractionsDoNotDivideByZero() {
        assertEquals(0.0,ReadingMath.scrollFraction(0,0),0.0)
        assertEquals(0.5,ReadingMath.scrollFraction(50,100),0.000001)
        assertEquals(1.0,ReadingMath.scrollFraction(150,100),0.000001)
    }
    @Test fun progressClampedAtNinetyNineUntilCompleted() {
        assertEquals(0,ReadingMath.fixedPageProgress(0,0))
        assertEquals(0,ReadingMath.fixedPageProgress(0,10))
        assertEquals(50,ReadingMath.fixedPageProgress(5,10))
        assertEquals(99,ReadingMath.fixedPageProgress(9,10,1.0))
    }
}
