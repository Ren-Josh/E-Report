package models;

public class MapPinOutOfServiceAreaException extends RuntimeException {

    public MapPinOutOfServiceAreaException(String message) {
        super(message);
    }

    public MapPinOutOfServiceAreaException(String message, Throwable cause) {
        super(message, cause);
    }
}