package tests.units;

import services.middleware.ValidationUtil;

/**
 * Pure unit tests for ValidationUtil.
 *
 * These tests require NO database connection — they exercise the validation
 * logic in complete isolation, making them the fastest tests in the suite.
 *
 * Covers:
 *   - isValidEmail()   : valid formats, invalid formats, null, empty string
 *   - isValidContact() : Philippine mobile numbers (09/08 prefix), invalid, null
 *   - isValidAge()     : boundary values (0, 200), valid range, negatives, over-limit
 */
public class ValidationUtilTest {

    private static boolean allTestsPassed = true;

    public static void main(String[] args) {
        System.out.println("===== STARTING VALIDATION UTIL TESTS (no DB) =====\n");

        // isValidEmail
        testValidEmail();
        testInvalidEmailNoAtSign();
        testInvalidEmailNoTLD();
        testNullEmail();
        testEmptyEmail();

        // isValidContact
        testValidContactWith09Prefix();
        testValidContactWith08Prefix();
        testInvalidContactTooShort();
        testInvalidContactWrongPrefix();
        testNullContact();

        // isValidAge
        testValidAgeLowerBound();
        testValidAgeUpperBound();
        testValidAgeMiddle();
        testInvalidAgeNegative();
        testInvalidAgeOverLimit();

        System.out.println("===================================================");
        if (allTestsPassed) {
            System.out.println("ALL VALIDATION UTIL TESTS PASSED!");
        } else {
            System.out.println("SOME VALIDATION UTIL TESTS FAILED. See the logs above.");
        }
    }

    // ==========================================
    // isValidEmail — valid cases
    // ==========================================

    public static void testValidEmail() {
        String input = "user@example.com";
        boolean result = ValidationUtil.isValidEmail(input);
        assertPass("[EMAIL] Valid email \"" + input + "\"", result);
    }

    // ==========================================
    // isValidEmail — invalid cases
    // ==========================================

    public static void testInvalidEmailNoAtSign() {
        String input = "userexample.com";
        boolean result = ValidationUtil.isValidEmail(input);
        assertFail("[EMAIL] No @ sign: \"" + input + "\" should be invalid", result);
    }

    public static void testInvalidEmailNoTLD() {
        // The regex used is ^[A-Za-z0-9+_.-]+@(.+)$ — so anything after @ passes.
        // This test documents the actual behavior rather than an ideal one.
        String input = "user@";
        boolean result = ValidationUtil.isValidEmail(input);
        // The regex requires at least one char after @, so "user@" fails.
        assertFail("[EMAIL] No domain after @: \"" + input + "\" should be invalid", result);
    }

    public static void testNullEmail() {
        boolean result = ValidationUtil.isValidEmail(null);
        assertFail("[EMAIL] null should be invalid", result);
    }

    public static void testEmptyEmail() {
        boolean result = ValidationUtil.isValidEmail("");
        assertFail("[EMAIL] Empty string should be invalid", result);
    }

    // ==========================================
    // isValidContact — valid cases
    // ==========================================

    public static void testValidContactWith09Prefix() {
        String input = "09171234567";
        boolean result = ValidationUtil.isValidContact(input);
        assertPass("[CONTACT] Valid 09-prefix: \"" + input + "\"", result);
    }

    public static void testValidContactWith08Prefix() {
        String input = "08171234567";
        boolean result = ValidationUtil.isValidContact(input);
        assertPass("[CONTACT] Valid 08-prefix: \"" + input + "\"", result);
    }

    // ==========================================
    // isValidContact — invalid cases
    // ==========================================

    public static void testInvalidContactTooShort() {
        String input = "0917123"; // only 7 digits — too short
        boolean result = ValidationUtil.isValidContact(input);
        assertFail("[CONTACT] Too short: \"" + input + "\" should be invalid", result);
    }

    public static void testInvalidContactWrongPrefix() {
        String input = "07171234567"; // starts with 07, not 09/08
        boolean result = ValidationUtil.isValidContact(input);
        assertFail("[CONTACT] Wrong prefix (07): \"" + input + "\" should be invalid", result);
    }

    public static void testNullContact() {
        boolean result = ValidationUtil.isValidContact(null);
        assertFail("[CONTACT] null should be invalid", result);
    }

    // ==========================================
    // isValidAge — valid cases
    // ==========================================

    public static void testValidAgeLowerBound() {
        assertPass("[AGE] Lower bound (0)", ValidationUtil.isValidAge(0));
    }

    public static void testValidAgeUpperBound() {
        assertPass("[AGE] Upper bound (200)", ValidationUtil.isValidAge(200));
    }

    public static void testValidAgeMiddle() {
        assertPass("[AGE] Typical age (25)", ValidationUtil.isValidAge(25));
    }

    // ==========================================
    // isValidAge — invalid cases
    // ==========================================

    public static void testInvalidAgeNegative() {
        assertFail("[AGE] Negative (-1) should be invalid", ValidationUtil.isValidAge(-1));
    }

    public static void testInvalidAgeOverLimit() {
        assertFail("[AGE] Over limit (201) should be invalid", ValidationUtil.isValidAge(201));
    }

    // ==========================================
    // ASSERTION HELPERS
    // ==========================================

    /**
     * Asserts that condition is true (i.e. the tested method returned true / was valid).
     */
    private static void assertPass(String label, boolean condition) {
        if (condition) {
            System.out.println("-> PASS: " + label);
        } else {
            System.out.println("-> FAIL: " + label + " — expected true but got false");
            allTestsPassed = false;
        }
    }

    /**
     * Asserts that condition is false (i.e. the tested method correctly rejected input).
     */
    private static void assertFail(String label, boolean condition) {
        if (!condition) {
            System.out.println("-> PASS: " + label);
        } else {
            System.out.println("-> FAIL: " + label + " — expected false but got true");
            allTestsPassed = false;
        }
    }
}
