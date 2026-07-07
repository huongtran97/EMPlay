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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

import emplay.entertainment.emplay.activity.MainActivity;
import emplay.entertainment.emplay.auth.AuthManager;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    // RuleChain ensures auth is reset BEFORE ActivityScenarioRule launches the activity.
    // Without this ordering, ActivityScenarioRule.before() fires before @Before, so the
    // activity launches with stale GUEST state from the previous test — no dialog appears.
    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(new ResetAuthRule())
            .around(new ActivityScenarioRule<>(MainActivity.class));

    private static class ResetAuthRule implements TestRule {
        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
                    AuthManager.getInstance(ctx).signOut();
                    base.evaluate();
                }
            };
        }
    }

    // Welcome dialog

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

    // Bottom navigation (all start with dialog dismissal)

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