package features.core.usermanagement;

import config.UIConfig;
import features.components.UIButton;
import features.components.UIComboBox;
import features.components.UIInput;
import services.validation.UIValidator;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UserEditFormPanel extends JPanel {

    public interface Listener {
        void onSaved(UserData user);

        void onCancelled();
    }

    private final Listener listener;
    private UserData editingUser; // <-- reference to the user currently being edited

    private UIInput nameField;
    private UIInput phoneField;
    private UIInput houseNumberField;
    private UIInput emailField;
    private UIComboBox<String> streetCombo;
    private UIComboBox<String> purokCombo;
    private UIComboBox<String> roleCombo;
    private JLabel errorLabel;

    public UserEditFormPanel(Listener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title with edit icon
        JLabel titleLabel = new JLabel("Edit User Information", loadIcon(UIConfig.EDIT_ICON_PATH, 22),
                SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setIconTextGap(10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 10, 20, 10);
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 10, 2, 10);

        nameField = createReadOnlyInput();
        addFormRow(gbc, 1, "Name:", nameField);

        phoneField = createInput();
        phoneField.setFieldType(UIValidator.FieldType.PHONE);
        addFormRow(gbc, 2, "Phone Number:", phoneField);

        emailField = createInput();
        emailField.setFieldType(UIValidator.FieldType.EMAIL);
        addFormRow(gbc, 3, "Email:", emailField);

        houseNumberField = createInput();
        addFormRow(gbc, 4, "House Number:", houseNumberField);

        String[] streets = { "Main Street", "2nd Street", "3rd Street", "Oak Street", "Maple Avenue" };
        streetCombo = new UIComboBox<>(streets);
        streetCombo.setPreferredSize(new Dimension(250, 32));
        addFormRow(gbc, 5, "Street:", streetCombo);

        String[] puroks = { "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5" };
        purokCombo = new UIComboBox<>(puroks);
        purokCombo.setPreferredSize(new Dimension(250, 32));
        addFormRow(gbc, 6, "Purok:", purokCombo);

        String[] roles = { "Resident", "Secretary", "Barangay Captain", "Admin" };
        roleCombo = new UIComboBox<>(roles);
        roleCombo.setPreferredSize(new Dimension(250, 32));
        addFormRow(gbc, 7, "Role:", roleCombo);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 60, 60));
        add(errorLabel, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        // Save button with save icon
        UIButton saveButton = new UIButton(
                "Save Changes",
                new Color(25, 118, 210),
                new Dimension(160, 38),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.PRIMARY);
        saveButton.setHorizontalTextPosition(SwingConstants.LEFT);
        saveButton.setIconTextGap(8);
        saveButton.addActionListener(e -> save());

        UIButton cancelButton = new UIButton(
                "Cancel",
                Color.WHITE,
                new Dimension(100, 38),
                UIConfig.BODY,
                8,
                UIButton.ButtonType.OUTLINED);
        cancelButton.addActionListener(e -> {
            if (listener != null)
                listener.onCancelled();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 9;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(buttonPanel, gbc);
    }

    private void addFormRow(GridBagConstraints gbc, int row, String labelText, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(UIConfig.BODY);
        label.setForeground(new Color(80, 80, 80));
        add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        add(component, gbc);
    }

    private UIInput createInput() {
        UIInput field = new UIInput(20);
        field.setPreferredSize(new Dimension(250, 32));
        field.setFont(UIConfig.BODY);
        field.setIdleBorderColor(new Color(200, 200, 200));
        return field;
    }

    private UIInput createReadOnlyInput() {
        UIInput field = createInput();
        field.setEditable(false);
        field.setReadonlyBackground(new Color(245, 245, 245));
        field.setForeground(new Color(100, 100, 100));
        return field;
    }

    public void loadUser(UserData user) {
        this.editingUser = user; // keep reference so we can mutate it on save
        nameField.setText(user.getName());
        phoneField.setText(user.getPhone());
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        houseNumberField.setText(user.getHouseNumber() != null ? user.getHouseNumber() : "");
        streetCombo.setSelectedItem(user.getStreet() != null ? user.getStreet() : "Main Street");
        purokCombo.setSelectedItem(user.getPurok());
        roleCombo.setSelectedItem(user.getRole());
        errorLabel.setText("");
    }

    public void clear() {
        errorLabel.setText("");
        phoneField.setText("");
        emailField.setText("");
        houseNumberField.setText("");
        streetCombo.setSelectedIndex(0);
        purokCombo.setSelectedIndex(0);
        roleCombo.setSelectedIndex(0);
        editingUser = null;
    }

    public void clearError() {
        errorLabel.setText("");
    }

    private void save() {
        if (editingUser == null) {
            errorLabel.setText("No user loaded");
            return;
        }

        String phone = phoneField.getValue();
        String email = emailField.getValue();

        if (phone.isEmpty()) {
            errorLabel.setText("Phone number is required");
            return;
        }
        if (phoneField.getState() == UIInput.ValidationState.INVALID) {
            errorLabel.setText("Invalid phone number format");
            return;
        }
        if (!email.isEmpty() && emailField.getState() == UIInput.ValidationState.INVALID) {
            errorLabel.setText("Invalid email format");
            return;
        }

        // Mutate the existing user object directly (matches original behavior)
        editingUser.setPhone(phone);
        editingUser.setEmail(email);
        editingUser.setHouseNumber(houseNumberField.getValue());
        editingUser.setStreet((String) streetCombo.getSelectedItem());
        editingUser.setPurok((String) purokCombo.getSelectedItem());
        editingUser.setRole((String) roleCombo.getSelectedItem());

        if (listener != null) {
            listener.onSaved(editingUser);
        }
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