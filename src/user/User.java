// User.java
package user;

public class User {
    private int userID;
    private String username;
    private String password;
    private String email;
    private int age;
    private double height;
    private double weight;
    private String workoutPreference;
    private String injuryInfo;
    // Default constructor
    public User() {}

    // Full constructor
    public User(int userID, String username, String password, String email,
                int age, double height, double weight, String workoutPreference, String injuryInfo) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.workoutPreference = workoutPreference;
        this.injuryInfo = injuryInfo;
    }
    public String getInjuryInfo() {
        return injuryInfo;
    }

    public void setInjuryInfo(String injuryInfo) {
        this.injuryInfo = injuryInfo;
    }
    // Getters and Setters
    public int getUserID() {
        return userID;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
    public String getWorkoutPreference() {
        return workoutPreference;
    }

    public void setWorkoutPreference(String workoutPreference) {
        this.workoutPreference = workoutPreference;
    }
}
