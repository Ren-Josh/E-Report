package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.HeaderPanel;
import features.components.NavPanel;
import features.core.BackgroundPanel;
import features.core.usermanagement.EditUserPanel;
import features.core.usermanagement.UserData;
import features.core.usermanagement.UserManagementPanel;
import services.fetcher.UserManagementFetcher;

import javax.swing.*;
import java.awt.*;

public class UserManagementView extends JPanel {

    private final E_Report app;

    private HeaderPanel header;
    private NavPanel nav;

    private CardLayout contentCardLayout;
    private JPanel contentPanel;
    private UserManagementPanel userManagementPanel;
    private EditUserPanel editUserPanel;

    public UserManagementView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout(15, 15));
        bgPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        header = new HeaderPanel(app);
        nav = new NavPanel();

        bgPanel.add(header, BorderLayout.NORTH);
        bgPanel.add(nav, BorderLayout.WEST);

        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setOpaque(false);

        userManagementPanel = createUserManagementPanel();
        editUserPanel = createEditUserPanel();

        contentPanel.add(userManagementPanel, "LIST");
        contentPanel.add(editUserPanel, "EDIT");

        bgPanel.add(contentPanel, BorderLayout.CENTER);

        String role = app.getUserSession().getRole().toLowerCase();
        if (role.equals("captain")) {
            nav.setCaptainMenus(route -> app.navigate(route));
        } else if (role.equals("secretary")) {
            nav.setSecretaryMenus(route -> app.navigate(route));
        } else if (role.equals("resident")) {
            nav.setResidentMenus(route -> app.navigate(route));
        }

        add(bgPanel, BorderLayout.CENTER);
    }

    private UserManagementPanel createUserManagementPanel() {
        UserManagementPanel panel = new UserManagementPanel();

        panel.setUserActionListener(new UserManagementPanel.UserActionListener() {
            @Override
            public void onEdit(int rowIndex, UserData user) {
                editUserPanel.setUserData(user);
                contentCardLayout.show(contentPanel, "EDIT");
            }

            @Override
            public void onBanToggle(int rowIndex, UserData user, boolean currentlyBanned) {
                user.setBanned(!currentlyBanned);
                if (UserManagementFetcher.toggleBanStatus(user)) {
                    panel.refreshDisplayedData();
                } else {
                    user.setBanned(currentlyBanned);
                }
            }
        });

        panel.setFilterListener(new UserManagementPanel.FilterListener() {
            @Override
            public void onFilterChanged(String name, String role, String purok, String status) {
                System.out.println("Filters — Name: " + name
                        + ", Role: " + role
                        + ", Purok: " + purok
                        + ", Status: " + status);
            }
        });

        panel.setData(UserManagementFetcher.fetchAllUsers());
        return panel;
    }

    private EditUserPanel createEditUserPanel() {
        return new EditUserPanel(new EditUserPanel.EditListener() {
            @Override
            public void onPasswordVerified() {
                // Panel internally switches from PASSWORD to EDIT card
            }

            @Override
            public void onUserSaved(UserData updatedUser) {
                if (UserManagementFetcher.updateUser(updatedUser)) {
                    userManagementPanel.refreshDisplayedData();
                    editUserPanel.resetToPasswordPanel();
                    contentCardLayout.show(contentPanel, "LIST");
                }
            }

            @Override
            public void onCancelled() {
                editUserPanel.resetToPasswordPanel();
                contentCardLayout.show(contentPanel, "LIST");
            }
        });
    }

    public HeaderPanel getHeader() {
        return header;
    }

    public NavPanel getNavPanel() {
        return nav;
    }

    public UserManagementPanel getUserManagementPanel() {
        return userManagementPanel;
    }

    public EditUserPanel getEditUserPanel() {
        return editUserPanel;
    }

    public void showListView() {
        editUserPanel.resetToPasswordPanel();
        contentCardLayout.show(contentPanel, "LIST");
    }

    public void showEditView(UserData user) {
        editUserPanel.setUserData(user);
        contentCardLayout.show(contentPanel, "EDIT");
    }
}