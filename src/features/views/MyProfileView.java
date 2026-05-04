package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.layout.common.profile.ProfilePanel;
import models.UserSession;

import javax.swing.*;
import java.awt.*;

/**
 * Composite view panel for the "My Profile" screen.
 * Displays the user's profile information with edit capabilities, and provides
 * navigation to the security password change screen. Ensures any active edit
 * mode is cancelled before leaving the view.
 */
public class MyProfileView extends JPanel {
    /**
     * Reference to the main application frame for session and navigation access.
     */
    private final E_Report app;
    /** Top header bar displaying user info and system branding. */
    private final HeaderPanel header;
    /** Left-side navigation panel with role-specific menu items. */
    private final NavPanel nav;
    /** Central panel containing the profile form and edit controls. */
    private ProfilePanel profilePanel;

    /**
     * Constructs the My Profile view, assembling header, navigation, and profile
     * content. Wraps the security password navigation callback to ensure
     * edit mode is cancelled before switching views.
     * 
     * @param app the main E_Report application frame
     */
    public MyProfileView(E_Report app) {
        this.app = app;

        // Instantiate shared chrome components.
        header = new HeaderPanel(app);
        nav = new NavPanel();

        // Build the profile panel with a callback that cancels edits and navigates
        // to the security password change screen.
        profilePanel = new ProfilePanel(app, () -> {
            profilePanel.cancelEditOnNavigate();
            app.navigate("securitypassword");
        });

        // Assemble the visual layout and configure role-based menus.
        initializeLayout();
        setupNavigation();
    }

    /**
     * Assembles the background panel and places header, nav, and profile content.
     * Uses BorderLayout with consistent gaps and padding.
     */
    private void initializeLayout() {
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);
        bgPanel.add(profilePanel, BorderLayout.CENTER);

        add(bgPanel, BorderLayout.CENTER);
    }

    /**
     * Configures the navigation menu based on the current user's role.
     * Wraps every navigation action to cancel any active profile edit before
     * switching views, preventing accidental data loss.
     */
    private void setupNavigation() {
        UserSession session = app.getUserSession();
        if (session == null)
            return;

        String role = session.getRole();

        // Create a wrapped navigator that always cancels edits before routing.
        java.util.function.Consumer<String> wrappedNavigator = route -> {
            profilePanel.cancelEditOnNavigate();
            app.navigate(route);
        };

        // Assign the appropriate menu set based on role.
        if (role.equalsIgnoreCase("captain")) {
            nav.setCaptainMenus(wrappedNavigator);
        } else if (role.equalsIgnoreCase("secretary")) {
            nav.setSecretaryMenus(wrappedNavigator);
        } else if (role.equalsIgnoreCase("resident")) {
            nav.setResidentMenus(wrappedNavigator);
        }
    }

    /** @return the header panel instance */
    public HeaderPanel getHeader() {
        return header;
    }

    /** @return the navigation panel instance */
    public NavPanel getNavPanel() {
        return nav;
    }

    /** @return the profile content panel instance */
    public ProfilePanel getProfilePanel() {
        return profilePanel;
    }

    /**
     * Refreshes the profile data and header display.
     * Typically called after a successful profile update or password change
     * to ensure the UI reflects the latest stored values.
     */
    public void refreshProfile() {
        profilePanel.loadFromApp();
        header.refresh();
    }
}