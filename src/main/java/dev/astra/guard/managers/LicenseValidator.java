package dev.astra.guard.managers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonPayload = String.format("{\"license\": \"%s\", \"ip\": \"%s\"}", license, serverIp);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                return response.contains("\"valid\":true");
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "License Checking Failed!", e);
            return false;
        }

        return false;
    }
}