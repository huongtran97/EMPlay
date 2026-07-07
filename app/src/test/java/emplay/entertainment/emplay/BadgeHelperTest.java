package emplay.entertainment.emplay;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import emplay.entertainment.emplay.tool.BadgeHelper;

import static org.junit.Assert.*;

/**
 * Tests for BadgeHelper's pure-Java date utility methods.
 * Methods that require Android Views/Context are not covered here.
 */
public class BadgeHelperTest {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private static String daysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return SDF.format(cal.getTime());
    }

    private static String daysFromNow(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        return SDF.format(cal.getTime());
    }

    // isFutureDate
    @Test
    public void isFutureDate_withFutureDate_returnsTrue() {
        assertTrue(BadgeHelper.isFutureDate(daysFromNow(5)));
    }

    @Test
    public void isFutureDate_withPastDate_returnsFalse() {
        assertFalse(BadgeHelper.isFutureDate(daysAgo(5)));
    }

    @Test
    public void isFutureDate_withNull_returnsFalse() {
        assertFalse(BadgeHelper.isFutureDate(null));
    }

    @Test
    public void isFutureDate_withEmptyString_returnsFalse() {
        assertFalse(BadgeHelper.isFutureDate(""));
    }

    @Test
    public void isFutureDate_withFarFutureDate_returnsTrue() {
        assertTrue(BadgeHelper.isFutureDate("2099-01-01"));
    }

    @Test
    public void isFutureDate_withFarPastDate_returnsFalse() {
        assertFalse(BadgeHelper.isFutureDate("2000-01-01"));
    }

    @Test
    public void isFutureDate_withInvalidString_returnsFalse() {
        assertFalse(BadgeHelper.isFutureDate("not-a-date"));
    }

    // isWithinDays (past only, not future)
    @Test
    public void isWithinDays_recentPastDate_returnsTrue() {
        assertTrue(BadgeHelper.isWithinDays(daysAgo(10), 30));
    }

    @Test
    public void isWithinDays_dateBeyondThreshold_returnsFalse() {
        assertFalse(BadgeHelper.isWithinDays(daysAgo(31), 30));
    }

    @Test
    public void isWithinDays_futureDate_returnsFalse() {
        assertFalse(BadgeHelper.isWithinDays(daysFromNow(5), 30));
    }

    @Test
    public void isWithinDays_nullDate_returnsFalse() {
        assertFalse(BadgeHelper.isWithinDays(null, 30));
    }

    @Test
    public void isWithinDays_emptyDate_returnsFalse() {
        assertFalse(BadgeHelper.isWithinDays("", 30));
    }

    @Test
    public void isWithinDays_exactBoundary_returnsFalse() {
        // A date exactly 30 days ago is at the boundary; cutoff is set to -30 days,
        // so "30 days ago" is NOT after the cutoff — should return false.
        assertFalse(BadgeHelper.isWithinDays(daysAgo(30), 30));
    }

    // isNotOlderThan (includes future dates)
    @Test
    public void isNotOlderThan_recentPastDate_returnsTrue() {
        assertTrue(BadgeHelper.isNotOlderThan(daysAgo(10), 30));
    }

    @Test
    public void isNotOlderThan_futureDate_returnsTrue() {
        assertTrue(BadgeHelper.isNotOlderThan(daysFromNow(5), 30));
    }

    @Test
    public void isNotOlderThan_oldDate_returnsFalse() {
        assertFalse(BadgeHelper.isNotOlderThan(daysAgo(31), 30));
    }

    @Test
    public void isNotOlderThan_nullDate_returnsFalse() {
        assertFalse(BadgeHelper.isNotOlderThan(null, 30));
    }

    @Test
    public void isNotOlderThan_emptyDate_returnsFalse() {
        assertFalse(BadgeHelper.isNotOlderThan("", 30));
    }

    //  getTVShowType
    @Test
    public void getTVShowType_within30Days_returnsNewTVShow() {
        assertEquals("New TV show", BadgeHelper.getTVShowType(daysAgo(15)));
    }

    @Test
    public void getTVShowType_between30And365Days_returnsSeason() {
        assertEquals("SEASON", BadgeHelper.getTVShowType(daysAgo(100)));
    }

    @Test
    public void getTVShowType_olderThan365Days_returnsEP() {
        assertEquals("EP", BadgeHelper.getTVShowType(daysAgo(400)));
    }

    @Test
    public void getTVShowType_nullDate_returnsEP() {
        // isIsNew → false, isWithinDays → false, so falls through to EP
        assertEquals("EP", BadgeHelper.getTVShowType(null));
    }

    // formatRelativeDate
    @Test
    public void formatRelativeDate_today_returnsToday() {
        assertEquals("Today", BadgeHelper.formatRelativeDate(daysAgo(0)));
    }

    @Test
    public void formatRelativeDate_yesterday_returnsYesterday() {
        assertEquals("Yesterday", BadgeHelper.formatRelativeDate(daysAgo(1)));
    }

    @Test
    public void formatRelativeDate_fiveDaysAgo_returnsDaysAgo() {
        assertEquals("5 days ago", BadgeHelper.formatRelativeDate(daysAgo(5)));
    }

    @Test
    public void formatRelativeDate_futureDate_returnsEmpty() {
        assertEquals("", BadgeHelper.formatRelativeDate(daysFromNow(3)));
    }

    @Test
    public void formatRelativeDate_null_returnsEmpty() {
        assertEquals("", BadgeHelper.formatRelativeDate(null));
    }

    @Test
    public void formatRelativeDate_emptyString_returnsEmpty() {
        assertEquals("", BadgeHelper.formatRelativeDate(""));
    }

    @Test
    public void formatRelativeDate_invalidString_returnsEmpty() {
        assertEquals("", BadgeHelper.formatRelativeDate("not-a-date"));
    }
}