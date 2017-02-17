package smartSystems.com.bloodBank;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.UiThreadTestRule;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import smartSystems.com.bloodBank.Activities.DefaultMapsActivity;
import smartSystems.com.bloodBank.Model.User;
import smartSystems.com.bloodBank.Places.Place;
import utils.LocationGenerator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.assertj.core.api.Assertions.assertThat;

import org.exparity.stub.random.RandomBuilder;

public class DefaultMapsActivityTest {

    @Rule
    public ActivityTestRule<DefaultMapsActivity> mActivityRule = new ActivityTestRule(DefaultMapsActivity.class);

    @Rule
    public UiThreadTestRule threadTestRule = new UiThreadTestRule();

    private GoogleMap googleMap;
    private CountingIdlingResource countingIdlingResource = new CountingIdlingResource("MapReady");

    /**
     * Before each.
     *
     * @throws Throwable the throwable
     */
    @Before
    public void beforeEach() throws Throwable {
        countingIdlingResource.increment();

        final SupportMapFragment mapFragment = (SupportMapFragment) mActivityRule.getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.map);

        final OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                countingIdlingResource.decrement();
                DefaultMapsActivityTest.this.googleMap = googleMap;
            }
        };

        threadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapFragment.getMapAsync(onMapReadyCallback);
            }
        });
        assertThat(Espresso.registerIdlingResources(countingIdlingResource)).isTrue();
    }


    /**
     * Draw places on map should add maker for each place.
     *
     * @throws Throwable the throwable
     */
    @Test
    public void drawPlacesOnMapShouldAddMakerForEachPlace() throws Throwable {
        final List<Place> places = RandomBuilder.aRandomListOf(Place.class);
        for (Place place : places) {
            place.setLatitude(LocationGenerator.latitude());
            place.setLongitude(LocationGenerator.longitude());
        }

        Log.i("places", String.valueOf(places.size()));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Place place : places) {
                    Marker marker = googleMap.addMarker(
                            new MarkerOptions().position(new LatLng(place.getLatitude(), place.getLongitude())));
                }
            }
        });
    }

    @Test
    public void clickOnSearchButton_ShowsAdvancedSearch() {

        onView(withId(R.id.fabSearch)).perform(click());

        // Check that DefaultMaps Activity was opened.
        onView(withId(R.id.btnBeginSearch)).check(matches(isDisplayed()));
    }


}
