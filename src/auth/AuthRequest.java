package auth;

// Helper classes for authentication requests
public record AuthRequest(String email, String hashedPassword) {
}
