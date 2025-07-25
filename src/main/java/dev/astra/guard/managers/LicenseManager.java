package dev.astra.guard.managers;

import org.bukkit.plugin.java.JavaPlugin;

public class LicenseManager {

    private final JavaPlugin plugin;

    public LicenseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean checkLicense(String licenseKey) {
        String serverIp = plugin.getServer().getIp();
        plugin.getLogger().info("Checking license for IP: " + serverIp);

        String apiUrl = "http://5.42.217.162:3000/";
        String apiKey = "AstRaDeeeveeepolmeeeent2025FindSecrrretttKeeyyFoRRRPLULGIN";

        LicenseValidator validator = new LicenseValidator(apiUrl, apiKey);
        return validator.validate(licenseKey, serverIp);
    }
}
