package dev.astra.guard;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UpdateChecker {

    private final Main plugin;
    @SuppressWarnings("FieldMayBeFinal")
    private static String UPDATE_URL;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    static {
        UPDATE_URL = "http://5.42.217.162:5000/version";
    }

    public UpdateChecker(Main plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> check() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPDATE_URL))
                .header("Accept", "application/json")
                .build();

        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleResponse)
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Failed to check for updates: " + ex.getMessage());
                    return null;
                });
    }

    private void handleResponse(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String latestVersion = json.get("version").getAsString();
            String currentVersion = plugin.getPluginVersion();


            if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                plugin.getLogger().warning("A new version of AstraGuard is available!");
                plugin.getLogger().warning("Current version: " + currentVersion + " → New version: " + latestVersion);
                plugin.getLogger().warning("Please visit the update page to download the latest version.");
            } else {
                plugin.getLogger().info("You are running the latest version of AstraGuard.");
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error parsing update response: " + e.getMessage(), e);
        }
    }
}
