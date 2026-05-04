package features.views;

import app.E_Report;
import config.UIConfig;
import features.components.UIButton;
import features.components.UIPasswordInput;
import features.core.FormLayoutUtils;
import services.controller.PasswordResetController;
import services.controller.PasswordResetController.OtpResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Three-step password change panel: verify identity, enter OTP, set new
 * password.
 * Uses CardLayout to guide the user through the OTP-based password reset flow.
 * All network and database operations run on SwingWorker background threads.
 */
public class SecurityPasswordChangePanel extends JPanel {

    /**
     * Reference to the main application frame for session and navigation access.
     */
    private final E_Report app;
    /**
     * Controller that handles OTP generation, email sending, and password reset.
     */
    private final PasswordResetController controller;
    /** CardLayout that switches between the three steps. */
    private CardLayout cardLayout;
    /** Container holding all three step panels managed by cardLayout. */
    private JPanel cardPanel;

    // -- Step 1: Identity Verification --
    /** Read-only field displaying the current user's username. */
    private JTextField txtUsername;
    /** Label showing the masked email address where OTP will be sent. */
    private JLabel lblStep1EmailMask;
    /** Button to trigger OTP sending. */
    private JButton btnSendOtp;
    /** Status label for step 1 feedback messages. */
    private JLabel lblStep1Status;

    // -- Step 2: OTP Verification --
    /** Input field for the 6-digit OTP code. */
    private JTextField txtOtp;
    /** Label showing where the OTP was sent. */
    private JLabel lblStep2EmailMask;
    /** Button to verify the entered OTP. */
    private JButton btnVerifyOtp;
    /** Button to request a new OTP with cooldown timer. */
    private JButton btnResendOtp;
    /** Status label for step 2 feedback messages. */
    private JLabel lblStep2Status;
    /** Timer that counts down the resend cooldown period. */
    private Timer countdownTimer;
    /** Seconds remaining before another OTP can be requested. */
    private int resendCooldown = 60;

    // -- Step 3: New Password --
    /** Input field for the new password. */
    private JPasswordField txtNewPassword;
    /** Input field to confirm the new password. */
    private JPasswordField txtConfirmPassword;
    /** Button to finalize the password change. */
    private JButton btnResetPassword;
    /** Status label for step 3 feedback messages. */
    private JLabel lblStep3Status;

    /**
     * Constructs the security password change panel and initializes the UI.
     * 
     * @param app the main E_Report application frame
     */
    public SecurityPasswordChangePanel(E_Report app) {
        this.app = app;
        this.controller = new PasswordResetController();
        initializeUI();
    }

    /**
     * Sets up the layout, card container, and all three step panels.
     * Defaults to showing step 1.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Center the card panel using the shared layout utility.
        JPanel centerWrapper = FormLayoutUtils.createCenterWrapper();

        // Initialize CardLayout to hold the three password reset steps.
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        // Add each step panel with a named card identifier.
        cardPanel.add(createStep1Panel(), "step1");
        cardPanel.add(createStep2Panel(), "step2");
        cardPanel.add(createStep3Panel(), "step3");

        centerWrapper.add(cardPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // Start at the identity verification step.
        cardLayout.show(cardPanel, "step1");
    }

    /**
     * Builds the first step: identity verification and OTP request.
     * Displays the username and masked email; disables send if no email exists.
     * 
     * @return the fully assembled step 1 panel
     */
    private JPanel createStep1Panel() {
        JPanel panel = createCardPanel("Change Password");

        GridBagConstraints gbc = FormLayoutUtils.createFormConstraints();
        gbc.insets = new Insets(8, 40, 8, 40);

        // Title and instruction labels.
        JLabel lblTitle = new JLabel("Verify Your Identity", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H3);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitle, gbc);

        JLabel lblInstr = new JLabel(
                "<html><center>For your security, we'll send a One-Time Password (OTP) to your registered email.</center></html>",
                SwingConstants.CENTER);
        lblInstr.setFont(UIConfig.CAPTION);
        lblInstr.setForeground(UIConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 20, 40);
        panel.add(lblInstr, gbc);

        // Read-only username field — user cannot edit their identity.
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(340, 45));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 15, 10, 15)));
        txtUsername.setEditable(false);
        txtUsername.setFocusable(false);
        txtUsername.setBackground(new Color(245, 245, 245));
        gbc.gridy = 2;
        FormLayoutUtils.addInputGroup(panel, "Username", txtUsername, gbc, 0, 2);

        // Email mask label updates dynamically based on the user's registered email.
        lblStep1EmailMask = new JLabel("Loading...", SwingConstants.CENTER);
        lblStep1EmailMask.setFont(UIConfig.CAPTION);
        lblStep1EmailMask.setForeground(UIConfig.TEXT_SECONDARY);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 40, 20, 40);
        panel.add(lblStep1EmailMask, gbc);

        // Status label for error and success messages.
        lblStep1Status = new JLabel(" ");
        lblStep1Status.setFont(UIConfig.CAPTION);
        lblStep1Status.setForeground(UIConfig.TEXT_SECONDARY);
        lblStep1Status.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(lblStep1Status, gbc);

        // Primary action button to send the OTP.
        btnSendOtp = new UIButton("Send OTP", UIConfig.PRIMARY,
                new Dimension(340, 50), UIConfig.BTN_SECONDARY_FONT, 25,
                UIButton.ButtonType.PRIMARY);
        gbc.gridy = 6;
        gbc.insets = new Insets(15, 40, 10, 40);
        panel.add(btnSendOtp, gbc);

        btnSendOtp.addActionListener(e -> onSendOtp());

        // Cancel button returns to the profile view and clears the session.
        JButton btnCancel = new JButton("← Back to Profile");
        btnCancel.setFont(UIConfig.SMALL);
        btnCancel.setContentAreaFilled(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setForeground(UIConfig.TEXT_MUTED);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> {
            controller.clearSession(getCurrentUsername());
            app.navigate("profile");
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 20, 0);
        panel.add(btnCancel, gbc);

        return panel;
    }

    /**
     * Builds the second step: OTP code entry and verification.
     * Includes a resend button with a 60-second cooldown timer.
     * 
     * @return the fully assembled step 2 panel
     */
    private JPanel createStep2Panel() {
        JPanel panel = createCardPanel("Verify OTP");

        GridBagConstraints gbc = FormLayoutUtils.createFormConstraints();
        gbc.insets = new Insets(8, 40, 8, 40);

        // Title and email confirmation labels.
        JLabel lblTitle = new JLabel("Enter OTP Code", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H3);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitle, gbc);

        lblStep2EmailMask = new JLabel("OTP sent to: ***", SwingConstants.CENTER);
        lblStep2EmailMask.setFont(UIConfig.CAPTION);
        lblStep2EmailMask.setForeground(UIConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 15, 40);
        panel.add(lblStep2EmailMask, gbc);

        // OTP input field styled for visibility and centered text.
        txtOtp = new JTextField(20);
        txtOtp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtOtp.setPreferredSize(new Dimension(340, 50));
        txtOtp.setHorizontalAlignment(JTextField.CENTER);
        txtOtp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 15, 10, 15)));
        gbc.gridy = 2;
        FormLayoutUtils.addInputGroup(panel, "6-Digit Code", txtOtp, gbc, 0, 2);

        // Status label for validation and error feedback.
        lblStep2Status = new JLabel(" ");
        lblStep2Status.setFont(UIConfig.CAPTION);
        lblStep2Status.setForeground(UIConfig.TEXT_SECONDARY);
        lblStep2Status.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(lblStep2Status, gbc);

        // Primary action button to verify the entered OTP.
        btnVerifyOtp = new UIButton("Verify OTP", UIConfig.SUCCESS,
                new Dimension(340, 50), UIConfig.BTN_SECONDARY_FONT, 25,
                UIButton.ButtonType.PRIMARY);
        gbc.gridy = 5;
        gbc.insets = new Insets(15, 40, 5, 40);
        panel.add(btnVerifyOtp, gbc);

        btnVerifyOtp.addActionListener(e -> onVerifyOtp());

        // Resend button starts disabled; enabled after cooldown expires.
        btnResendOtp = new JButton("Resend OTP (60s)");
        btnResendOtp.setFont(UIConfig.SMALL);
        btnResendOtp.setContentAreaFilled(false);
        btnResendOtp.setBorderPainted(false);
        btnResendOtp.setForeground(UIConfig.TEXT_MUTED);
        btnResendOtp.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnResendOtp.setEnabled(false);
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(btnResendOtp, gbc);

        btnResendOtp.addActionListener(e -> onResendOtp());

        // Back button returns to step 1 and clears the OTP session.
        JButton btnBack = new JButton("← Use different method");
        btnBack.setFont(UIConfig.SMALL);
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setForeground(UIConfig.TEXT_MUTED);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            controller.clearSession(getCurrentUsername());
            txtOtp.setText("");
            lblStep1Status.setText(" ");
            cardLayout.show(cardPanel, "step1");
        });
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 20, 0);
        panel.add(btnBack, gbc);

        return panel;
    }

    /**
     * Builds the third step: new password creation.
     * Validates password length and confirmation match before updating.
     * 
     * @return the fully assembled step 3 panel
     */
    private JPanel createStep3Panel() {
        JPanel panel = createCardPanel("Set New Password");

        GridBagConstraints gbc = FormLayoutUtils.createFormConstraints();
        gbc.insets = new Insets(8, 40, 8, 40);

        // Title and instruction labels.
        JLabel lblTitle = new JLabel("Create New Password", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H3);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblTitle, gbc);

        JLabel lblInstr = new JLabel(
                "<html><center>Your identity is verified. Enter a strong new password.</center></html>",
                SwingConstants.CENTER);
        lblInstr.setFont(UIConfig.CAPTION);
        lblInstr.setForeground(UIConfig.TEXT_SECONDARY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 20, 40);
        panel.add(lblInstr, gbc);

        // New password and confirmation fields.
        txtNewPassword = new UIPasswordInput(20);
        txtNewPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 2;
        FormLayoutUtils.addInputGroup(panel, "New Password", txtNewPassword, gbc, 0, 2);

        txtConfirmPassword = new UIPasswordInput(20);
        txtConfirmPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 4;
        FormLayoutUtils.addInputGroup(panel, "Confirm Password", txtConfirmPassword, gbc, 0, 4);

        // Status label for validation feedback.
        lblStep3Status = new JLabel(" ");
        lblStep3Status.setFont(UIConfig.CAPTION);
        lblStep3Status.setForeground(UIConfig.TEXT_SECONDARY);
        lblStep3Status.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 40, 5, 40);
        panel.add(lblStep3Status, gbc);

        // Primary action button to finalize the password reset.
        btnResetPassword = new UIButton("Update Password", UIConfig.SUCCESS,
                new Dimension(340, 50), UIConfig.BTN_SECONDARY_FONT, 25,
                UIButton.ButtonType.PRIMARY);
        gbc.gridy = 7;
        gbc.insets = new Insets(15, 40, 10, 40);
        panel.add(btnResetPassword, gbc);

        btnResetPassword.addActionListener(e -> onResetPassword());

        return panel;
    }

    /**
     * Factory method that creates a styled white card panel with GridBagLayout.
     * 
     * @param title the card title (used for potential future header labeling)
     * @return a styled JPanel ready for form content
     */
    private JPanel createCardPanel(String title) {
        JPanel card = new features.components.UICard(30, Color.WHITE);
        card.setPreferredSize(new Dimension(440, 520));
        card.setLayout(new GridBagLayout());
        return card;
    }

    /**
     * Resets the panel to its initial state before being shown.
     * Clears any previous session data, reads the current user's username and
     * email,
     * updates the email mask label, and shows step 1.
     */
    public void preparePanel() {
        // Clear any stale OTP session data for this user.
        controller.clearSession(getCurrentUsername());

        // Read current session info to populate the identity fields.
        String username = getCurrentUsername();
        String email = null;
        if (app.getUserInfo() != null) {
            email = app.getUserInfo().getEmail();
        }

        // Populate the read-only username field.
        if (username != null && !username.isBlank()) {
            txtUsername.setText(username);
        } else {
            txtUsername.setText("");
        }

        // Enable or disable OTP sending based on whether an email is registered.
        if (email != null && !email.isBlank()) {
            lblStep1EmailMask.setText("OTP will be sent to: " + maskEmail(email));
            btnSendOtp.setEnabled(true);
            lblStep1Status.setText(" ");
        } else {
            lblStep1EmailMask.setText("No email address on file.");
            lblStep1Status.setText("Cannot change password without a registered email.");
            lblStep1Status.setForeground(Color.RED);
            btnSendOtp.setEnabled(false);
        }

        // Clear all input fields and status labels from previous attempts.
        txtOtp.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
        lblStep2Status.setText(" ");
        lblStep3Status.setText(" ");

        // Reset to the first step.
        cardLayout.show(cardPanel, "step1");
    }

    /**
     * Retrieves the username from the current user session.
     * 
     * @return the session username, or null if no session exists
     */
    private String getCurrentUsername() {
        if (app.getUserSession() != null) {
            return app.getUserSession().getUsername();
        }
        return null;
    }

    /**
     * Handles the Send OTP button click.
     * Validates the username, then runs the OTP generation and email sending
     * on a background SwingWorker thread.
     */
    private void onSendOtp() {
        String username = getCurrentUsername();
        if (username == null || username.isBlank()) {
            lblStep1Status.setText("Unable to identify user. Please log in again.");
            lblStep1Status.setForeground(Color.RED);
            return;
        }

        setStep1Loading(true);

        // Background worker to prevent UI blocking during email sending.
        SwingWorker<OtpResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OtpResult doInBackground() {
                return controller.sendOtp(username);
            }

            @Override
            protected void done() {
                try {
                    OtpResult result = get();
                    if (result.success) {
                        // Transition to step 2 and start the resend cooldown.
                        lblStep2Status.setText(" ");
                        txtOtp.setText("");
                        lblStep2EmailMask.setText(result.message);
                        cardLayout.show(cardPanel, "step2");
                        startResendCooldown();
                    } else {
                        lblStep1Status.setText(result.message);
                        lblStep1Status.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    lblStep1Status.setText("An error occurred. Please try again.");
                    lblStep1Status.setForeground(Color.RED);
                    e.printStackTrace();
                } finally {
                    setStep1Loading(false);
                }
            }
        };

        worker.execute();
    }

    /**
     * Handles the Verify OTP button click.
     * Validates the OTP format (6 digits), then runs verification on a background
     * thread.
     */
    private void onVerifyOtp() {
        String otp = txtOtp.getText().trim();

        // Empty check.
        if (otp.isEmpty()) {
            lblStep2Status.setText("Please enter the OTP code.");
            lblStep2Status.setForeground(Color.RED);
            return;
        }

        // Format check: must be exactly 6 digits.
        if (otp.length() != 6 || !otp.matches("\\d{6}")) {
            lblStep2Status.setText("Please enter a valid 6-digit code.");
            lblStep2Status.setForeground(Color.RED);
            return;
        }

        setStep2Loading(true);

        // Background worker to prevent UI blocking during OTP validation.
        SwingWorker<OtpResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OtpResult doInBackground() {
                return controller.verifyOtp(getCurrentUsername(), otp);
            }

            @Override
            protected void done() {
                try {
                    OtpResult result = get();
                    if (result.success) {
                        // Transition to step 3 to set the new password.
                        lblStep3Status.setText(" ");
                        txtNewPassword.setText("");
                        txtConfirmPassword.setText("");
                        cardLayout.show(cardPanel, "step3");
                    } else {
                        lblStep2Status.setText(result.message);
                        lblStep2Status.setForeground(Color.RED);
                        txtOtp.selectAll();
                        txtOtp.requestFocus();
                    }
                } catch (Exception e) {
                    lblStep2Status.setText("Verification failed. Please try again.");
                    lblStep2Status.setForeground(Color.RED);
                    e.printStackTrace();
                } finally {
                    setStep2Loading(false);
                }
            }
        };

        worker.execute();
    }

    /**
     * Handles the Resend OTP button click.
     * Re-runs the OTP sending logic and restarts the cooldown timer on success.
     */
    private void onResendOtp() {
        String username = getCurrentUsername();
        if (username == null || username.isBlank()) {
            lblStep2Status.setText("Session error. Please go back and try again.");
            lblStep2Status.setForeground(Color.RED);
            return;
        }

        // Disable the button immediately to prevent double-clicks.
        btnResendOtp.setEnabled(false);
        lblStep2Status.setText("Resending...");
        lblStep2Status.setForeground(UIConfig.TEXT_SECONDARY);

        // Background worker to handle the resend request.
        SwingWorker<OtpResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OtpResult doInBackground() {
                return controller.sendOtp(username);
            }

            @Override
            protected void done() {
                try {
                    OtpResult result = get();
                    if (result.success) {
                        lblStep2Status.setText("New OTP sent!");
                        lblStep2Status.setForeground(new Color(52, 168, 83));
                        txtOtp.setText("");
                        txtOtp.requestFocus();
                        startResendCooldown();
                    } else {
                        lblStep2Status.setText(result.message);
                        lblStep2Status.setForeground(Color.RED);
                        btnResendOtp.setEnabled(true);
                    }
                } catch (Exception e) {
                    lblStep2Status.setText("Failed to resend. Please try again.");
                    lblStep2Status.setForeground(Color.RED);
                    btnResendOtp.setEnabled(true);
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    /**
     * Handles the Update Password button click.
     * Validates password fields for emptiness, match, and minimum length,
     * then runs the password update on a background thread.
     */
    private void onResetPassword() {
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        // Empty check.
        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            lblStep3Status.setText("Please fill in both password fields.");
            lblStep3Status.setForeground(Color.RED);
            return;
        }

        // Match check.
        if (!newPass.equals(confirmPass)) {
            lblStep3Status.setText("Passwords do not match.");
            lblStep3Status.setForeground(Color.RED);
            txtConfirmPassword.setText("");
            txtConfirmPassword.requestFocus();
            return;
        }

        // Minimum length check.
        if (newPass.length() < 6) {
            lblStep3Status.setText("Password must be at least 6 characters.");
            lblStep3Status.setForeground(Color.RED);
            return;
        }

        setStep3Loading(true);

        // Background worker to prevent UI blocking during password hashing and update.
        SwingWorker<OtpResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OtpResult doInBackground() {
                return controller.resetPassword(getCurrentUsername(), newPass);
            }

            @Override
            protected void done() {
                try {
                    OtpResult result = get();
                    if (result.success) {
                        // Show success feedback and return to the profile view.
                        JOptionPane.showMessageDialog(SecurityPasswordChangePanel.this,
                                "Password updated successfully! Please use your new password on next login.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        controller.clearSession(getCurrentUsername());
                        app.navigate("profile");
                    } else {
                        lblStep3Status.setText(result.message);
                        lblStep3Status.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    lblStep3Status.setText("Error: " + e.getMessage());
                    lblStep3Status.setForeground(Color.RED);
                    e.printStackTrace();
                } finally {
                    setStep3Loading(false);
                }
            }
        };

        worker.execute();
    }

    /**
     * Toggles the loading state for step 1 controls.
     * 
     * @param loading true to disable and show "Sending..."; false to restore normal
     *                state
     */
    private void setStep1Loading(boolean loading) {
        btnSendOtp.setEnabled(!loading);
        btnSendOtp.setText(loading ? "Sending..." : "Send OTP");
    }

    /**
     * Toggles the loading state for step 2 controls.
     * 
     * @param loading true to disable and show "Verifying..."; false to restore
     *                normal state
     */
    private void setStep2Loading(boolean loading) {
        btnVerifyOtp.setEnabled(!loading);
        btnVerifyOtp.setText(loading ? "Verifying..." : "Verify OTP");
        txtOtp.setEnabled(!loading);
    }

    /**
     * Toggles the loading state for step 3 controls.
     * 
     * @param loading true to disable and show "Updating..."; false to restore
     *                normal state
     */
    private void setStep3Loading(boolean loading) {
        btnResetPassword.setEnabled(!loading);
        btnResetPassword.setText(loading ? "Updating..." : "Update Password");
        txtNewPassword.setEnabled(!loading);
        txtConfirmPassword.setEnabled(!loading);
    }

    /**
     * Starts the 60-second countdown timer for the resend button.
     * Disables the button and updates its text each second until expiry.
     */
    private void startResendCooldown() {
        resendCooldown = 60;
        btnResendOtp.setEnabled(false);
        btnResendOtp.setText("Resend OTP (" + resendCooldown + "s)");

        // Stop any existing timer to prevent duplicate countdowns.
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        // Create a new timer that ticks every second.
        countdownTimer = new Timer(1000, e -> {
            resendCooldown--;
            if (resendCooldown > 0) {
                btnResendOtp.setText("Resend OTP (" + resendCooldown + "s)");
            } else {
                // Cooldown expired: re-enable the button and stop the timer.
                btnResendOtp.setText("Resend OTP");
                btnResendOtp.setEnabled(true);
                countdownTimer.stop();
            }
        });
        countdownTimer.start();
    }

    /**
     * Masks an email address for privacy display.
     * Replaces the middle portion of the local part with asterisks.
     * 
     * @param email the raw email address
     * @return the masked email string (e.g., "a***z@example.com")
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1)
            return email;

        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }

        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }
}