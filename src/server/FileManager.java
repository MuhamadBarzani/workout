package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import utils.LocalDateTimeAdapter;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class FileManager {
    private static FileManager instance;
    private final Gson gson;
    private final String DATA_DIR = "data";

    private FileManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        createDataDirectory();
    }

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
    }

    public <T> void saveData(String filename, T data) {
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            String json = gson.toJson(data);
            Files.writeString(filePath, json);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    public <T> T loadData(String filename, Class<T> type) {
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            if (!Files.exists(filePath)) {
                return null;
            }
            String json = Files.readString(filePath);
            return gson.fromJson(json, type);
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return null;
        }
    }

    public <T> List<T> loadList(String filename, Class<T[]> type) {
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }
            String json = Files.readString(filePath);
            T[] array = gson.fromJson(json, type);
            return array != null ? Arrays.asList(array) : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error loading list: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public synchronized int getNextId(String counterFile) {
        try {
            Path counterPath = Paths.get(DATA_DIR, counterFile);
            int currentId = 1;

            if (Files.exists(counterPath)) {
                String idStr = Files.readString(counterPath);
                currentId = Integer.parseInt(idStr) + 1;
            }

            Files.writeString(counterPath, String.valueOf(currentId));
            return currentId;
        } catch (IOException e) {
            System.err.println("Error managing ID counter: " + e.getMessage());
            return -1;
        }
    }
}