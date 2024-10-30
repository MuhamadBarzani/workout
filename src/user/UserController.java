// UserController.java
package user;

public class UserController {
    private UserModel model;
    private User currentUser;  // Added to maintain current user state

    public UserController(UserModel model) {
        this.model = model;
        this.currentUser = null;
    }

    public boolean saveUser(String username, String password, String email, int age,
                            double height, double weight, String workoutPreference) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setAge(age);
        newUser.setHeight(height);
        newUser.setWeight(weight);
        newUser.setWorkoutPreference(workoutPreference);

        boolean success = model.createUser(newUser);
        if (success) {
            currentUser = newUser;
        }
        return success;
    }

    public User getUserProfile(String email) {
        // Use model to fetch user data
        User user = model.getUserByEmail(email);
        if (user != null) {
            currentUser = user;  // Update current user if found
        }
        return user;
    }

    public boolean updateUserProfile(String email, int age, double height,
                                     double weight, String workoutPreference) {
        if (currentUser != null) {
            currentUser.setEmail(email);
            currentUser.setAge(age);
            currentUser.setHeight(height);
            currentUser.setWeight(weight);
            currentUser.setWorkoutPreference(workoutPreference);

            return model.updateUser(currentUser);
        }
        return false;
    }

    public boolean deleteUser() {
        if (currentUser != null) {
            boolean success = model.deleteUser(currentUser.getUserID());
            if (success) {
                currentUser = null;  // Clear current user if deletion successful
            }
            return success;
        }
        return false;
    }

    // Added methods to manage current user state
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean hasCurrentUser() {
        return currentUser != null;
    }
}