// UserController.java
package user;

import client.Client;
import server.ServerResponse;
import com.google.gson.Gson;

import java.io.IOException;

public class UserController {
    private final Client client;
    private User currentUser;
    private final Gson gson;

    public UserController() {
        this.client = new Client();
        this.gson = new Gson();
        try {
            this.client.connect();
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public boolean updateUserProfile(String field, String value) {
        if (currentUser != null) {
            switch (field) {
                case "email":
                    currentUser.setEmail(value);
                    break;
                case "age":
                    currentUser.setAge(Integer.parseInt(value));
                    break;
                case "height":
                    currentUser.setHeight(Double.parseDouble(value));
                    break;
                case "weight":
                    currentUser.setWeight(Double.parseDouble(value));
                    break;
                case "workoutPreference":
                    currentUser.setWorkoutPreference(value);
                    break;
                case "injuryInfo":
                    currentUser.setInjuryInfo(value);
                    break;
                default:
                    return false;
            }

            ServerResponse response = client.sendRequest("UPDATE_USER", currentUser);
            return response.isSuccess();
        }
        return false;
    }

    public boolean deleteUser() {
        if (currentUser != null) {
            ServerResponse response = client.sendRequest("DELETE_USER", currentUser.getUserID());
            if (response.isSuccess()) {
                currentUser = null;
            }
            return response.isSuccess();
        }
        return false;
    }

    // Existing methods remain the same
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

}