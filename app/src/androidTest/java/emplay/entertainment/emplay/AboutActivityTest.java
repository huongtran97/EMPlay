package emplay.entertainment.emplay;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import emplay.entertainment.emplay.activity.AboutActivity;

@RunWith(AndroidJUnit4.class)
public class AboutActivityTest {

    @Rule
    public ActivityScenarioRule<AboutActivity> activityRule =
            new ActivityScenarioRule<>(AboutActivity.class);

    // --- Static content ---

    @Test
    public void versionText_isDisplayed() {
        onView(withId(R.id.app_version)).check(matches(isDisplayed()));
    }

    @Test
    public void versionText_startsWithVersionPrefix() {
        onView(withId(R.id.app_version)).check(matches(withText(containsString("Version"))));
    }

    // --- Clickable rows ---

    @Test
    public void librariesRow_isDisplayed() {
        onView(withId(R.id.libraries_row)).check(matches(isDisplayed()));
    }

    @Test
    public void librariesRow_isClickable() {
        onView(withId(R.id.libraries_row)).check(matches(isClickable()));
    }

    @Test
    public void privacyPolicyRow_isDisplayed() {
        onView(withId(R.id.privacy_policy_row)).check(matches(isDisplayed()));
    }

    // --- Libraries dialog ---

    @Test
    public void clickLibraries_showsDialog() {
        onView(withId(R.id.libraries_row)).perform(click());
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()));
    }

    @Test
    public void librariesDialog_closeButton_dismissesDialog() {
        onView(withId(R.id.libraries_row)).perform(click());
        onView(withId(R.id.btn_close)).perform(click());
        onView(withId(R.id.btn_close)).check(doesNotExist());
    }
}