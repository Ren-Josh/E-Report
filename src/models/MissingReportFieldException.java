package models;

public class MissingReportFieldException extends IllegalArgumentException {

    private final String fieldName;

    public MissingReportFieldException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}