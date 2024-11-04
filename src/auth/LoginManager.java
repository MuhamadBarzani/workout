package auth;

import user.User;
import user.UserController;
import client.Client;
import server.ServerResponse;
import com.google.gson.Gson;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class LoginManager {
    private static final String SESSION_FILE = "session.txt";
    private final UserController userController;
    private final Client client;
    private final Gson gson;
    private String currentUsername;

    public LoginManager(UserController userController) {
        this.userController = userController;
        this.client = new Client();
        this.gson = new Gson();
        try {
            this.client.connect();
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public boolean authenticateUser(String email, String password) {
        try {
            // Create authentication request
            AuthRequest authRequest = new AuthRequest(email, hashPassword(password));
            ServerResponse response = client.sendRequest("AUTHENTICATE_USER", authRequest);

            if (response.isSuccess()) {
                User user = gson.fromJson(response.getMessage(), User.class);
                currentUsername = user.getUsername();
                userController.setCurrentUser(user);
                saveSession(user.getEmail());
                return true;
            }
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
        }
        System.out.println("Invalid login credentials.");
        return false;
    }

    public boolean registerUser(String username, String password, String email,
                                int age, double height, double weight,
                                String workoutPreference, String injuryInfo) {
        try {
            // Create registration request
            RegisterRequest registerRequest = new RegisterRequest(
                    username, hashPassword(password), email,
                    age, height, weight, workoutPreference, injuryInfo
            );

            ServerResponse response = client.sendRequest("REGISTER_USER", registerRequest);

            if (response.isSuccess()) {
                User user = gson.fromJson(response.getMessage(), User.class);
                currentUsername = username;
                userController.setCurrentUser(user);
                saveSession(email);
                System.out.println("User registered successfully.");
                return true;
            } else {
                System.out.println(response.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
        }
        return false;
    }

    public boolean autoLogin() {
        try {
            String savedEmail = readSessionFile();
            if (savedEmail != null && !savedEmail.trim().isEmpty()) {
                ServerResponse response = client.sendRequest("AUTO_LOGIN", savedEmail);
                if (response.isSuccess()) {
                    User user = gson.fromJson(response.getMessage(), User.class);
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
            client.sendRequest("LOGOUT", "");
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
            return password;
        }
    }

}

