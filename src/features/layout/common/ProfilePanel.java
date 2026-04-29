package features.layout.common;

import app.E_Report;
import features.components.RoundedLineBorder;
import models.UserInfo;
import models.UserSession;
import services.controller.UserServiceController;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class ProfilePanel extends JPanel {
    private static final Color BG_MAIN = new Color(248, 250, 252);
    private static final Color BG_GRADIENT_TOP = new Color(230, 240, 255);
    private static final Color BG_GRADIENT_BOTTOM = new Color(245, 250, 255);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color ERROR = new Color(239, 68, 68);
    private static final Color WARNING = new Color(234, 179, 8);
    private static final Color BORDER_COLOR = new Color(210, 215, 225);

    private static final int SPACING_MD = 16;
    private static final int SPACING_LG = 24;

    private final E_Report app;
    private boolean isEditing = false;
    private boolean hasUnsavedChanges = false;
    private Map<String, String> originalValues = new HashMap<>();

    private final ProfileHeaderPanel headerPanel;
    private final ProfileInfoCard profileInfoCard;
    private final AccountInfoCard accountInfoCard;
    private final SecurityCard securityCard;
    private final ProfileStatusBar statusBar;

    public ProfilePanel(E_Report app) {
        this.app = app;

        headerPanel = new ProfileHeaderPanel("Profile Settings", () -> {
            if (hasUnsavedChanges) {
                int result = JOptionPane.showConfirmDialog(this,
                        "You have unsaved changes. Are you sure you want to leave?",
                        "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION)
                    return;
            }
            app.navigate("dashboard");
        });

        profileInfoCard = new ProfileInfoCard();
        accountInfoCard = new AccountInfoCard();
        securityCard = new SecurityCard();
        statusBar = new ProfileStatusBar();

        setupUI();
        setupEvents();
        loadFromApp();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(BG_MAIN);

        add(headerPanel, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);

        // Main content with equal padding on all sides
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, SPACING_MD, 0);

        // Row 0: Profile info card (full width)
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        content.add(profileInfoCard, gbc);

        // Row 1: Two cards side by side — 75% / 25% split
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Account Information: 3/4 width
        gbc.gridx = 0;
        gbc.weightx = 0.75;
        gbc.insets = new Insets(0, 0, 0, SPACING_MD / 2);
        content.add(accountInfoCard, gbc);

        // Security: 1/4 width
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        gbc.insets = new Insets(0, SPACING_MD / 2, 0, 0);
        content.add(securityCard, gbc);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar verticalBar = scroll.getVerticalScrollBar();
        verticalBar.setUnitIncrement(16);
        verticalBar.setPreferredSize(new Dimension(8, 0));

        add(scroll, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, BG_GRADIENT_TOP, 0, getHeight(), BG_GRADIENT_BOTTOM);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private void setupEvents() {
        accountInfoCard.getEditButton().addActionListener(e -> toggleEditMode());
        accountInfoCard.getCancelButton().addActionListener(e -> cancelEdit());

        securityCard.getChangePasswordButton().addActionListener(e -> app.navigate("securitypassword"));

        registerKeyboardAction(
                e -> toggleEditMode(),
                KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        accountInfoCard.getEmailField().getDocument().addDocumentListener(new ValidationListener(
                accountInfoCard.getEmailField(),
                () -> isValidEmail(accountInfoCard.getEmailField().getText()),
                "Invalid email format"));
        accountInfoCard.getPhoneField().getDocument().addDocumentListener(new ValidationListener(
                accountInfoCard.getPhoneField(),
                () -> isValidPhone(accountInfoCard.getPhoneField().getText()),
                "Invalid phone format"));

        DocumentChangeListener dirtyListener = new DocumentChangeListener();
        accountInfoCard.getNameField().getDocument().addDocumentListener(dirtyListener);
        accountInfoCard.getPhoneField().getDocument().addDocumentListener(dirtyListener);
        accountInfoCard.getEmailField().getDocument().addDocumentListener(dirtyListener);
        accountInfoCard.getAddressField().getDocument().addDocumentListener(dirtyListener);
        accountInfoCard.getPurokField().getDocument().addDocumentListener(dirtyListener);
        accountInfoCard.getUsernameField().getDocument().addDocumentListener(dirtyListener);
    }

    private void toggleEditMode() {
        isEditing = !isEditing;

        if (isEditing) {
            storeOriginalValues();
            accountInfoCard.setFieldsEditable(true);
            accountInfoCard.getEditButton().setText("✅ Save");
            accountInfoCard.getCancelButton().setVisible(true);
            showStatus("Editing profile information...", WARNING);
        } else {
            if (validateAccountInfo()) {
                saveAccountInfo();
                accountInfoCard.setFieldsEditable(false);
                accountInfoCard.getEditButton().setText("✏️ Edit");
                accountInfoCard.getCancelButton().setVisible(false);
                hasUnsavedChanges = false;
                showStatus("Profile updated successfully!", SUCCESS);
            } else {
                isEditing = true;
            }
        }

        accountInfoCard.updateFieldBackgrounds(isEditing);
        revalidate();
        repaint();
    }

    private void cancelEdit() {
        if (hasUnsavedChanges) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Discard all changes?", "Cancel Edit",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.YES_OPTION)
                return;
            restoreOriginalValues();
        }

        isEditing = false;
        accountInfoCard.setFieldsEditable(false);
        accountInfoCard.getEditButton().setText("✏️ Edit");
        accountInfoCard.getCancelButton().setVisible(false);
        hasUnsavedChanges = false;
        showStatus("Edit cancelled", TEXT_SECONDARY);
        accountInfoCard.updateFieldBackgrounds(false);
    }

    private void storeOriginalValues() {
        originalValues.put("name", accountInfoCard.getNameField().getText());
        originalValues.put("phone", accountInfoCard.getPhoneField().getText());
        originalValues.put("email", accountInfoCard.getEmailField().getText());
        originalValues.put("address", accountInfoCard.getAddressField().getText());
        originalValues.put("purok", accountInfoCard.getPurokField().getText());
        originalValues.put("username", accountInfoCard.getUsernameField().getText());
    }

    private void restoreOriginalValues() {
        accountInfoCard.getNameField().setText(originalValues.get("name"));
        accountInfoCard.getPhoneField().setText(originalValues.get("phone"));
        accountInfoCard.getEmailField().setText(originalValues.get("email"));
        accountInfoCard.getAddressField().setText(originalValues.get("address"));
        accountInfoCard.getPurokField().setText(originalValues.get("purok"));
        accountInfoCard.getUsernameField().setText(originalValues.get("username"));
    }

    private boolean validateAccountInfo() {
        if (!isValidEmail(accountInfoCard.getEmailField().getText())) {
            showStatus("Please enter a valid email address", ERROR);
            accountInfoCard.getEmailField().requestFocus();
            return false;
        }
        if (!isValidPhone(accountInfoCard.getPhoneField().getText())) {
            showStatus("Please enter a valid phone number", ERROR);
            accountInfoCard.getPhoneField().requestFocus();
            return false;
        }
        if (accountInfoCard.getNameField().getText().trim().isEmpty()) {
            showStatus("Name cannot be empty", ERROR);
            accountInfoCard.getNameField().requestFocus();
            return false;
        }
        return true;
    }

    private void saveAccountInfo() {
        profileInfoCard.setDisplayName(accountInfoCard.getNameField().getText());
        profileInfoCard.setDisplayRole("User");
    }

    private void showStatus(String message, Color color) {
        statusBar.showStatus(message, color);
    }

    public void loadFromApp() {
        if (app == null) {
            showStatus("App reference not available", ERROR);
            return;
        }

        try {
            UserInfo ui = app.getUserInfo();
            UserSession us = app.getUserSession();

            if (ui == null && us != null) {
                UserServiceController service = new UserServiceController();
                ui = service.getUserInfo(us.getUserId());
                if (ui != null) {
                    app.setUserInfo(ui);
                }
            }

            if (ui != null && us != null) {
                StringBuilder nameBuilder = new StringBuilder();
                if (notEmpty(ui.getFName()))
                    nameBuilder.append(ui.getFName()).append(" ");
                if (notEmpty(ui.getMName()))
                    nameBuilder.append(ui.getMName()).append(" ");
                if (notEmpty(ui.getLName()))
                    nameBuilder.append(ui.getLName());
                String fullName = nameBuilder.toString().trim();

                String phone = safeString(ui.getContact());
                String email = safeString(ui.getEmail());

                StringBuilder addrBuilder = new StringBuilder();
                if (notEmpty(ui.getHouseNum()))
                    addrBuilder.append(ui.getHouseNum()).append(" ");
                if (notEmpty(ui.getStreet()))
                    addrBuilder.append(ui.getStreet());
                String address = addrBuilder.toString().trim();

                String purok = safeString(ui.getPurok());
                String username = (app.getUserSession() != null) ? safeString(app.getUserSession().getUsername()) : "";
                String role = safeString(us.getRole());

                setProfileData(fullName, phone, email, address, purok, username, role);
                showStatus("Profile loaded successfully", TEXT_SECONDARY);
            } else {
                showStatus("No user session found. Please log in.", WARNING);
            }
        } catch (Exception e) {
            showStatus("Error loading profile data", ERROR);
            e.printStackTrace();
        }
    }

    public void setProfileData(String name, String phone, String email, String address,
            String purok, String username, String role) {
        SwingUtilities.invokeLater(() -> {
            accountInfoCard.getNameField().setText(name);
            accountInfoCard.getPhoneField().setText(phone);
            accountInfoCard.getEmailField().setText(email);
            accountInfoCard.getAddressField().setText(address);
            accountInfoCard.getPurokField().setText(purok);
            accountInfoCard.getUsernameField().setText(username);
            profileInfoCard.setDisplayName(name.isEmpty() ? "User" : name);
            profileInfoCard.setDisplayRole(role);
        });
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^\\+?[0-9\\-\\s\\(\\)]{10,}$");
    }

    private String safeString(String value) {
        return (value != null) ? value : "";
    }

    private boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private class DocumentChangeListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            handleChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            handleChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            handleChange();
        }

        private void handleChange() {
            hasUnsavedChanges = isEditing;
            if (isEditing) {
                accountInfoCard.updateFieldBackgrounds(true);
            }
        }
    }

    private class ValidationListener implements DocumentListener {
        private final JComponent field;
        private final java.util.function.BooleanSupplier validator;
        private final String errorMessage;

        ValidationListener(JComponent field, java.util.function.BooleanSupplier validator, String errorMessage) {
            this.field = field;
            this.validator = validator;
            this.errorMessage = errorMessage;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validate();
        }

        private void validate() {
            SwingUtilities.invokeLater(() -> {
                boolean isValid = validator.getAsBoolean();
                field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedLineBorder(isValid ? BORDER_COLOR : ERROR, 6),
                        new EmptyBorder(8, 12, 8, 12)));
                if (!isValid && field.hasFocus()) {
                    showStatus(errorMessage, ERROR);
                }
            });
        }
    }
}