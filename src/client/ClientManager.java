package client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.time.LocalDateTime;

public class ClientManager {
    private static ClientManager instance;
    private final Client client;
    private final Gson gson;

    private ClientManager() {
        this.client = new Client();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setDateFormat("yyyy-MM-dd")
                .create();
        try {
            this.client.connect();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize client: " + e.getMessage());
        }
    }

    public static synchronized ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

    public Client getClient() {
        return client;
    }

    public Gson getGson() {
        return gson;
    }
}
