package emplay.entertainment.emplay;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import emplay.entertainment.emplay.tool.PaginationHelper;

import static org.junit.Assert.*;

public class PaginationHelperTest {

    /** Captures the most recent callback values for assertions. */
    private static class Capture<T> implements PaginationHelper.PaginationCallback<T> {
        List<T> lastPage = new ArrayList<>();
        int currentPage, totalPages;
        boolean hasPrev, hasNext;

        @Override
        public void onPageUpdated(List<T> pageItems) {
            lastPage = new ArrayList<>(pageItems);
        }

        @Override
        public void onUiUpdate(int current, int total, boolean hasPrev, boolean hasNext) {
            this.currentPage = current;
            this.totalPages = total;
            this.hasPrev = hasPrev;
            this.hasNext = hasNext;
        }
    }

    private static List<Integer> range(int from, int toInclusive) {
        List<Integer> list = new ArrayList<>();
        for (int i = from; i <= toInclusive; i++) list.add(i);
        return list;
    }

    // Single-page scenarios
    @Test
    public void singlePage_showsAllItems() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(5, range(1, 3), cap);
        helper.showPage();

        assertEquals(Arrays.asList(1, 2, 3), cap.lastPage);
        assertEquals(1, cap.currentPage);
        assertEquals(1, cap.totalPages);
        assertFalse(cap.hasPrev);
        assertFalse(cap.hasNext);
    }

    @Test
    public void exactlyOneFullPage_hasNoPrevOrNext() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(5, range(1, 5), cap);
        helper.showPage();

        assertEquals(5, cap.lastPage.size());
        assertFalse(cap.hasPrev);
        assertFalse(cap.hasNext);
    }

    // Multi-page navigation
    @Test
    public void nextPage_advancesToSecondPage() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 7), cap);
        helper.showPage();
        helper.nextPage();

        assertEquals(Arrays.asList(4, 5, 6), cap.lastPage);
        assertEquals(2, cap.currentPage);
        assertTrue(cap.hasPrev);
        assertTrue(cap.hasNext);
    }

    @Test
    public void nextPage_lastPageHasRemainder() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 7), cap);
        helper.showPage();
        helper.nextPage();
        helper.nextPage(); // page 3 — only item 7

        assertEquals(Arrays.asList(7), cap.lastPage);
        assertEquals(3, cap.currentPage);
        assertTrue(cap.hasPrev);
        assertFalse(cap.hasNext);
    }

    @Test
    public void nextPage_doesNotGoPastLastPage() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 3), cap);
        helper.showPage();
        helper.nextPage(); // already on last page — should stay
        helper.nextPage();

        assertEquals(1, cap.currentPage);
    }

    @Test
    public void prevPage_goesBackFromSecondPage() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 6), cap);
        helper.showPage();
        helper.nextPage();
        helper.prevPage();

        assertEquals(Arrays.asList(1, 2, 3), cap.lastPage);
        assertEquals(1, cap.currentPage);
        assertFalse(cap.hasPrev);
    }

    @Test
    public void prevPage_doesNotGoBelowPageOne() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 3), cap);
        helper.showPage();
        helper.prevPage(); // already on page 1 — no-op
        helper.prevPage();

        assertEquals(1, cap.currentPage);
    }

    // isAtLastLocalPage
    @Test
    public void isAtLastLocalPage_trueOnFirstPageWhenOnePage() {
        PaginationHelper<Integer> helper = new PaginationHelper<>(5, range(1, 3), new Capture<>());
        assertTrue(helper.isAtLastLocalPage());
    }

    @Test
    public void isAtLastLocalPage_falseOnFirstOfMultiplePages() {
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 7), new Capture<>());
        assertFalse(helper.isAtLastLocalPage());
    }

    @Test
    public void isAtLastLocalPage_trueAfterNavigatingToLastPage() {
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 6), new Capture<>());
        helper.nextPage(); // now on page 2 of 2
        assertTrue(helper.isAtLastLocalPage());
    }

    @Test
    public void isAtLastLocalPage_trueWhenEmpty() {
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, new ArrayList<>(), new Capture<>());
        assertTrue(helper.isAtLastLocalPage());
    }

    // updateData resets state
    @Test
    public void updateData_resetsToPageOne() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 6), cap);
        helper.showPage();
        helper.nextPage(); // move to page 2
        helper.updateData(range(10, 14));

        assertEquals(1, cap.currentPage);
        assertEquals(Arrays.asList(10, 11, 12), cap.lastPage);
    }

    // appendData grows the list without changing current page
    @Test
    public void appendData_growsListButDoesNotTriggerCallback() {
        Capture<Integer> cap = new Capture<>();
        PaginationHelper<Integer> helper = new PaginationHelper<>(3, range(1, 3), cap);
        helper.showPage(); // page 1 only
        helper.appendData(range(4, 6));

        // No showPage called yet — cap still reflects old state
        assertEquals(1, cap.currentPage);
        // After navigating, new items are available
        helper.nextPage();
        assertEquals(Arrays.asList(4, 5, 6), cap.lastPage);
    }

    // getCurrentPage
    @Test
    public void getCurrentPage_startsAtOne() {
        PaginationHelper<Integer> helper = new PaginationHelper<>(5, range(1, 10), new Capture<>());
        assertEquals(1, helper.getCurrentPage());
    }

    @Test
    public void getCurrentPage_reflectsNavigation() {
        PaginationHelper<Integer> helper = new PaginationHelper<>(5, range(1, 10), new Capture<>());
        helper.showPage();
        helper.nextPage();
        assertEquals(2, helper.getCurrentPage());
    }
}