package features.core.usermanagement;

public class UserData {
    private int id;
    private String name;
    private String role;
    private String purok;
    private String phone;
    private boolean banned;
    private String houseNumber;
    private String street;
    private String email;

    // ── Constructors ─────────────────────────────────────────────

    public UserData(String name, String role, String purok, String phone, boolean banned) {
        this.name = name;
        this.role = role;
        this.purok = purok;
        this.phone = phone;
        this.banned = banned;
    }

    public UserData(int id, String name, String role, String purok, String phone,
            boolean banned, String houseNumber, String street) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.purok = purok;
        this.phone = phone;
        this.banned = banned;
        this.houseNumber = houseNumber;
        this.street = street;
    }

    public UserData(int id, String name, String role, String purok, String phone,
            boolean banned, String houseNumber, String street, String email) {
        this(id, name, role, purok, phone, banned, houseNumber, street);
        this.email = email;
    }

    // ── Helpers ──────────────────────────────────────────────────

    public String getStatus() {
        return banned ? "Banned" : "Active";
    }

    public UserData copy() {
        UserData copy = new UserData(id, name, role, purok, phone, banned, houseNumber, street, email);
        return copy;
    }

    @Override
    public String toString() {
        return "UserData{id=" + id + ", name='" + name + '\'' + ", role='" + role + '\'' + '}';
    }

    // ── Getters / Setters ────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPurok() {
        return purok;
    }

    public void setPurok(String purok) {
        this.purok = purok;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}