package smartSystems.com.bloodBank.DefaultUsersMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import smartSystems.com.bloodBank.users.usersContract;
import smartSystems.com.bloodBank.users.usersPresenter;

import static org.mockito.Mockito.verify;

public class DefaultUsersMapTest {

    private usersPresenter mUsersPresenter;

    @Mock
    private usersContract.View mUsersView;

    @Before
    public void setupNotesPresenter() {
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mUsersPresenter = new usersPresenter(mUsersView);
    }

    @Test
    public void clickOnUserMarker_ShowsDetailsUi() {
        // When clicking on marker
        mUsersPresenter.openUserDetails();

        // Then details UI is shown
        verify(mUsersView).showUserMarkerDetail();
    }

}