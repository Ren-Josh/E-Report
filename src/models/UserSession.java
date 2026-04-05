package models;

public class UserSession {
    private int userId;
    private String role;
    private boolean isVerified;

    public UserSession(int userId, String role, boolean isVerified) {
        this.userId = userId;
        this.role = role;
        this.isVerified = isVerified;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public boolean isVerified() {
        return isVerified;
    }
}