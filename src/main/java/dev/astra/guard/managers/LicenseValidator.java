package dev.astra.guard.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LicenseValidator {
    private final String apiUrl;
    private final String apiKey;
    private static final Logger LOGGER = Logger.getLogger(LicenseValidator.class.getName());

    public LicenseValidator(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public boolean validate(String license, String serverIp) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            String jsonPayload = String.format("{\"license\":\"%s\",\"ip\":\"%s\"}", license, serverIp);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8).useDelimiter("\\A")) {
                    String response = scanner.hasNext() ? scanner.next() : "";
                    JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                    return jsonObject.has("valid") && jsonObject.get("valid").getAsBoolean();
                }
            } else {
                LOGGER.log(Level.WARNING, "License validation failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "License Checking Failed!", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error parsing license validation response!", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return false;
    }
}