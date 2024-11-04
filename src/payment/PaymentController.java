package payment;// In client/src/controllers/PaymentController.java


import client.Client;
import com.google.gson.GsonBuilder;
import server.ServerResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentController {
    private final Client client;
    private final Gson gson;

    // In PaymentController.java
    public PaymentController() {
        this.client = new Client();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        try {
            this.client.connect();
        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public boolean processPayment(int userID, int orderID, String paymentType) {
        PaymentMethod payment = new PaymentMethod(userID, orderID, paymentType, LocalDateTime.now());
        ServerResponse response = client.sendRequest("PROCESS_PAYMENT", payment);
        return response.isSuccess();
    }

    public boolean removePayment(int paymentID) {
        ServerResponse response = client.sendRequest("REMOVE_PAYMENT", paymentID);
        return response.isSuccess();
    }

    public List<PaymentMethod> getUserPayments(int userID) {
        ServerResponse response = client.sendRequest("GET_USER_PAYMENTS", userID);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<PaymentMethod>>(){}.getType();
            return gson.fromJson(response.getMessage(), listType);
        }
        return new ArrayList<>();
    }
}