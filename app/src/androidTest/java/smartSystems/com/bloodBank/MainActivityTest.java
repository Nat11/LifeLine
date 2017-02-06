package smartSystems.com.bloodBank;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import smartSystems.com.bloodBank.Activities.MainActivity;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;

@RunWith(AndroidJUnit4.class)
@LargeTest

public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    @Test
    public void clickProceedNextButton_opensMainOrLoginUi() throws Exception {
        // Click on the proceed next button
        onView(withId(R.id.btnProceed)).perform(click());

        // Check if the login screen or user profile screen is displayed
        onView(
                anyOf(withId(R.id.etEmailAddr), withId(R.id.layoutUser))
        ).check(matches(isDisplayed()));
    }
}
