package models;

public class Credential {
    private String username, password, role, dateCreated;
    private boolean isVerified;
    private int UI_ID;

    public Credential(int UI_ID, String username, String password, String role, boolean isVerified,
            String dateCreated) {
        this.UI_ID = UI_ID;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isVerified = isVerified;
        this.dateCreated = dateCreated;
    }

    public Credential(String username, String password, String role) {        
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Credential(String username, String password) {        
        this.username = username;
        this.password = password;        
    }

    public Credential() {
    };

    public int getUI_ID() {
        return UI_ID;
    }

    public void setUI_ID(int UI_ID) {
        this.UI_ID = UI_ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
