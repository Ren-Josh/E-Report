package services.validation;

import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern CONTACT_PATTERN = Pattern.compile("^(09|08)[0-9]{9}$");

    public static boolean requireFields(Map<JTextField, String> fields, StringBuilder error) {

        boolean hasError = false;
        JTextField field;
        String value;
        String label;

        for (Map.Entry<JTextField, String> entry : fields.entrySet()) {

            field = entry.getKey();
            value = field.getText().trim();
            label = entry.getValue();

            if (value.isEmpty()) {

                if (!hasError) {
                    field.requestFocus();
                }

                error.append("- ").append(label).append("\n");
                hasError = true;
            }
        }

        return hasError;
    }

    public static boolean requirePasswordFields(Map<JPasswordField, String> fields, StringBuilder error) {

        boolean hasError = false;
        JPasswordField field;
        String value;
        String label;

        for (Map.Entry<JPasswordField, String> entry : fields.entrySet()) {

            field = entry.getKey();
            value = new String(field.getPassword()).trim();
            label = entry.getValue();

            if (value.isEmpty()) {

                if (!hasError) {
                    field.requestFocus();
                }

                error.append("- ").append(label).append("\n");
                hasError = true;
            }
        }

        return hasError;
    }

    public static boolean validateEmailField(String email, StringBuilder error) {

        if (email == null || email.trim().isEmpty()) {
            error.append("- Email is required\n");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            error.append("- Invalid email format\n");
            return false;
        }

        return true;
    }

    public static boolean validateContactField(String contact, StringBuilder error) {

        if (contact == null || contact.trim().isEmpty()) {
            error.append("- Contact is required\n");
            return false;
        }

        if (!CONTACT_PATTERN.matcher(contact).matches()) {
            error.append("- Invalid contact number (must start with 09 or 08)\n");
            return false;
        }

        return true;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidContact(String phoneNumber) {
        return phoneNumber != null && CONTACT_PATTERN.matcher(phoneNumber).matches();
    }

    public static boolean isValidAge(int age) {
        return age >= 0 && age <= 200;
    }

    public static boolean validatePassword(String password, String confirm, StringBuilder error) {
        if (password == null || password.isEmpty()) {
            error.append("- Password is required\n");
            return false;
        }
        if (password.length() < 8) {
            error.append("- Password must be at least 8 characters\n");
            return false;
        }
        if (confirm != null && !password.equals(confirm)) {
            error.append("- Passwords do not match\n");
            return false;
        }
        return true;
    }

    public static boolean validatePasswordStrength(String password, StringBuilder error) {
        int strength = UIValidator.calculatePasswordStrength(password);
        if (strength < 30) {
            error.append("- Password is too weak\n");
            return false;
        }
        return true;
    }
}