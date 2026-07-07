package emplay.entertainment.emplay;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.espresso.intent.rule.IntentsRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import emplay.entertainment.emplay.activity.LoginActivity;
import emplay.entertainment.emplay.activity.MainActivity;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Rule
    public IntentsRule intentsRule = new IntentsRule();

    // Layout visibility
    @Test
    public void googleButton_isDisplayed() {
        onView(withId(R.id.btn_google)).check(matches(isDisplayed()));
    }

    @Test
    public void tmdbButton_isDisplayed() {
        onView(withId(R.id.btn_tmdb)).check(matches(isDisplayed()));
    }

    @Test
    public void guestLink_isDisplayed() {
        onView(withId(R.id.tv_guest)).check(matches(isDisplayed()));
    }

    // Guest navigation
    @Test
    public void guestLink_click_firesMainActivityIntent() {
        // Stub MainActivity so the host activity finishes cleanly without
        // actually launching and loading the full main screen during this test.
        intending(hasComponent(MainActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));

        onView(withId(R.id.tv_guest)).perform(click());

        intended(hasComponent(MainActivity.class.getName()));
    }
}