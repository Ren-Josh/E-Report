package features.layout.common;

import features.components.UIButton;
import features.components.UICard;
import features.components.UIPasswordInput;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SecurityCard extends UICard {
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final int SPACING_SM = 8;
    private static final int SPACING_MD = 16;
    private static final int SPACING_XS = 4;
    private static final int FIELD_HEIGHT = 42;

    private final UIPasswordInput currentPassField, newPassField, confirmPassField;
    private final UIButton savePassButton;
    private final JProgressBar passwordStrengthBar;
    private final JLabel passwordStrengthLabel;

    public SecurityCard() {
        super(12, Color.WHITE);
        setLayout(new BorderLayout());
        setShowBorder(true);
        setBorderColor(new Color(226, 232, 240));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD));

        JLabel title = new JLabel("Security");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, SPACING_MD, SPACING_MD, SPACING_MD));

        currentPassField = createPasswordField("Enter current password");
        newPassField = createPasswordField("Enter new password");
        confirmPassField = createPasswordField("Confirm new password");

        // Wire confirm field to validate against new field
        confirmPassField.setMatchTarget(newPassField);

        passwordStrengthBar = new JProgressBar(0, 100);
        passwordStrengthBar.setPreferredSize(new Dimension(1, 4));
        passwordStrengthBar.setStringPainted(false);
        passwordStrengthBar.setVisible(false);

        passwordStrengthLabel = new JLabel();
        passwordStrengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        passwordStrengthLabel.setForeground(TEXT_SECONDARY);

        savePassButton = new UIButton("Save Changes", new Color(37, 99, 235),
                new Dimension(120, 40), new Font("Segoe UI", Font.BOLD, 13), 8,
                UIButton.ButtonType.PRIMARY);
        savePassButton.setHoverBg(new Color(29, 78, 216));
        savePassButton.setPressedBg(new Color(30, 64, 175));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(SPACING_SM, 0, SPACING_SM, 0);
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(createPasswordRow("Current Password", currentPassField), gbc);

        gbc.gridy = 1;
        JPanel newPassPanel = createPasswordRow("New Password", newPassField);
        newPassPanel.add(createPasswordStrengthPanel(), BorderLayout.SOUTH);
        form.add(newPassPanel, gbc);

        gbc.gridy = 2;
        form.add(createPasswordRow("Confirm Password", confirmPassField), gbc);

        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(SPACING_MD, 0, 0, 0);
        form.add(savePassButton, gbc);

        add(form, BorderLayout.CENTER);
    }

    private UIPasswordInput createPasswordField(String placeholder) {
        UIPasswordInput f = new UIPasswordInput(20);
        f.setPlaceholder(placeholder);
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, FIELD_HEIGHT));
        return f;
    }

    private JPanel createPasswordRow(String label, UIPasswordInput field) {
        JPanel row = new JPanel(new BorderLayout(0, SPACING_XS));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_PRIMARY);
        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JPanel createPasswordStrengthPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, SPACING_XS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(SPACING_XS, 0, 0, 0));
        panel.add(passwordStrengthBar, BorderLayout.NORTH);
        panel.add(passwordStrengthLabel, BorderLayout.CENTER);
        return panel;
    }

    public UIPasswordInput getCurrentPassField() {
        return currentPassField;
    }

    public UIPasswordInput getNewPassField() {
        return newPassField;
    }

    public UIPasswordInput getConfirmPassField() {
        return confirmPassField;
    }

    public JButton getSavePassButton() {
        return savePassButton;
    }

    public JProgressBar getPasswordStrengthBar() {
        return passwordStrengthBar;
    }

    public JLabel getPasswordStrengthLabel() {
        return passwordStrengthLabel;
    }
}