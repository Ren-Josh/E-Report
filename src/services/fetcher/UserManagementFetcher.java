package services.fetcher;

import services.controller.UserManagementController;
import features.core.usermanagement.UserData;
import java.util.List;

/**
 * Static utility facade for user management data operations.
 * Provides a thin, stateless layer over UserManagementController so that
 * UI panels do not need to instantiate or manage the controller directly.
 */
public class UserManagementFetcher {

    /** Singleton controller instance shared across all fetcher calls. */
    private static final UserManagementController controller = new UserManagementController();

    /** Private constructor prevents instantiation; this is a utility class. */
    private UserManagementFetcher() {
        // utility class
    }

    /**
     * Retrieves every user record from the database.
     * 
     * @return a list of UserData objects representing all registered users
     */
    public static List<UserData> fetchAllUsers() {
        return controller.getAllUsers();
    }

    /**
     * Searches for users matching the provided filter criteria.
     * Any parameter may be null or empty to disable filtering on that dimension.
     * 
     * @param name   partial or full name to search for
     * @param role   role string to match (e.g., "Resident", "Captain")
     * @param purok  purok string to match
     * @param status account status string to match
     * @return a list of UserData objects that satisfy all active filters
     */
    public static List<UserData> fetchFilteredUsers(String name, String role, String purok, String status) {
        return controller.searchUsers(name, role, purok, status);
    }

    /**
     * Persists changes to an existing user record.
     * 
     * @param user the UserData object containing updated fields
     * @return true if the update succeeded; false otherwise
     */
    public static boolean updateUser(UserData user) {
        return controller.updateUser(user);
    }

    /**
     * Toggles the ban (active/inactive) status of a user account.
     * Delegates to the controller to flip the current state and persist it.
     * 
     * @param user the UserData object identifying the target user
     * @return true if the toggle succeeded; false otherwise
     */
    public static boolean toggleBanStatus(UserData user) {
        return controller.toggleBanStatus(user);
    }
}