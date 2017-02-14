package smartSystems.com.bloodBank.users;

public interface usersContract {

    interface View {
        void showUserMarkerDetail();
    }

    interface UserActionsListener {
        void openUserDetails();
    }
}
