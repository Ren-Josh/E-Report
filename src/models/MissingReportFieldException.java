package models;

/**
 * Thrown when a required report field is missing or empty.
 */
public class MissingReportFieldException extends RuntimeException {
    private final String fieldName;

    public MissingReportFieldException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}