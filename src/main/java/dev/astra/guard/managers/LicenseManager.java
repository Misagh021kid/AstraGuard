package dev.astra.guard.managers;

import dev.astra.guard.Main;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LicenseManager {

    private final Main plugin;
    @SuppressWarnings("FieldMayBeFinal")
    private static String LICENSE_SERVER;
    @SuppressWarnings("FieldMayBeFinal")
    private static String API_KEY;
    static {
        LICENSE_SERVER = "http://testserver";
        API_KEY = "AstRaDeeeveeepolmeeeent2025FindSecrrretttKeeyyFoRRRPLULGIN"; // i put it just for example (:
    }

    public LicenseManager(Main plugin) {
        this.plugin = plugin;
    }

    public void checkLicense(String licenseKey) {
        try {
            String serverIP = getServerIP();

            JsonObject requestData = new JsonObject();
            requestData.addProperty("license", licenseKey);
            requestData.addProperty("ip", serverIP);

            String response = sendPostRequest(LICENSE_SERVER + "/validate", requestData.toString());
            JsonObject responseObject = JsonParser.parseString(response).getAsJsonObject();

            boolean valid = responseObject.get("valid").getAsBoolean();
            String message = responseObject.get("message").getAsString();

            if (!valid) {
                plugin.getLogger().severe("License validation failed: " + message);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to validate license: " + e.getMessage());
        }
    }

    public LicenseStatus checkLicenseStatus(String licenseKey) {
        try {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("license", licenseKey);

            String response = sendPostRequest("http://5.42.217.162:5000" + "/status", requestData.toString());
            JsonObject responseObject = JsonParser.parseString(response).getAsJsonObject();

            LicenseStatus status = new LicenseStatus();
            status.valid = responseObject.get("valid").getAsBoolean();

            if (responseObject.has("active")) {
                status.active = responseObject.get("active").getAsBoolean();
            }

            if (responseObject.has("status")) {
                status.status = responseObject.get("status").getAsString();
            }

            if (responseObject.has("plugin_name")) {
                status.pluginName = responseObject.get("plugin_name").getAsString();
            }

            if (responseObject.has("days_left")) {
                status.daysLeft = responseObject.get("days_left").getAsInt();
            }

            if (responseObject.has("reason")) {
                status.reason = responseObject.get("reason").getAsString();
            }

            return status;

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to check license status: " + e.getMessage());
            LicenseStatus errorStatus = new LicenseStatus();
            errorStatus.valid = false;
            errorStatus.reason = "Connection failed";
            return errorStatus;
        }
    }

    private String sendPostRequest(String urlString, String jsonInputString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            throw new RuntimeException("HTTP Error: " + responseCode);
        }
    }

    private String getServerIP() {
        try {
            String ip = java.net.InetAddress.getLocalHost().getHostAddress();
            if (ip == null || ip.isEmpty() || "127.0.0.1".equals(ip)) {
                return getPublicIP();
            }
            if (plugin.getConfig().getString("ip") != null) {
                plugin.getConfig().set("ip", null);
            }
            return ip;
        } catch (Exception e) {
            if (plugin.getConfig().getString("ip") == null) {
                plugin.getConfig().set("ip", "127.0.0.1");
            }
            return plugin.getConfig().getString("ip");
        }
    }

    private String getPublicIP() {
        try {
            boolean isPterodactyl = System.getenv("P_SERVER_UUID") != null;

            if (isPterodactyl) {
                plugin.getLogger().info("Detected Pterodactyl environment, using SERVER_IP env or fallback.");

                String envIp = System.getenv("SERVER_IP");
                if (envIp != null && !envIp.equals("0.0.0.0")) {
                    return envIp;
                }

                return "127.0.0.1";
            }

            URL url = new URL("http://5.42.217.162:5000/ip/");
            plugin.getLogger().info("Requesting license check from: " + url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "AstraGuard");


            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                plugin.getLogger().info("License server response: " + response);
                return response.toString().trim();
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Could not get public IP: " + e.getMessage());
            return "127.0.0.1";
        }
    }


    public static class LicenseStatus {
        public boolean valid = false;
        public boolean active = true;
        public String status = "unknown";
        public String pluginName = "";
        public int daysLeft = 0;
        public String reason = "";

        @Override
        public String toString() {
            return "LicenseStatus{" +
                    "valid=" + valid +
                    ", active=" + active +
                    ", status='" + status + '\'' +
                    ", pluginName='" + pluginName + '\'' +
                    ", daysLeft=" + daysLeft +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }
}
