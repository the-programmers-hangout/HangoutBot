import me.markhc.hangoutbot.utilities.TimeFormatter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TimeFormatterTest {
    private fun makeTime(h: Int, m: Int, s: Int): Long {
        return  h * 60L * 60L * 1000L +
                m * 60L * 1000L +
                s * 1000L
    }

    @Test
    fun `TimeFormatter toLongDurationString 0 case`() {
        assertEquals("0 seconds", TimeFormatter.toLongDurationString(0))
        assertEquals("0 seconds", TimeFormatter.toLongDurationString(1))
        assertEquals("0 seconds", TimeFormatter.toLongDurationString(250))
        assertEquals("0 seconds", TimeFormatter.toLongDurationString(500))
        assertEquals("0 seconds", TimeFormatter.toLongDurationString(750))
        assertEquals("0 seconds", TimeFormatter.toLongDurationString(999))
    }

    @Test
    fun `TimeFormatter toLongDurationString seconds`() {
        assertEquals("1 second", TimeFormatter.toLongDurationString(makeTime(0, 0, 1)))
        assertEquals("2 seconds", TimeFormatter.toLongDurationString(makeTime(0, 0, 2)))
        assertEquals("30 seconds", TimeFormatter.toLongDurationString(makeTime(0, 0, 30)))
        assertEquals("59 seconds", TimeFormatter.toLongDurationString(makeTime(0, 0, 59)))
    }

    @Test
    fun `TimeFormatter toLongDurationString minutes`() {
        assertEquals("1 minute", TimeFormatter.toLongDurationString(makeTime(0, 1, 0)))
        assertEquals("1 minute, 1 second", TimeFormatter.toLongDurationString(makeTime(0, 1, 1)))
        assertEquals("1 minute, 2 seconds", TimeFormatter.toLongDurationString(makeTime(0, 1, 2)))

        assertEquals("2 minutes", TimeFormatter.toLongDurationString(makeTime(0, 2, 0)))
        assertEquals("2 minutes, 1 second", TimeFormatter.toLongDurationString(makeTime(0, 2, 1)))
        assertEquals("2 minutes, 2 seconds", TimeFormatter.toLongDurationString(makeTime(0, 2, 2)))

        assertEquals("30 minutes", TimeFormatter.toLongDurationString(makeTime(0, 30, 0)))
        assertEquals("30 minutes, 1 second", TimeFormatter.toLongDurationString(makeTime(0, 30, 1)))
        assertEquals("30 minutes, 2 seconds", TimeFormatter.toLongDurationString(makeTime(0, 30, 2)))

        assertEquals("59 minutes", TimeFormatter.toLongDurationString(makeTime(0, 59, 0)))
        assertEquals("59 minutes, 1 second", TimeFormatter.toLongDurationString(makeTime(0, 59, 1)))
        assertEquals("59 minutes, 2 seconds", TimeFormatter.toLongDurationString(makeTime(0, 59, 2)))
    }

    @Test
    fun `TimeFormatter toLongDurationString hours`() {
        assertEquals("1 hour", TimeFormatter.toLongDurationString(makeTime(1, 0, 0)))

        assertEquals("1 hour, 1 minute", TimeFormatter.toLongDurationString(makeTime(1, 1, 0)))
        assertEquals("1 hour, 1 minute, 1 second", TimeFormatter.toLongDurationString(makeTime(1, 1, 1)))
        assertEquals("1 hour, 1 minute, 2 seconds", TimeFormatter.toLongDurationString(makeTime(1, 1, 2)))

        assertEquals("2 hours, 2 minutes", TimeFormatter.toLongDurationString(makeTime(2, 2, 0)))
        assertEquals("2 hours, 2 minutes, 1 second", TimeFormatter.toLongDurationString(makeTime(2, 2, 1)))
        assertEquals("2 hours, 2 minutes, 2 seconds", TimeFormatter.toLongDurationString(makeTime(2, 2, 2)))

        assertEquals("23 hours, 30 minutes", TimeFormatter.toLongDurationString(makeTime(23, 30, 0)))
        assertEquals("23 hours, 30 minutes, 1 second", TimeFormatter.toLongDurationString(makeTime(23, 30, 1)))
        assertEquals("23 hours, 30 minutes, 2 seconds", TimeFormatter.toLongDurationString(makeTime(23, 30, 2)))
    }

    @Test
    fun `TimeFormatter toLongDurationString days`() {
        assertEquals("1 day", TimeFormatter.toLongDurationString(makeTime(24, 0, 0)))

        assertEquals("1 day, 1 hour", TimeFormatter.toLongDurationString(makeTime(25, 0, 0)))
        assertEquals("1 day, 2 hours", TimeFormatter.toLongDurationString(makeTime(26, 0, 0)))

        assertEquals("1 day, 1 hour, 1 minute", TimeFormatter.toLongDurationString(makeTime(25, 1, 0)))
        assertEquals("1 day, 1 hour, 2 minutes", TimeFormatter.toLongDurationString(makeTime(25, 2, 0)))
        assertEquals("1 day, 2 hours, 1 minute", TimeFormatter.toLongDurationString(makeTime(26, 1, 0)))
        assertEquals("1 day, 2 hours, 2 minutes", TimeFormatter.toLongDurationString(makeTime(26, 2, 0)))

        assertEquals("2 days, 1 hour, 1 minute", TimeFormatter.toLongDurationString(makeTime(49, 1, 0)))
        assertEquals("2 days, 1 hour, 2 minutes", TimeFormatter.toLongDurationString(makeTime(49, 2, 0)))
        assertEquals("2 days, 2 hours, 1 minute", TimeFormatter.toLongDurationString(makeTime(50, 1, 0)))
        assertEquals("2 days, 2 hours, 2 minutes", TimeFormatter.toLongDurationString(makeTime(50, 2, 0)))

        assertEquals("35 days, 1 hour, 1 minute", TimeFormatter.toLongDurationString(makeTime(35 * 24 + 1, 1, 0)))
        assertEquals("35 days, 1 hour, 2 minutes", TimeFormatter.toLongDurationString(makeTime(35 * 24 + 1, 2, 0)))
        assertEquals("35 days, 2 hours, 1 minute", TimeFormatter.toLongDurationString(makeTime(35 * 24 + 2, 1, 0)))
        assertEquals("35 days, 2 hours, 2 minutes", TimeFormatter.toLongDurationString(makeTime(35 * 24 + 2, 2, 0)))
    }

    @Test
    fun `TimeFormatter toShortDurationString`() {
        assertEquals("00:00:00", TimeFormatter.toShortDurationString(makeTime(0, 0, 0)))
        assertEquals("00:00:01", TimeFormatter.toShortDurationString(makeTime(0, 0, 1)))
        assertEquals("00:01:00", TimeFormatter.toShortDurationString(makeTime(0, 1, 0)))
        assertEquals("01:00:00", TimeFormatter.toShortDurationString(makeTime(1, 0, 0)))

        assertEquals("01:00:00", TimeFormatter.toShortDurationString(makeTime(1, 0, 0)))
        assertEquals("01:00:01", TimeFormatter.toShortDurationString(makeTime(1, 0, 1)))
        assertEquals("01:01:00", TimeFormatter.toShortDurationString(makeTime(1, 1, 0)))
        assertEquals("01:01:01", TimeFormatter.toShortDurationString(makeTime(1, 1, 1)))

        assertEquals("24:00:00", TimeFormatter.toShortDurationString(makeTime(24, 0, 0)))
        assertEquals("24:00:01", TimeFormatter.toShortDurationString(makeTime(24, 0, 1)))
        assertEquals("24:01:00", TimeFormatter.toShortDurationString(makeTime(24, 1, 0)))
        assertEquals("24:01:01", TimeFormatter.toShortDurationString(makeTime(24, 1, 1)))

        assertEquals("48:00:00", TimeFormatter.toShortDurationString(makeTime(48, 0, 0)))
        assertEquals("48:00:01", TimeFormatter.toShortDurationString(makeTime(48, 0, 1)))
        assertEquals("48:01:00", TimeFormatter.toShortDurationString(makeTime(48, 1, 0)))
        assertEquals("48:01:01", TimeFormatter.toShortDurationString(makeTime(48, 1, 1)))
    }
}
