package features.core.usermanagement;

import javax.swing.*;
import java.awt.*;

public class EditUserPanel extends JPanel {

    public interface EditListener {
        void onPasswordVerified();

        void onUserSaved(UserData updatedUser);

        void onCancelled();
    }

    private final EditListener editListener;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private final PasswordVerificationPanel passwordPanel;
    private final UserEditFormPanel editFormPanel;

    private UserData currentUser;
    private String secretaryPassword = "password123";

    public EditUserPanel(EditListener listener) {
        this.editListener = listener;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        passwordPanel = new PasswordVerificationPanel(new PasswordVerificationPanel.Listener() {
            @Override
            public void onVerified() {
                if (editListener != null)
                    editListener.onPasswordVerified();
                cardLayout.show(contentPanel, "EDIT");
            }

            @Override
            public void onCancelled() {
                if (editListener != null)
                    editListener.onCancelled();
            }
        });

        editFormPanel = new UserEditFormPanel(new UserEditFormPanel.Listener() {
            @Override
            public void onSaved(UserData user) {
                if (editListener != null)
                    editListener.onUserSaved(user);
            }

            @Override
            public void onCancelled() {
                if (editListener != null)
                    editListener.onCancelled();
            }
        });

        contentPanel.add(wrapCard(passwordPanel), "PASSWORD");
        contentPanel.add(wrapCard(editFormPanel), "EDIT");

        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "PASSWORD");
    }

    private JPanel wrapCard(JPanel inner) {
        features.components.UICard card = new features.components.UICard(16, Color.WHITE);
        card.setShadowEnabled(true);
        card.setShadowOffset(4, 4);
        card.setLayout(new BorderLayout());
        card.add(inner, BorderLayout.CENTER);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return card;
    }

    public void setUserData(UserData user) {
        this.currentUser = user;
        editFormPanel.loadUser(user);
        passwordPanel.clear();
        editFormPanel.clearError();
        cardLayout.show(contentPanel, "PASSWORD");
    }

    public void resetToPasswordPanel() {
        passwordPanel.clear();
        cardLayout.show(contentPanel, "PASSWORD");
    }

    public void clearForm() {
        passwordPanel.clear();
        editFormPanel.clear();
    }

    public void setSecretaryPassword(String password) {
        this.secretaryPassword = password;
        passwordPanel.setExpectedPassword(password);
    }

    public UserData getCurrentUser() {
        return currentUser;
    }
}