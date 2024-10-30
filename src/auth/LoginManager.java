package auth;

import user.User;
import user.UserController;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class LoginManager {
    private static final String SESSION_FILE = "session.txt";
    private final UserController userController;
    private String currentUsername;

    public LoginManager(UserController userController) {
        this.userController = userController;
    }

    public boolean authenticateUser(String email, String password) {
        // Get user by email using controller
        User user = userController.getUserProfile(email);

        if (user != null && verifyPassword(password, user.getPassword())) {
            currentUsername = user.getUsername();
            userController.setCurrentUser(user);
            saveSession(user.getEmail());
            return true;
        } else {
            System.out.println("Invalid login credentials.");
            return false;
        }
    }

    public boolean registerUser(String username, String password, String email,
                                int age, double height, double weight,
                                String workoutPreference) {
        User existingUser = userController.getUserProfile(email);
        if (existingUser != null) {
            System.out.println("An account with this email already exists.");
            return false;
        }

        String hashedPassword = hashPassword(password);

        boolean success = userController.saveUser(username, hashedPassword, email, age,
                height, weight, workoutPreference);

        if (success) {
            System.out.println("User registered successfully.");
            currentUsername = username;
            return true;
        }

        System.out.println("Registration failed.");
        return false;
    }

    public boolean autoLogin() {
        try {
            String savedEmail = readSessionFile();
            if (savedEmail != null && !savedEmail.trim().isEmpty()) {
                User user = userController.getUserProfile(savedEmail);
                if (user != null) {
                    currentUsername = user.getUsername();
                    userController.setCurrentUser(user);
                    System.out.println("Auto-login successful for user: " + user.getUsername());
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("No previous session found or error reading session: " + e.getMessage());
        }
        return false;
    }

    public void logout() {
        try {
            Files.deleteIfExists(Paths.get(SESSION_FILE));
            currentUsername = null;
            userController.setCurrentUser(null);
            System.out.println("Logged out successfully.");
        } catch (IOException e) {
            System.out.println("Error clearing session: " + e.getMessage());
        }
    }

    private void saveSession(String email) {
        try (FileWriter writer = new FileWriter(SESSION_FILE)) {
            writer.write(email);
        } catch (IOException e) {
            System.out.println("Error saving session: " + e.getMessage());
        }
    }

    private String readSessionFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(SESSION_FILE))) {
            return reader.readLine();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return password; // Fallback to plain password if hashing fails
        }
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        String hashedInput = hashPassword(inputPassword);
        return hashedInput.equals(storedPassword);
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}