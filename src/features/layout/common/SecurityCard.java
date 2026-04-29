package features.layout.common;

import config.UIConfig;
import features.components.UIButton;
import features.components.UICard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SecurityCard extends UICard {
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final int SPACING_MD = 16;

    private final UIButton changePasswordButton;
    private final JLabel lblIcon;

    public SecurityCard() {
        super(12, Color.WHITE);
        setLayout(new BorderLayout());
        setShowBorder(true);
        setBorderColor(new Color(226, 232, 240));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD));

        JLabel title = new JLabel("Security");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Center panel with true centering via GridBagLayout
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        // Icon — auto-sized from AppConfig path, falls back to emoji if path is
        // null/empty
        lblIcon = new JLabel();
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        loadIcon();

        // Description — centered text, proper wrapping
        JLabel lblInfo = new JLabel(
                "<html><div style='text-align: center; width: 180px;'>"
                        + "Secure your account by changing your password periodically. "
                        + "We'll send a verification code to your registered email."
                        + "</div></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfo.setForeground(TEXT_SECONDARY);
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);

        // Button
        changePasswordButton = new UIButton("Change Password", new Color(37, 99, 235),
                new Dimension(160, 40), new Font("Segoe UI", Font.BOLD, 13), 8,
                UIButton.ButtonType.PRIMARY);
        changePasswordButton.setHoverBg(new Color(29, 78, 216));
        changePasswordButton.setPressedBg(new Color(30, 64, 175));

        // Layout: single column, centered
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridy = 0;
        centerPanel.add(lblIcon, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        centerPanel.add(lblInfo, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        centerPanel.add(changePasswordButton, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Loads the lock icon from AppConfig. If no path is set or image fails to load,
     * falls back to a Unicode lock emoji.
     */
    private void loadIcon() {
        String iconPath = UIConfig.LOCK_ICON_PATH;
        int iconSize = UIConfig.SECURITY_LOCK_ICON_SIZE;

        if (iconPath != null && !iconPath.isBlank()) {
            try {
                ImageIcon original = new ImageIcon(getClass().getResource(iconPath));
                Image scaled = original.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                lblIcon.setIcon(new ImageIcon(scaled));
                lblIcon.setText(null);
                return;
            } catch (Exception e) {
                // Fall through to default
            }
        }

        // Fallback: Unicode lock emoji
        lblIcon.setText("🔒");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, iconSize));
    }

    public JButton getChangePasswordButton() {
        return changePasswordButton;
    }
}