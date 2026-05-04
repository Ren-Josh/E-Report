package services.controller;

import javax.swing.*;

import services.validation.ValidationUtil;

/**
 * Static utility controller for validating user input fields before submission.
 * Provides a single entry point that checks email, contact number, and age
 * in sequence, showing modal error dialogs for the first failure encountered.
 */
public class UserInputController {

    /**
     * Validates email, contact number, and age using ValidationUtil rules.
     * Displays a JOptionPane error dialog for the first invalid field and
     * returns false immediately. If all fields pass, prints a confirmation
     * message to the console and returns true.
     * 
     * @param email   the email address string to validate
     * @param contact the contact number string to validate
     * @param age     the age integer to validate
     * @return true if all inputs are valid; false if any check fails
     */
    public static boolean validateAndSubmit(String email, String contact, int age) {
        // Validate email format first; abort if it fails.
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(null, "Invalid email format!");
            return false;
        }

        // Validate contact number format; abort if it fails.
        if (!ValidationUtil.isValidContact(contact)) {
            JOptionPane.showMessageDialog(null, "Invalid contact number!");
            return false;
        }

        // Validate age against allowed range; abort if it fails.
        if (!ValidationUtil.isValidAge(age)) {
            JOptionPane.showMessageDialog(null, "Invalid age!");
            return false;
        }

        // All checks passed — signal readiness for downstream submission logic.
        System.out.println("All inputs valid. Ready to submit!");
        return true;
    }
}