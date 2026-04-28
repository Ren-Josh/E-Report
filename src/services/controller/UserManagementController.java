package services.controller;

import daos.UserManagementDao;
import config.database.DBConnection;
import features.core.usermanagement.UserData;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserManagementController {

    private final UserManagementDao userManagementDao;

    public UserManagementController() {
        this.userManagementDao = new UserManagementDao();
    }

    public List<UserData> getAllUsers() {
        try (Connection con = DBConnection.connect()) {
            return userManagementDao.getAllUsers(con);
        } catch (SQLException e) {
            System.err.println("Error fetching all users");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<UserData> searchUsers(String name, String role, String purok, String status) {
        try (Connection con = DBConnection.connect()) {
            return userManagementDao.searchUsers(con, name, role, purok, status);
        } catch (SQLException e) {
            System.err.println("Error searching users");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean updateUser(UserData user) {
        Connection con = null;
        try {
            con = DBConnection.connect();
            con.setAutoCommit(false);

            boolean success = userManagementDao.updateUser(con, user);

            if (success) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error updating user");
            e.printStackTrace();
            return false;

        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean toggleBanStatus(UserData user) {
        Connection con = null;
        try {
            con = DBConnection.connect();
            con.setAutoCommit(false);

            boolean success = userManagementDao.setBanStatus(con, user.getId(), user.isBanned());

            if (success) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error toggling ban status");
            e.printStackTrace();
            return false;

        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}