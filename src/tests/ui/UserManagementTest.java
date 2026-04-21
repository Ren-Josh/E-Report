package tests.ui;

import javax.swing.*;
import java.awt.*;
import features.core.usermanagement.EditUserPanel;
import features.core.usermanagement.UserData;
import features.core.usermanagement.UserManagementPanel;

public class UserManagementTest extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private UserManagementPanel userListPanel;
    private EditUserPanel editUserPanel;

    public UserManagementTest() {
        setTitle("User Management System - Test Environment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // 1. Initialize Layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(245, 247, 250));

        // 2. Initialize the Edit Panel first (so we can reference it in the listener)
        editUserPanel = new EditUserPanel(new EditUserPanel.EditListener() {
            @Override
            public void onPasswordVerified() {
                System.out.println("Trace: Secretary password verified.");
            }

            @Override
            public void onUserSaved(UserData updatedUser) {
                System.out.println("Trace: Saving user " + updatedUser.getName());
                // In a real app, you'd update the DB here
                cardLayout.show(mainPanel, "USER_LIST");
                JOptionPane.showMessageDialog(UserManagementTest.this, "User updated successfully!");
            }

            @Override
            public void onCancelled() {
                System.out.println("Trace: Edit cancelled.");
                cardLayout.show(mainPanel, "USER_LIST");
            }
        });

        // 3. Initialize the Management Panel
        userListPanel = new UserManagementPanel(
                new UserManagementPanel.UserActionListener() {
                    @Override
                    public void onEdit(int rowIndex, UserData user) {
                        System.out.println("Trace: Opening edit for " + user.getName());
                        editUserPanel.setUserData(user);
                        cardLayout.show(mainPanel, "EDIT_FORM");
                    }

                    @Override
                    public void onBanToggle(int rowIndex, UserData user, boolean currentlyBanned) {
                        String action = currentlyBanned ? "unban" : "ban";
                        int choice = JOptionPane.showConfirmDialog(UserManagementTest.this,
                                "Are you sure you want to " + action + " " + user.getName() + "?",
                                "Confirm Action", JOptionPane.YES_NO_OPTION);

                        if (choice == JOptionPane.YES_OPTION) {
                            user.setBanned(!currentlyBanned);
                            // Refresh the UI - in this simple test, we just repaint
                            userListPanel.repaint();
                            System.out.println(
                                    "Trace: User " + user.getName() + " ban status changed to " + user.isBanned());
                        }
                    }
                },
                new UserManagementPanel.FilterListener() {
                    @Override
                    public void onFilterChanged(String name, String role, String purok, String street, String status) {
                        System.out.println(String.format("Trace: Filter Applied -> Name: %s, Role: %s, Purok: %s", name,
                                role, purok));
                    }
                });

        // 4. Assemble
        mainPanel.add(userListPanel, "USER_LIST");
        mainPanel.add(editUserPanel, "EDIT_FORM");

        add(mainPanel);

        // Start on the list view
        cardLayout.show(mainPanel, "USER_LIST");
    }

    public static void main(String[] args) {
        // Set Look and Feel to System default for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new UserManagementTest().setVisible(true);
        });
    }
}
