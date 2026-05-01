package features.views;

import app.E_Report;
import config.AppConfig;
import config.UIConfig;
import features.components.*;
import features.core.BackgroundPanel;
import features.core.FormLayoutUtils;
import models.Credential;
import models.UserInfo;
import services.controller.UserServiceController;
import services.validation.UIValidator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class RegisterView extends JPanel {
    private E_Report app;
    private CardLayout cardLayout;
    private JPanel formContainer;
    private String fName, mName, lName, sex, contact, email, houseNum, purok, username, password,
            confirmPassword;
    private UIInput txtFName, txtMName, txtLName, txtContact, txtEmail, txtHouseNum, txtUsername;
    private UIPasswordInput txtPassword, txtConfirmPassword;
    private UIRadioButtonGroup rbgSex;
    private UIComboBox<String> cbPurok;

    // Live status label for password feedback
    private JLabel lblCredentialStatus;

    public RegisterView(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        BackgroundPanel bgPanel = new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        headerPanel.setOpaque(false);

        ImageIcon logo = new ImageIcon(new ImageIcon(UIConfig.LOGO_PATH).getImage()
                .getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        headerPanel.add(new JLabel(logo));

        JLabel lblHeaderText = new JLabel("E-Reporting System");
        lblHeaderText.setFont(UIConfig.H2);
        headerPanel.add(lblHeaderText);

        bgPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        UICard regCard = new UICard(30, Color.WHITE);
        regCard.setPreferredSize(new Dimension(750, 880));

        cardLayout = new CardLayout();
        formContainer = new JPanel(cardLayout);
        formContainer.setOpaque(false);

        formContainer.add(createPersonalInfoPanel(), "personal");
        formContainer.add(createCredentialPanel(), "credentials");

        regCard.setLayout(new BorderLayout());
        regCard.add(formContainer, BorderLayout.CENTER);

        centerWrapper.add(regCard);
        bgPanel.add(centerWrapper, BorderLayout.CENTER);
        add(bgPanel, BorderLayout.CENTER);
    }

    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel("Personal Information", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H2);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 0, 15, 0);
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        String[] puroks = AppConfig.REPORT_PUROK_OPTIONS;
        cbPurok = new UIComboBox<>(puroks);
        UIComboBox.applyPreset(cbPurok, UIConfig.COMBOBOX_WIDTH_STANDARD);
        cbPurok.setFont(new Font("Arial", Font.PLAIN, 16));
        cbPurok.applySizePreset(UIComboBox.SizePreset.LARGE);

        rbgSex = new UIRadioButtonGroup(new String[] { "Male", "Female" });

        txtFName = new UIInput(15);
        txtMName = new UIInput(15);
        txtLName = new UIInput(15);
        txtEmail = new UIInput(15);
        txtEmail.setPlaceholder("example@email.com");
        txtEmail.setFieldType(UIValidator.FieldType.EMAIL);

        txtContact = new UIInput(15);
        txtContact.setPlaceholder("09123456789");
        txtContact.setLimit(11, true);
        txtContact.setFieldType(UIValidator.FieldType.PHONE);

        txtHouseNum = new UIInput(15);
        txtHouseNum.setPlaceholder("123");
        txtHouseNum.setLimit(5, true);

        txtUsername = new UIInput(10);
        txtUsername.setPlaceholder("Provide a username");
        txtPassword = new UIPasswordInput(10);
        txtPassword.setPlaceholder("Provide a password");
        txtConfirmPassword = new UIPasswordInput(10);
        txtConfirmPassword.setPlaceholder("Re-enter your password");
        txtConfirmPassword.setMatchTarget(txtPassword);

        addInputGroup(panel, "First Name", txtFName, gbc, 0, 1);
        addInputGroup(panel, "Middle Name", txtMName, gbc, 1, 1);
        addInputGroup(panel, "Last Name", txtLName, gbc, 0, 3);

        rbgSex.setPreferredSize(new Dimension(200, 40));
        addInputGroup(panel, "Sex", rbgSex, gbc, 1, 3);

        addInputGroup(panel, "Phone Number", txtContact, gbc, 0, 5);
        addInputGroup(panel, "Email Address", txtEmail, gbc, 1, 5);

        addInputGroup(panel, "House Number", txtHouseNum, gbc, 0, 7);

        gbc.gridwidth = 2;
        addInputGroup(panel, "Purok", cbPurok, gbc, 1, 7);

        UIButton btnNext = new UIButton("Continue to Credentials", UIConfig.SUCCESS,
                new Dimension(540, 50), UIConfig.BTN_SECONDARY_FONT, 25, UIButton.ButtonType.PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 40, 5, 40);

        btnNext.addActionListener(e -> {
            boolean hasError = UIValidator.validateInputs(List.of(
                    txtFName, txtMName, txtLName, txtContact, txtEmail, txtHouseNum));

            if (UIValidator.validateComboBox(cbPurok))
                hasError = true;

            if (rbgSex.getSelectedValue() == null) {
                JOptionPane.showMessageDialog(this, "Please select Sex");
                hasError = true;
            }

            if (hasError)
                return;

            fName = txtFName.getValue();
            mName = txtMName.getValue();
            lName = txtLName.getValue();
            contact = txtContact.getValue();
            email = txtEmail.getValue();
            houseNum = txtHouseNum.getValue();
            sex = rbgSex.getSelectedValue();
            purok = String.valueOf(cbPurok.getSelectedItem());

            cardLayout.show(formContainer, "credentials");
        });

        panel.add(btnNext, gbc);

        JPanel footer = FormLayoutUtils.createFooterLink(
                "Already have an account? ",
                "Login here",
                UIConfig.PRIMARY,
                () -> app.navigate("login"));
        gbc.gridy = 12;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(5, 0, 25, 0);
        panel.add(footer, gbc);

        return panel;
    }

    private JPanel createCredentialPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Account Credentials", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 0, 40, 0);
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 0, 40, 0);

        addInputGroup(panel, "Username", txtUsername, gbc, 0, 1);
        addInputGroup(panel, "Password", txtPassword, gbc, 0, 3);
        addInputGroup(panel, "Confirm Password", txtConfirmPassword, gbc, 0, 5);

        /* ===== LIVE STATUS LABEL ===== */
        lblCredentialStatus = new JLabel(" ");
        lblCredentialStatus.setFont(UIConfig.CAPTION);
        lblCredentialStatus.setForeground(UIConfig.TEXT_SECONDARY);
        lblCredentialStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(lblCredentialStatus, gbc);

        UIButton btnFinish = new UIButton("Complete Registration", UIConfig.SUCCESS,
                new Dimension(540, 50), UIConfig.BTN_SECONDARY_FONT, 25, UIButton.ButtonType.PRIMARY);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(40, 40, 10, 40);
        panel.add(btnFinish, gbc);

        JButton btnBack = new JButton("← Back to Personal Info");
        btnBack.setFont(UIConfig.SMALL);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setForeground(UIConfig.TEXT_MUTED);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(formContainer, "personal"));

        gbc.gridy = 8;
        gbc.insets = new Insets(5, 0, 20, 0);
        panel.add(btnBack, gbc);

        /* ===== LIVE VALIDATION LISTENERS ===== */
        DocumentListener passwordListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validatePasswordLive();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validatePasswordLive();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validatePasswordLive();
            }
        };

        txtPassword.getDocument().addDocumentListener(passwordListener);
        txtConfirmPassword.getDocument().addDocumentListener(passwordListener);

        btnFinish.addActionListener(e -> {
            boolean hasError = UIValidator.validateInputs(List.of(txtUsername));
            if (hasError)
                return;

            String newPass = txtPassword.getValue();
            String confirmPass = txtConfirmPassword.getValue();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                lblCredentialStatus.setText("Please fill in both password fields.");
                lblCredentialStatus.setForeground(Color.RED);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                lblCredentialStatus.setText("Passwords do not match.");
                lblCredentialStatus.setForeground(Color.RED);
                txtConfirmPassword.setText("");
                txtConfirmPassword.requestFocus();
                return;
            }

            if (newPass.length() < 6) {
                lblCredentialStatus.setText("Password must be at least 6 characters.");
                lblCredentialStatus.setForeground(Color.RED);
                return;
            }

            lblCredentialStatus.setText(" ");
            password = newPass;
            username = txtUsername.getValue();

            UserInfo ui = new UserInfo(fName, mName, lName, sex, contact, email, houseNum, purok);
            Credential cred = new Credential(username, password);
            String result = new UserServiceController().registerUser(ui, cred);

            if ("SUCCESS".equals(result)) {
                JOptionPane.showMessageDialog(this, "Registration Successful!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                app.navigate("login");
            } else {
                JOptionPane.showMessageDialog(this, result, "Registration Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    /**
     * Live password validation — updates status label as user types.
     * Shows strength when typing password, shows match status when typing confirm.
     */
    private void validatePasswordLive() {
        String pass = txtPassword.getValue();
        String confirm = txtConfirmPassword.getValue();

        // If both empty, clear status
        if (pass.isEmpty() && confirm.isEmpty()) {
            lblCredentialStatus.setText(" ");
            return;
        }

        // If confirm field has text, prioritize match check
        if (!confirm.isEmpty()) {
            if (!pass.equals(confirm)) {
                lblCredentialStatus.setText("Passwords do not match.");
                lblCredentialStatus.setForeground(Color.RED);
                return;
            } else {
                lblCredentialStatus.setText("Passwords match.");
                lblCredentialStatus.setForeground(new Color(52, 168, 83)); // green
                return;
            }
        }

        // Otherwise show strength of password field
        if (pass.length() < 6) {
            lblCredentialStatus.setText("Password is too weak (min 6 characters).");
            lblCredentialStatus.setForeground(Color.ORANGE);
        } else if (pass.length() < 10) {
            lblCredentialStatus.setText("Password strength: Fair");
            lblCredentialStatus.setForeground(Color.ORANGE);
        } else {
            lblCredentialStatus.setText("Password strength: Strong");
            lblCredentialStatus.setForeground(new Color(52, 168, 83)); // green
        }
    }

    private void addInputGroup(JPanel panel, String title, JComponent input,
            GridBagConstraints gbc, int x, int y) {

        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 30, 2, 10);

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConfig.INPUT_TITLE);
        panel.add(lbl, gbc);

        gbc.gridy = y + 1;
        gbc.insets = new Insets(5, 30, 5, 30);

        panel.add(input, gbc);
    }
}