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
import android.content.Context;

import androidx.test.espresso.intent.rule.IntentsRule;
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

import emplay.entertainment.emplay.activity.LoginActivity;
import emplay.entertainment.emplay.activity.MainActivity;
import emplay.entertainment.emplay.auth.AuthManager;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    // ResetAuthRule must be outermost so auth is NONE before the activity launches.
    // On a real device the user may be signed in via Firebase, which causes
    // LoginActivity.onStart() to immediately redirect — making all views disappear.
    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(new ResetAuthRule())
            .around(new IntentsRule())
            .around(new ActivityScenarioRule<>(LoginActivity.class));

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