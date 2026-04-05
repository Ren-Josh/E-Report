package services.middleware;

import java.util.regex.Pattern;

public class checkValidation {

    /**
     * This method is used to validate email address
     * isValidEmail();
     * 
     * @params String email
     * @return true if valid, false if not
     */

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    /**
     * This method is used to validate email address
     * isValidContact();
     * 
     * @params String phoneNumber
     * @return true if valid, false if not
     */

    public static boolean isValidContact(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }

        // ^(09|08) : Must start with either 09 or 08
        // [0-9]{9} : Must be followed by exactly 9 digits
        // $ : Must end the string here (no extra characters allowed)
        String phoneRegex = "^(09|08)[0-9]{9}$";

        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phoneNumber).matches();
    }

    public static boolean isValidAge(int age) {
        return age >= 0 && age <= 200;
    }
}
