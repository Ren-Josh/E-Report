package features.ui;

import javax.swing.*;
import java.awt.*;
import config.UIConfig;
import features.components.*;
import app.E_Report;

public class RegisterUI extends JPanel {
    private E_Report app;
    private CardLayout cardLayout;
    private JPanel formContainer;

    public RegisterUI(E_Report app) {
        this.app = app;
        setLayout(new BorderLayout());

        // 1. Background Setup
        HomepageUI temp = new HomepageUI(app);
        JPanel bgPanel = temp.new BackgroundPanel(UIConfig.BACKGROUND_PATH);
        bgPanel.setLayout(new BorderLayout());

        // 2. Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        headerPanel.setOpaque(false);
        ImageIcon logo = new ImageIcon(new ImageIcon(UIConfig.LOGO_PATH).getImage()
                .getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        headerPanel.add(new JLabel(logo));
        JLabel lblHeaderText = new JLabel("E-Reporting System");
        lblHeaderText.setFont(UIConfig.H2);
        headerPanel.add(lblHeaderText);
        bgPanel.add(headerPanel, BorderLayout.NORTH);

        // 3. Center Wrapper
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // 4. Main Card - Adjusted height and added padding
        UICard regCard = new UICard(30, Color.WHITE);
        // Using a slightly larger dimension to accommodate scaling
        regCard.setPreferredSize(new Dimension(750, 880)); 
        
        cardLayout = new CardLayout();
        formContainer = new JPanel(cardLayout);
        formContainer.setOpaque(false);

        formContainer.add(createPersonalInfoPanel(), "PERSONAL");
        formContainer.add(createCredentialPanel(), "CREDENTIALS");

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

        // Title
        JLabel lblTitle = new JLabel("Personal Information", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H2);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 0, 15, 0); 
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        // Row 1: First Name | Middle Name
        addInputGroup(panel, "First Name", new UIInput(15), gbc, 0, 1);
        addInputGroup(panel, "Middle Name", new UIInput(15), gbc, 1, 1);

        // Row 2: Last Name | Sex
        addInputGroup(panel, "Last Name", new UIInput(15), gbc, 0, 3);
        UIRadioButtonGroup rbgSex = new UIRadioButtonGroup(new String[]{"Male", "Female"});
        // Set a preferred size to ensure the radio group doesn't collapse
        rbgSex.setPreferredSize(new Dimension(200, 40)); 
        addInputGroup(panel, "Sex", rbgSex, gbc, 1, 3);

        // Row 3: Phone | Email Address
        addInputGroup(panel, "Phone Number", new UIInput(15), gbc, 0, 5);
        addInputGroup(panel, "Email Address", new UIInput(15), gbc, 1, 5);
        
        // Row 4: House Number | Street
        String[] streets = {"Select Street", "Main St.", "Rizal St.", "Mabini St."};
        UIComboBox<String> cbStreet = new UIComboBox<>(streets);
        addInputGroup(panel, "House Number", new UIInput(15), gbc, 0, 7);
        addInputGroup(panel, "Street", cbStreet, gbc, 1, 7);

        // Row 5: Purok (Full Width)
        gbc.gridwidth = 2;
        String[] puroks = {"Select Purok", "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5"};
        UIComboBox<String> cbPurok = new UIComboBox<>(puroks);
        addInputGroup(panel, "Purok", cbPurok, gbc, 0, 9);

        // Continue Button
        UIButton btnNext = new UIButton("Continue to Credentials", UIConfig.SUCCESS, new Dimension(540, 50), 
                                        UIConfig.BTN_SECONDARY_FONT, 25, UIButton.ButtonType.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 40, 5, 40); 
        btnNext.addActionListener(e -> cardLayout.show(formContainer, "CREDENTIALS"));
        panel.add(btnNext, gbc);

        addFooter(panel, gbc, 12);

        return panel;
    }

    // UNTOUCHED METHOD: As requested
    private JPanel createCredentialPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Account Credentials", SwingConstants.CENTER);
        lblTitle.setFont(UIConfig.H2);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 0, 40, 0);
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 2;
        addInputGroup(panel, "Username", new UIInput(30), gbc, 0, 1);
        addInputGroup(panel, "Password", new UIPasswordInput(30), gbc, 0, 3);
        addInputGroup(panel, "Confirm Password", new UIPasswordInput(30), gbc, 0, 5);

        UIButton btnFinish = new UIButton("Complete Registration", UIConfig.SUCCESS, new Dimension(540, 50), 
                                          UIConfig.BTN_SECONDARY_FONT, 25, UIButton.ButtonType.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.insets = new Insets(40, 40, 10, 40);
        panel.add(btnFinish, gbc);

        // Transparent "Go Back" button
        JButton btnBack = new JButton("← Back to Personal Info");
        btnBack.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setForeground(Color.GRAY);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> cardLayout.show(formContainer, "PERSONAL"));
        
        gbc.gridy = 8;
        gbc.insets = new Insets(5, 0, 20, 0);
        panel.add(btnBack, gbc);

        return panel;
    }

    private void addFooter(JPanel panel, GridBagConstraints gbc, int row) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        footer.setOpaque(false);
        JLabel lblHaveAccount = new JLabel("Already have an account?");
        lblHaveAccount.setFont(UIConfig.CAPTION);
        JLabel lblLoginLink = new JLabel("Login here");
        lblLoginLink.setForeground(UIConfig.PRIMARY);
        lblLoginLink.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblLoginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblLoginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                app.navigate("login");
            }
        });

        footer.add(lblHaveAccount); footer.add(lblLoginLink);

        gbc.gridy = row; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.insets = new Insets(5, 0, 25, 0);
        panel.add(footer, gbc);
    }

    private void addInputGroup(JPanel panel, String title, JComponent input, GridBagConstraints gbc, int x, int y) {
        int originalGridWidth = gbc.gridwidth;

        gbc.gridx = x; gbc.gridy = y; gbc.weighty = 0;
        // Reduced vertical insets to prevent height overflow/cropping
        gbc.insets = new Insets(0, 40, 2, 40); 
        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConfig.INPUT_TITLE);
        panel.add(lbl, gbc);
        
        gbc.gridy = y + 1;
        gbc.insets = new Insets(5, 40, 5, 40);
        
        // Fix for "borderless" or "lifeless" looking components
        if (input instanceof UIComboBox || input instanceof UIRadioButtonGroup) {
            input.setOpaque(false);
        }
        
        panel.add(input, gbc);
        gbc.gridwidth = originalGridWidth;
    }
}