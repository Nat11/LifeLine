package smartSystems.com.bloodBank;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import smartSystems.com.bloodBank.Activities.UserActivity;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class UserActivityTest {

    @Rule
    public ActivityTestRule<UserActivity> mActivityRule = new ActivityTestRule(UserActivity.class);

    @Test
    public void clickOnUpdateNavigationItem_ShowsUpdateScreen() {
        // Open Drawer to click on navigation.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_update));

        // Check that update Activity was opened.
        String expectedText = InstrumentationRegistry.getTargetContext()
                .getString(R.string.address);
        onView(withId(R.id.tvNewAddress)).check(matches(withText(expectedText)));
    }

    @Test
    public void clickOnLogoutNavigationItem_ShowsLoginScreen() {
        // Open Drawer to click on navigation.
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_logout));

        // Check that Login Activity was opened.
        onView(withId(R.id.etEmailAddr)).check(matches(isDisplayed()));
    }

    @Test
    public void clickOnSearchButton_ShowsMapsScreen() {

        onView(withId(R.id.btnDefaultSearch)).perform(click());

        // Check that DefaultMaps Activity was opened.
        onView(withId(R.id.fabSearch)).check(matches(isDisplayed()));
    }
}
