package server;

// ServerResponse.java
public class ServerResponse {
    private final boolean success;
    private final String message;

    public ServerResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}