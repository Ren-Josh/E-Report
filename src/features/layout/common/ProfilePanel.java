package features.layout.common;

import app.E_Report;
import config.UIConfig;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class ProfilePanel extends JPanel {

    // ========== Colors ==========
    private static final Color BG_TOP = new Color(186, 225, 255);
    private static final Color BG_BOTTOM = new Color(214, 237, 255);
    private static final Color CARD_BG = new Color(255, 255, 255, 230);
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_DARK = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color HEADER_BG = new Color(255, 255, 255, 180);

    // ========== App Reference ==========
    private final E_Report app;

    // ========== Account Fields ==========
    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField emailField;
    private final JTextField addressField;
    private final JTextField purokField;
    private final JTextField usernameField;

    // ========== Password Fields ==========
    private final JPasswordField currentPassField;
    private final JPasswordField newPassField;
    private final JPasswordField confirmPassField;

    // ========== Display ==========
    private final JLabel displayNameLabel;
    private final JLabel displayRoleLabel;
    private final JLabel avatarLabel;

    // ========== Buttons ==========
    private final JButton backButton;
    private final JButton editButton;
    private final JButton savePassButton;

    // ========== State ==========
    private boolean isEditing = false;

    public ProfilePanel(E_Report app) {
        this.app = app;

        // --- Instantiate ALL fields first ---
        nameField = createTextField();
        phoneField = createTextField();
        emailField = createTextField();
        addressField = createTextField();
        purokField = createTextField();
        usernameField = createTextField();

        currentPassField = createPasswordField();
        newPassField = createPasswordField();
        confirmPassField = createPasswordField();

        displayNameLabel = new JLabel("User");
        displayNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        displayNameLabel.setForeground(TEXT_DARK);

        displayRoleLabel = new JLabel("Role");
        displayRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        displayRoleLabel.setForeground(TEXT_MUTED);

        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(56, 56));
        avatarLabel.setMinimumSize(new Dimension(56, 56));

        backButton = createIconButton("\u2190 Back");
        editButton = createIconButton("\u270E Edit");
        savePassButton = createPrimaryButton("Save Changes");

        // --- Layout ---
        setLayout(new BorderLayout());
        setOpaque(true);

        add(createHeaderBar(), BorderLayout.NORTH);
        add(createScrollableContent(), BorderLayout.CENTER);

        // --- Events ---
        backButton.addActionListener(e -> app.navigate("dashboard"));
        editButton.addActionListener(e -> toggleEditMode());
        savePassButton.addActionListener(e -> savePassword());

        // --- Load Data ---
        loadFromApp();
    }

    // ==================== HEADER ====================

    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(true);
        bar.setBackground(HEADER_BG);
        bar.setBorder(new EmptyBorder(14, 20, 14, 20));

        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bar.add(backButton, BorderLayout.WEST);

        JLabel title = new JLabel("Profile Setting");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(title);
        bar.add(centerWrap, BorderLayout.CENTER);

        return bar;
    }

    // ==================== SCROLLABLE CONTENT ====================

    private JScrollPane createScrollableContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 24, 24));

        content.add(createProfileCard());
        content.add(Box.createVerticalStrut(16));

        JPanel formsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        formsRow.setOpaque(false);
        formsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        formsRow.setPreferredSize(new Dimension(800, 380));
        formsRow.add(createAccountCard());
        formsRow.add(createPasswordCard());
        content.add(formsRow);

        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    // ==================== PROFILE CARD ====================

    private JPanel createProfileCard() {
        JPanel card = createRoundedPanel(CARD_BG);
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setPreferredSize(new Dimension(800, 85));

        // Avatar
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int s = Math.min(getWidth(), getHeight());
                Ellipse2D c = new Ellipse2D.Float(0, 0, s, s);
                g2.setColor(new Color(148, 163, 184));
                g2.fill(c);

                String txt = displayNameLabel.getText();
                String initial = (txt != null && !txt.isEmpty()) ? txt.substring(0, 1).toUpperCase() : "U";
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (s - fm.stringWidth(initial)) / 2,
                        ((s - fm.getHeight()) / 2) + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(56, 56));
        avatar.setOpaque(false);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.add(displayNameLabel);
        textPanel.add(displayRoleLabel);

        card.add(avatar);
        card.add(textPanel);
        return card;
    }

    // ==================== ACCOUNT CARD ====================

    private JPanel createAccountCard() {
        JPanel card = createRoundedPanel(CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Account Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        // Row 0: Name (full width)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        form.add(wrapField("Name", nameField), gbc);

        // Row 1: Phone | Email
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(5, 0, 5, 8);
        form.add(wrapField("Phone Number", phoneField), gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 8, 5, 0);
        form.add(wrapField("Email", emailField), gbc);

        // Row 2: Address (full width)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 0);
        form.add(wrapField("Address", addressField), gbc);

        // Row 3: Purok | Username
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(5, 0, 5, 8);
        form.add(wrapField("Purok", purokField), gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 8, 5, 0);
        form.add(wrapField("Username", usernameField), gbc);

        // Row 4: Edit button (right)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 0, 0, 0);
        form.add(editButton, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    // ==================== PASSWORD CARD ====================

    private JPanel createPasswordCard() {
        JPanel card = createRoundedPanel(CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Change Password");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(wrapField("Current Password", currentPassField), gbc);
        gbc.gridy = 1;
        form.add(wrapField("New Password", newPassField), gbc);
        gbc.gridy = 2;
        form.add(wrapField("Confirm Password", confirmPassField), gbc);

        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(savePassButton, gbc);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    // ==================== HELPERS ====================

    private JPanel createRoundedPanel(Color bg) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField createTextField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        f.setBackground(new Color(250, 250, 250));
        f.setEditable(false);
        return f;
    }

    private JPasswordField createPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        f.setBackground(Color.WHITE);
        f.setEchoChar('\u2022');
        return f;
    }

    private JButton createPrimaryButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY_BLUE.darker()
                        : getModel().isRollover() ? PRIMARY_BLUE.brighter() : PRIMARY_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(115, 34));
        return b;
    }

    private JButton createIconButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(TEXT_DARK);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ==================== LOGIC ====================

    private void toggleEditMode() {
        isEditing = !isEditing;
        boolean editable = isEditing;

        nameField.setEditable(editable);
        phoneField.setEditable(editable);
        emailField.setEditable(editable);
        addressField.setEditable(editable);
        purokField.setEditable(editable);
        usernameField.setEditable(editable);

        Color bg = editable ? Color.WHITE : new Color(250, 250, 250);
        nameField.setBackground(bg);
        phoneField.setBackground(bg);
        emailField.setBackground(bg);
        addressField.setBackground(bg);
        purokField.setBackground(bg);
        usernameField.setBackground(bg);

        editButton.setText(editable ? "\u2713 Save" : "\u270E Edit");

        if (!editable) {
            saveAccountInfo();
        }
    }

    private void saveAccountInfo() {
        displayNameLabel.setText(nameField.getText());
        // TODO: persist to DB via app or controller
    }

    private void savePassword() {
        String cur = new String(currentPassField.getPassword());
        String nw = new String(newPassField.getPassword());
        String cf = new String(confirmPassField.getPassword());

        if (cur.isEmpty() || nw.isEmpty() || cf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!nw.equals(cf)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentPassField.setText("");
        newPassField.setText("");
        confirmPassField.setText("");

        JOptionPane.showMessageDialog(this, "Password updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        // TODO: persist to DB
    }

    // ==================== DATA BINDING ====================

    private void loadFromApp() {
        if (app == null)
            return;

        // Adjust these getters to match your actual model classes
        String name = "";
        String phone = "";
        String email = "";
        String address = "";
        String purok = "";
        String username = "";
        String role = "User";

        if (app.getUserInfo() != null) {
            // Example: app.getUserInfo().getFirstName() etc.
            // Replace with your actual field names
            name = safeString(app.getUserInfo().toString()); // placeholder
            phone = "";
            email = "";
            address = "";
            purok = "";
        }

        if (app.getUserSession() != null) {
            // role = app.getUserSession().getRole();
        }

        // Set values
        nameField.setText(name);
        phoneField.setText(phone);
        emailField.setText(email);
        addressField.setText(address);
        purokField.setText(purok);
        usernameField.setText(username);

        displayNameLabel.setText(name.isEmpty() ? "User" : name);
        displayRoleLabel.setText(role);
    }

    private String safeString(Object o) {
        return o != null ? o.toString() : "";
    }

    // ==================== PUBLIC API ====================

    public void setProfileData(String name, String phone, String email,
            String address, String purok, String username, String role) {
        nameField.setText(name);
        phoneField.setText(phone);
        emailField.setText(email);
        addressField.setText(address);
        purokField.setText(purok);
        usernameField.setText(username);
        displayNameLabel.setText(name);
        displayRoleLabel.setText(role);
    }

    public String getFieldName() {
        return nameField.getText();
    }

    public String getFieldPhone() {
        return phoneField.getText();
    }

    public String getFieldEmail() {
        return emailField.getText();
    }

    public String getFieldAddress() {
        return addressField.getText();
    }

    public String getFieldPurok() {
        return purokField.getText();
    }

    public String getFieldUsername() {
        return usernameField.getText();
    }
}