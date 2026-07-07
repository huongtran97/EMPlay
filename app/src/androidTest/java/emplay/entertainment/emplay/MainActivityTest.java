package emplay.entertainment.emplay;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import emplay.entertainment.emplay.activity.MainActivity;
import emplay.entertainment.emplay.auth.AuthManager;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Reset auth to NONE before each test so the welcome dialog always shows,
     * regardless of what a previous test did.
     * ActivityScenarioRule launches a fresh activity after @Before runs, so the
     * singleton state set here is what the activity sees on its first call.
     */
    @Before
    public void resetAuthState() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AuthManager.getInstance(context).signOut();
    }

    // --- Welcome dialog ---

    @Test
    public void welcomeDialog_isShown_whenNotLoggedIn() {
        onView(withId(R.id.btn_guest)).check(matches(isDisplayed()));
    }

    @Test
    public void welcomeDialog_showsLoginAndGuestButtons() {
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_guest)).check(matches(isDisplayed()));
    }

    @Test
    public void guestButton_dismissesWelcomeDialog() {
        onView(withId(R.id.btn_guest)).perform(click());
        onView(withId(R.id.btn_guest)).check(doesNotExist());
    }

    // --- Bottom navigation (all start with dialog dismissal) ---

    @Test
    public void bottomNav_allTabs_areVisibleAfterGuestLogin() {
        onView(withId(R.id.btn_guest)).perform(click());

        onView(withId(R.id.nav_home)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_search)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_profile)).check(matches(isDisplayed()));
    }

    @Test
    public void bottomNav_searchTab_isClickable() {
        onView(withId(R.id.btn_guest)).perform(click());
        onView(withId(R.id.nav_search)).perform(click());
        onView(withId(R.id.nav_search)).check(matches(isDisplayed()));
    }

    @Test
    public void bottomNav_profileTab_isClickable() {
        onView(withId(R.id.btn_guest)).perform(click());
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.nav_profile)).check(matches(isDisplayed()));
    }

    @Test
    public void bottomNav_homeTab_isClickable_afterTabSwitch() {
        onView(withId(R.id.btn_guest)).perform(click());
        onView(withId(R.id.nav_search)).perform(click());
        onView(withId(R.id.nav_home)).perform(click());
        onView(withId(R.id.nav_home)).check(matches(isDisplayed()));
    }

    @Test
    public void bottomNav_canCycleThroughAllTabs() {
        onView(withId(R.id.btn_guest)).perform(click());
        onView(withId(R.id.nav_search)).perform(click());
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.nav_home)).perform(click());
        onView(withId(R.id.nav_home)).check(matches(isDisplayed()));
    }
}