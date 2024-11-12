package user;
import server.FileManager;

import java.util.*;

public class UserModel {
    private static final String USERS_FILE = "users.json";
    private static final String USER_COUNTER = "user_counter.txt";
    private final FileManager fileManager;

    public UserModel() {
        this.fileManager = FileManager.getInstance();
    }

    public boolean createUser(User user) {
        try {
            List<User> users = getAllUsers();

            // Check if email already exists
            if (users.stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
                return false;
            }

            // Generate new user ID
            int userId = fileManager.getNextId(USER_COUNTER);

            // Create new user with generated ID (using constructor)
            User newUser = new User(
                    userId,
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getAge(),
                    user.getHeight(),
                    user.getWeight(),
                    user.getWorkoutPreference(),
                    user.getInjuryInfo()
            );

            users.add(newUser);
            fileManager.saveData(USERS_FILE, users);
            return true;
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public User getUserByEmail(String email) {
        List<User> users = getAllUsers();
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    public User getUserByUserId(int userId) {
        List<User> users = getAllUsers();
        return users.stream()
                .filter(u -> u.getUserID() == userId)
                .findFirst()
                .orElse(null);
    }

    public boolean updateUser(User user) {
        try {
            List<User> users = getAllUsers();
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserID() == user.getUserID()) {
                    users.set(i, user);
                    fileManager.saveData(USERS_FILE, users);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    private List<User> getAllUsers() {
        List<User> users = fileManager.loadList(USERS_FILE, User[].class);
        return users != null ? users : new ArrayList<>();
    }

    public String getInjuryInfo(int userId) {
        User user = getUserByUserId(userId);
        return user != null ? user.getInjuryInfo() : null;
    }
}