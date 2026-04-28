package features.core.usermanagement;

import config.UIConfig;
import features.components.UIButton;
import features.components.UIPasswordInput;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PasswordVerificationPanel extends JPanel {

    public interface Listener {
        void onVerified();

        void onCancelled();
    }

    private final Listener listener;
    private final UIPasswordInput passwordField;
    private final JLabel errorLabel;
    private String expectedPassword = "password123";

    public PasswordVerificationPanel(Listener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title with lock icon
        JLabel titleLabel = new JLabel("Secretary Verification Required", loadIcon(UIConfig.LOCK_ICON_PATH, 20),
                SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setIconTextGap(10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelAdd(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Please enter your password to edit user information", SwingConstants.CENTER);
        subtitleLabel.setFont(UIConfig.BODY);
        subtitleLabel.setForeground(new Color(100, 100, 100));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 20, 10);
        panelAdd(subtitleLabel, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(UIConfig.BODY);
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 5, 10);
        panelAdd(passwordLabel, gbc);

        passwordField = new UIPasswordInput(20);
        passwordField.setPreferredSize(new Dimension(250, 38));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPlaceholder("Enter secretary password");
        passwordField.setIdleBorderColor(new Color(200, 200, 200));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 5, 10);
        panelAdd(passwordField, gbc);

        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 60, 60));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 10, 15, 10);
        panelAdd(errorLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        UIButton verifyButton = new UIButton(
                "Verify",
                new Color(25, 118, 210),
                new Dimension(100, 35),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.PRIMARY);
        verifyButton.addActionListener(e -> verify());

        UIButton cancelButton = new UIButton(
                "Cancel",
                Color.WHITE,
                new Dimension(100, 35),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.OUTLINED);
        cancelButton.addActionListener(e -> {
            if (listener != null)
                listener.onCancelled();
        });

        buttonPanel.add(verifyButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 10, 10, 10);
        panelAdd(buttonPanel, gbc);

        passwordField.addActionListener(e -> verify());
    }

    private void panelAdd(Component comp, GridBagConstraints gbc) {
        add(comp, gbc);
    }

    private void verify() {
        String entered = passwordField.getValue();

        if (entered.isEmpty()) {
            errorLabel.setText("Please enter a password");
            return;
        }
        if (entered.equals(expectedPassword)) {
            errorLabel.setText("");
            passwordField.setText("");
            if (listener != null)
                listener.onVerified();
        } else {
            errorLabel.setText("Incorrect password. Please try again.");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    public void setExpectedPassword(String password) {
        this.expectedPassword = password;
    }

    public void clear() {
        passwordField.setText("");
        errorLabel.setText("");
    }

    private static ImageIcon loadIcon(String path, int size) {
        try {
            Image src = new ImageIcon(path).getImage();
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(src, 0, 0, size, size, null);
            g2d.dispose();
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }
}