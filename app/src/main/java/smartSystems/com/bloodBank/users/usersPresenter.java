package smartSystems.com.bloodBank.users;

import smartSystems.com.bloodBank.Model.User;

public class usersPresenter implements usersContract.UserActionsListener {

    private final usersContract.View mUserView;

    public usersPresenter(usersContract.View usersView) {
        mUserView = usersView;
    }

    @Override
    public void openUserDetails() {
        mUserView.showUserMarkerDetail();
    }
}
