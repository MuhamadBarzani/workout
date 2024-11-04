package auth;

public class RegisterRequest {
    private final String username;
    private final String hashedPassword;
    private final String email;
    private final int age;
    private final double height;
    private final double weight;
    private final String workoutPreference;
    private final String injuryInfo;

    public RegisterRequest(String username, String hashedPassword, String email,
                           int age, double height, double weight,
                           String workoutPreference, String injuryInfo) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.workoutPreference = workoutPreference;
        this.injuryInfo = injuryInfo;
    }

    // Getters
    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public String getWorkoutPreference() { return workoutPreference; }
    public String getInjuryInfo() { return injuryInfo; }
}