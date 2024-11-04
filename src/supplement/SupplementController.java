package supplement;

import client.Client;
import server.ServerResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.List;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class SupplementController {
    private final Client client;
    private final Gson gson;

    public SupplementController() {
        this.client = new Client();
        this.gson = new Gson();
        try {
            this.client.connect();
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public List<Supplement> getAllSupplements() {
        try {
            ServerResponse response = client.sendRequest("GET_ALL_SUPPLEMENTS", "");
            if (response.isSuccess()) {
                Type listType = new TypeToken<List<Supplement>>(){}.getType();
                return gson.fromJson(response.getMessage(), listType);
            }
        } catch (Exception e) {
            System.out.println("Error fetching supplements: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<Supplement> getSupplementsByCategory(String category) {
        try {
            ServerResponse response = client.sendRequest("GET_SUPPLEMENTS_BY_CATEGORY", category);
            if (response.isSuccess()) {
                Type listType = new TypeToken<List<Supplement>>(){}.getType();
                return gson.fromJson(response.getMessage(), listType);
            }
        } catch (Exception e) {
            System.out.println("Error fetching supplements by category: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public Supplement getSupplementInfo(int supplementID) {
        try {
            ServerResponse response = client.sendRequest("GET_SUPPLEMENT_INFO", supplementID);
            if (response.isSuccess()) {
                return gson.fromJson(response.getMessage(), Supplement.class);
            }
        } catch (Exception e) {
            System.out.println("Error fetching supplement info: " + e.getMessage());
        }
        return null;
    }

}

