// Client.java
package client;

import java.net.*;
import java.io.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import server.ServerResponse;
import utils.LocalDateTimeAdapter;
import java.time.LocalDateTime;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;

    public Client() {
        // Initialize Gson with LocalDateTime adapter
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public ServerResponse sendRequest(String command, Object data) {
        try {
            String jsonData = gson.toJson(data);
            String request = command + "|" + jsonData;
            out.println(request);

            String response = in.readLine();
            return gson.fromJson(response, ServerResponse.class);
        } catch (IOException e) {
            return new ServerResponse(false, "Network error: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}
