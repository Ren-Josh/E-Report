package features.layout.common;

import features.components.RoundedLineBorder;
import features.components.UIButton;
import features.components.UICard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountInfoCard extends UICard {
    // FIX: neutral gray border instead of bright green
    private static final Color BORDER_COLOR = new Color(210, 215, 225);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final int SPACING_SM = 8;
    private static final int SPACING_MD = 16;
    private static final int FIELD_HEIGHT = 42;

    private final JTextField nameField, phoneField, emailField, addressField, purokField, usernameField;
    private final UIButton editButton, cancelButton;

    public AccountInfoCard() {
        super(12, Color.WHITE);
        setLayout(new BorderLayout());
        setShowBorder(true);
        setBorderColor(new Color(226, 232, 240));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD));

        JLabel title = new JLabel("Account Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, SPACING_SM, 0));
        buttonPanel.setOpaque(false);

        cancelButton = new UIButton("Cancel", new Color(241, 245, 249), new Dimension(80, 36),
                new Font("Segoe UI", Font.PLAIN, 13), 8, UIButton.ButtonType.OUTLINED);
        cancelButton.setBorderColor(new Color(200, 200, 200));
        cancelButton.setVisible(false);

        editButton = new UIButton("✏️ Edit", new Color(37, 99, 235), new Dimension(100, 36),
                new Font("Segoe UI", Font.BOLD, 13), 8, UIButton.ButtonType.PRIMARY);
        editButton.setHoverBg(new Color(29, 78, 216));
        editButton.setPressedBg(new Color(30, 64, 175));

        buttonPanel.add(cancelButton);
        buttonPanel.add(editButton);
        header.add(buttonPanel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, SPACING_MD, SPACING_MD, SPACING_MD));

        nameField = createTextField();
        phoneField = createTextField();
        emailField = createTextField();
        addressField = createTextField();
        purokField = createTextField();
        usernameField = createTextField();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(SPACING_SM, 0, SPACING_SM, 0);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        form.add(createFormRow("Full Name", nameField), gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(SPACING_SM, 0, SPACING_SM, SPACING_SM);
        form.add(createFormRow("Phone Number", phoneField), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(SPACING_SM, SPACING_SM, SPACING_SM, 0);
        form.add(createFormRow("Email Address", emailField), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(SPACING_SM, 0, SPACING_SM, 0);
        form.add(createFormRow("Address", addressField), gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(SPACING_SM, 0, SPACING_SM, SPACING_SM);
        form.add(createFormRow("Purok", purokField), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(SPACING_SM, SPACING_SM, SPACING_SM, 0);
        form.add(createFormRow("Username", usernameField), gbc);

        add(form, BorderLayout.CENTER);
    }

    private JTextField createTextField() {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!isEditable()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0, 0, 0, 5));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 6),
                new EmptyBorder(8, 12, 8, 12)));
        f.setBackground(new Color(250, 250, 250));
        f.setEditable(false);
        f.setSelectionColor(new Color(186, 230, 253));
        f.setPreferredSize(new Dimension(f.getPreferredSize().width, FIELD_HEIGHT));
        return f;
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_PRIMARY);
        row.add(lbl, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    public JTextField getNameField() {
        return nameField;
    }

    public JTextField getPhoneField() {
        return phoneField;
    }

    public JTextField getEmailField() {
        return emailField;
    }

    public JTextField getAddressField() {
        return addressField;
    }

    public JTextField getPurokField() {
        return purokField;
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public JButton getEditButton() {
        return editButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        phoneField.setEditable(editable);
        emailField.setEditable(editable);
        addressField.setEditable(editable);
        purokField.setEditable(editable);
        usernameField.setEditable(editable);
    }

    public void updateFieldBackgrounds(boolean isEditing) {
        Color editableBg = Color.WHITE;
        Color readonlyBg = new Color(248, 250, 252);
        nameField.setBackground(isEditing ? editableBg : readonlyBg);
        phoneField.setBackground(isEditing ? editableBg : readonlyBg);
        emailField.setBackground(isEditing ? editableBg : readonlyBg);
        addressField.setBackground(isEditing ? editableBg : readonlyBg);
        purokField.setBackground(isEditing ? editableBg : readonlyBg);
        usernameField.setBackground(isEditing ? editableBg : readonlyBg);
    }
}