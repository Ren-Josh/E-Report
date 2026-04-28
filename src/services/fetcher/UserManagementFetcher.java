package services.fetcher;

import services.controller.UserManagementController;
import features.core.usermanagement.UserData;
import java.util.List;

public class UserManagementFetcher {

    private static final UserManagementController controller = new UserManagementController();

    private UserManagementFetcher() {
        // utility class
    }

    public static List<UserData> fetchAllUsers() {
        return controller.getAllUsers();
    }

    public static List<UserData> fetchFilteredUsers(String name, String role, String purok, String status) {
        return controller.searchUsers(name, role, purok, status);
    }

    public static boolean updateUser(UserData user) {
        return controller.updateUser(user);
    }

    public static boolean toggleBanStatus(UserData user) {
        return controller.toggleBanStatus(user);
    }
}