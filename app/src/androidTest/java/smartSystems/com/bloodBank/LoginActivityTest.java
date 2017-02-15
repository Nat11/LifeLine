package smartSystems.com.bloodBank;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import smartSystems.com.bloodBank.Activities.LoginActivity;
import smartSystems.com.bloodBank.Activities.UserActivity;
import smartSystems.com.bloodBank.IdlingResource.SimpleIdlingResource;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule(LoginActivity.class);

    private SimpleIdlingResource mIdlingResource;

    @Before
    public void registerIntentServiceIdlingResource() {
        LoginActivity activity = mActivityRule.getActivity();
        mIdlingResource = new SimpleIdlingResource(activity);
        Espresso.registerIdlingResources(mIdlingResource);
    }

    @After
    public void unregisterIntentServiceIdlingResource() {
        Espresso.unregisterIdlingResources(mIdlingResource);
    }


    @Test
    public void login() {
        String username = "natalio.otayek@hotmail.com";
        String password = "123456";

        // Add Email and pwd
        onView(withId(R.id.etEmailAddr)).perform(typeText(username), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText(password),
                closeSoftKeyboard()); // Type new description and close the keyboard

        onView(withId(R.id.btnLogin)).perform(click());

        // Sleep to validate credentials with firebase
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // check if user activity is opened
        onView(withId(R.id.txtUser)).check(matches(isDisplayed()));

    }

    @Test
    public void register() {
        onView(withId(R.id.btnRegister)).perform(click());

        // check if register activity is opened
        onView(withId(R.id.AutoCompleteAddress)).check(matches(isDisplayed()));
    }

    @Test
    public void resetPassword() {
        onView(withId(R.id.btnForgetPass)).perform(click());

        // check if forgot password activity is opened
        onView(withId(R.id.btnResetPass)).check(matches(isDisplayed()));
    }
}
