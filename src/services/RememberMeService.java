package services;

import java.io.*;
import java.util.Properties;

public class RememberMeService {

    private final File storageFile;

    // Default sample directory (modifiable)
    public static final String DEFAULT_DIR = System.getProperty("user.home")
            + File.separator + "e_reporting"
            + File.separator + "auth";

    public RememberMeService() {
        this(DEFAULT_DIR);
    }

    public RememberMeService(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.storageFile = new File(dir, "remember_me.properties");
    }

    // Save credentials (you can choose to hash password later)
    public void saveCredentials(String username, String password) {
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("password", password);

        try (FileOutputStream fos = new FileOutputStream(storageFile)) {
            props.store(fos, "Remember Me Credentials");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RememberedUser loadCredentials() {
        if (!storageFile.exists())
            return null;

        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(storageFile)) {
            props.load(fis);

            String username = props.getProperty("username");
            String password = props.getProperty("password");

            if (username == null || password == null)
                return null;

            return new RememberedUser(username, password);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void clear() {
        if (storageFile.exists()) {
            storageFile.delete();
        }
    }

    // Simple DTO
    public static class RememberedUser {
        public final String username;
        public final String password;

        public RememberedUser(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}