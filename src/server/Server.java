package server;

import java.net.*;
import java.io.*;

import auth.AuthRequest;
import auth.RegisterRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exercise.Exercise;
import exercise.ExerciseModel;
import order.Order;
import order.OrderCreateRequest;
import order.OrderModel;
import order.OrderSupplement;
import payment.PaymentMethod;
import payment.PaymentModel;
import supplement.PurchaseRequest;
import supplement.Supplement;
import supplement.SupplementModel;
import user.User;
import user.UserModel;
import utils.LocalDateTimeAdapter;
import workout.WorkoutPlanRequest;
import workout.models.WorkoutPlan;
import workout.models.WorkoutPlanModel;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
public class Server {
    private static final int PORT = 8080;
    private final OrderModel orderModel;
    private final ExerciseModel exerciseModel;
    private final SupplementModel supplementModel;
    private final UserModel userModel;
    private final WorkoutPlanModel workoutPlanModel;  // Add this field
    private final Gson gson;
    private final PaymentModel paymentModel;

    public Server() {
        this.orderModel = new OrderModel();
        this.exerciseModel = new ExerciseModel();
        this.supplementModel = new SupplementModel();
        this.userModel = new UserModel();
        this.workoutPlanModel = new WorkoutPlanModel();
        this.paymentModel = new PaymentModel();

        // Create Gson instance with date handling and LocalDateTime adapter
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setDateFormat("yyyy-MM-dd")  // Use SQL date format for other date types
                .create();
    }
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        new Thread(() -> {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] parts = inputLine.split("\\|", 2);
                    String command = parts[0];
                    String requestData = parts.length > 1 ? parts[1] : "";

                    String response = handleRequest(command, requestData);
                    out.println(response);
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            }
        }).start();
    }

    private String handleRequest(String command, String requestData) {
        try {
            switch (command) {
                case "GET_USER_ORDERS":
                    int userID = gson.fromJson(requestData, Integer.class);
                    List<Order> orders = orderModel.getUserOrders(userID);
                    return gson.toJson(new ServerResponse(true, gson.toJson(orders)));

                case "GET_ORDER_DETAILS":
                    int orderID = gson.fromJson(requestData, Integer.class);
                    Order order = orderModel.getOrderById(orderID);
                    return gson.toJson(new ServerResponse(order != null,
                            order != null ? gson.toJson(order) : "Order not found"));

                case "GET_ORDER_ITEMS":
                    int itemsOrderID = gson.fromJson(requestData, Integer.class);
                    List<OrderSupplement> items = orderModel.getOrderItems(itemsOrderID);
                    return gson.toJson(new ServerResponse(items != null,
                            items != null ? gson.toJson(items) : "Items not found"));

                case "CREATE_ORDER":
                    OrderCreateRequest createRequest = gson.fromJson(requestData, OrderCreateRequest.class);

                    // First validate all supplement quantities
                    boolean quantitiesAvailable = true;
                    for (OrderSupplement item : createRequest.getItems()) {
                        Supplement supp = supplementModel.getSupplementById(item.getSupplementID());
                        if (supp == null || supp.getQuantityAvailable() < item.getQuantityOrdered()) {
                            quantitiesAvailable = false;
                            break;
                        }
                    }

                    if (!quantitiesAvailable) {
                        return gson.toJson(new ServerResponse(false, "Insufficient quantity available"));
                    }

                    // Proceed with order creation
                    int newOrderID = orderModel.createOrder(
                            createRequest.getUserID(),
                            createRequest.getItems(),
                            createRequest.getDeliveryLocation()
                    );
                    return gson.toJson(new ServerResponse(newOrderID != -1,
                            gson.toJson(newOrderID)));

                case "GET_ALL_EXERCISES":
                    List<Exercise> exercises = exerciseModel.getAllExercises();
                    return gson.toJson(new ServerResponse(true, gson.toJson(exercises)));

                case "CANCEL_ORDER":
                    int cancelOrderID = gson.fromJson(requestData, Integer.class);
                    boolean cancelSuccess = orderModel.cancelOrder(cancelOrderID);
                    return gson.toJson(new ServerResponse(cancelSuccess,
                            "Order " + (cancelSuccess ? "cancelled successfully" : "cancellation failed")));
                case "GET_ALL_SUPPLEMENTS":
                    List<Supplement> supplements = supplementModel.getAllSupplements();
                    return gson.toJson(new ServerResponse(true, gson.toJson(supplements)));
                case "UPDATE_USER":
                    User updatedUser = gson.fromJson(requestData, User.class);
                    boolean updateSuccess = userModel.updateUser(updatedUser);
                    return gson.toJson(new ServerResponse(updateSuccess,
                            updateSuccess ? "User updated successfully" : "Failed to update user"));
                case "GET_SUPPLEMENTS_BY_CATEGORY":
                    String category = gson.fromJson(requestData, String.class);
                    List<Supplement> categorySupplements = supplementModel.getSupplementsByCategory(category);
                    return gson.toJson(new ServerResponse(true, gson.toJson(categorySupplements)));

                case "GET_SUPPLEMENT_INFO":
                    int supplementId = gson.fromJson(requestData, Integer.class);
                    Supplement supplement = supplementModel.getSupplementById(supplementId);
                    return gson.toJson(new ServerResponse(supplement != null,
                            supplement != null ? gson.toJson(supplement) : "Supplement not found"));

                case "PURCHASE_SUPPLEMENT":
                    PurchaseRequest purchaseRequest = gson.fromJson(requestData, PurchaseRequest.class);
                    Supplement purchaseSupp = supplementModel.getSupplementById(purchaseRequest.supplementID());

                    if (purchaseSupp != null && purchaseSupp.getQuantityAvailable() >= purchaseRequest.quantity()) {
                        int newQuantity = purchaseSupp.getQuantityAvailable() - purchaseRequest.quantity();
                        boolean success = supplementModel.updateQuantity(
                                purchaseRequest.supplementID(),
                                newQuantity
                        );
                        return gson.toJson(new ServerResponse(success,
                                success ? "Purchase successful" : "Purchase failed"));
                    }
                    return gson.toJson(new ServerResponse(false, "Insufficient quantity"));
                case "AUTHENTICATE_USER":
                    AuthRequest authRequest = gson.fromJson(requestData, AuthRequest.class);
                    User user = userModel.getUserByEmail(authRequest.email());  // Changed from getEmail()

                    if (user != null && user.getPassword().equals(authRequest.hashedPassword())) {  // Changed from getHashedPassword()
                        user.setPassword(null); // Don't send password back to client
                        return gson.toJson(new ServerResponse(true, gson.toJson(user)));
                    }
                    return gson.toJson(new ServerResponse(false, "Invalid credentials"));
                case "REGISTER_USER":
                    RegisterRequest regRequest = gson.fromJson(requestData, RegisterRequest.class);
                    // Check if email already exists
                    if (userModel.getUserByEmail(regRequest.getEmail()) != null) {
                        return gson.toJson(new ServerResponse(false, "Email already exists"));
                    }

                    User newUser = new User();
                    newUser.setUsername(regRequest.getUsername());
                    newUser.setPassword(regRequest.getHashedPassword());
                    newUser.setEmail(regRequest.getEmail());
                    newUser.setAge(regRequest.getAge());
                    newUser.setHeight(regRequest.getHeight());
                    newUser.setWeight(regRequest.getWeight());
                    newUser.setWorkoutPreference(regRequest.getWorkoutPreference());

                    boolean success = userModel.createUser(newUser);
                    if (success) {
                        newUser = userModel.getUserByEmail(regRequest.getEmail()); // Get the saved user with ID
                        newUser.setPassword(null); // Don't send password back
                        return gson.toJson(new ServerResponse(true, gson.toJson(newUser)));
                    }
                    return gson.toJson(new ServerResponse(false, "Registration failed"));

                case "AUTO_LOGIN":
                    String email = gson.fromJson(requestData, String.class);
                    User autoUser = userModel.getUserByEmail(email);
                    if (autoUser != null) {
                        autoUser.setPassword(null); // Don't send password back
                        return gson.toJson(new ServerResponse(true, gson.toJson(autoUser)));
                    }
                    return gson.toJson(new ServerResponse(false, "Auto-login failed"));

                case "LOGOUT":
                    // Handle any server-side logout logic if needed
                    return gson.toJson(new ServerResponse(true, "Logged out successfully"));
                case "GENERATE_WORKOUT_PLAN":
                    WorkoutPlanRequest planRequest = gson.fromJson(requestData, WorkoutPlanRequest.class);
                    User workoutUser = userModel.getUserByUserId(planRequest.userId());
                    String injuryInfo = userModel.getInjuryInfo(planRequest.userId());

                    WorkoutPlan generatedPlan = workoutPlanModel.generateWorkoutPlan(
                            workoutUser,
                            planRequest.targetGoal(),
                            planRequest.daysPerWeek(),
                            injuryInfo
                    );

                    return gson.toJson(new ServerResponse(generatedPlan != null,
                            generatedPlan != null ? gson.toJson(generatedPlan) : "Failed to generate workout plan"));

                case "GET_WORKOUT_HISTORY":
                    int historyUserId = gson.fromJson(requestData, Integer.class);
                    try {
                        List<WorkoutPlan> history = workoutPlanModel.getUserWorkoutHistory(historyUserId);
                        return gson.toJson(new ServerResponse(true, gson.toJson(history)));
                    } catch (SQLException e) {
                        return gson.toJson(new ServerResponse(false, "Error retrieving workout history: " + e.getMessage()));
                    }

                case "GET_CURRENT_WORKOUT":
                    int currentUserId = gson.fromJson(requestData, Integer.class);
                    try {
                        WorkoutPlan currentPlan = workoutPlanModel.getCurrentWorkoutPlan(currentUserId);
                        return gson.toJson(new ServerResponse(true, gson.toJson(currentPlan)));
                    } catch (SQLException e) {
                        return gson.toJson(new ServerResponse(false, "Error retrieving current workout: " + e.getMessage()));
                    }
                    // Add to Server.java's handleRequest method
                case "PROCESS_PAYMENT":
                    PaymentMethod payment = gson.fromJson(requestData, PaymentMethod.class);
                    boolean paymentSuccess = paymentModel.addPayment(payment);
                    return gson.toJson(new ServerResponse(paymentSuccess,
                            paymentSuccess ? "Payment processed successfully" : "Payment processing failed"));

                case "REMOVE_PAYMENT":
                    int paymentId = gson.fromJson(requestData, Integer.class);
                    boolean removeSuccess = paymentModel.removePayment(paymentId);
                    return gson.toJson(new ServerResponse(removeSuccess,
                            removeSuccess ? "Payment removed successfully" : "Failed to remove payment"));

                case "GET_USER_PAYMENTS":
                    int userId = gson.fromJson(requestData, Integer.class);
                    List<PaymentMethod> payments = paymentModel.getAllPaymentsForUser(userId);
                    return gson.toJson(new ServerResponse(true, gson.toJson(payments)));
                default:
                    return gson.toJson(new ServerResponse(false, "Unknown command"));
            }
        } catch (Exception e) {
            return gson.toJson(new ServerResponse(false, "Error: " + e.getMessage()));
        }
    }
}