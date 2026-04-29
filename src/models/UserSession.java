package models;

public class UserSession {
    private int userId;
    private String role;
    private String username;
    private boolean isVerified;

    public UserSession(int userId, String role, boolean isVerified) {
        this.userId = userId;
        this.role = role;
        this.isVerified = isVerified;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}