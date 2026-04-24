package services.validation;

import features.components.UIComboBox;
import features.components.UIInput;
import features.components.UIPasswordInput;
import java.util.List;
import java.util.regex.Pattern;

public class UIValidator {

    public enum FieldType {
        TEXT,
        EMAIL,
        PHONE,
        PASSWORD
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern CONTACT_PATTERN = Pattern.compile("^(09|08)[0-9]{9}$");

    public static boolean validateInputs(List<UIInput> inputs) {
        boolean hasError = false;
        for (UIInput input : inputs) {
            String value = input.getValue();
            if (value.isEmpty() || !isValidField(input.getFieldType(), value)) {
                if (!hasError)
                    input.requestFocus();
                input.setError();
                hasError = true;
            } else {
                input.setValid();
            }
        }
        return hasError;
    }

    public static boolean isValidField(FieldType type, String value) {
        if (value == null || value.trim().isEmpty())
            return false;
        return switch (type) {
            case EMAIL -> EMAIL_PATTERN.matcher(value).matches();
            case PHONE -> CONTACT_PATTERN.matcher(value).matches();
            case PASSWORD -> value.length() >= 8;
            case TEXT -> value.length() >= 1;
        };
    }

    public static boolean validatePasswords(List<UIPasswordInput> inputs) {
        boolean hasError = false;
        for (UIPasswordInput input : inputs) {
            if (input.getValue().isEmpty()) {
                if (!hasError)
                    input.requestFocus();
                input.setError();
                hasError = true;
            } else {
                input.setValid();
            }
        }
        return hasError;
    }

    public static boolean validateComboBox(UIComboBox<?> comboBox) {
        if (comboBox.isInvalidSelection()) {
            comboBox.setError();
            return true;
        }
        comboBox.setValid();
        return false;
    }

    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty())
            return 0;
        int score = 0;
        if (password.length() >= 8)
            score += 25;
        if (password.matches(".*[a-z].*"))
            score += 20;
        if (password.matches(".*[A-Z].*"))
            score += 20;
        if (password.matches(".*\\d.*"))
            score += 15;
        if (password.matches(".*[!@#$%^&*()].*"))
            score += 20;
        return Math.min(score, 100);
    }

    public static boolean isStrongPassword(String password) {
        return calculatePasswordStrength(password) >= 60;
    }
}