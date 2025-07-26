package dev.astra.guard;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import dev.astra.guard.managers.LicenseManager;
import dev.astra.guard.checks.CheckManager;
import dev.astra.guard.config.ConfigManager;
import dev.astra.guard.listeners.PlayerListener;
import dev.astra.guard.utils.TaskUtil;
import dev.astra.guard.managers.LicenseManager.LicenseStatus;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private CheckManager checkManager;
    private ConfigManager configManager;
    private LicenseManager licenseManager;
    private static Main instance;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        String licenseKey = getConfig().getString("license-key");

        licenseManager = new LicenseManager(this);

        getLogger().info("Checking license status...");
        LicenseStatus status = licenseManager.checkLicenseStatus(licenseKey);

        if (!status.valid) {
            getLogger().severe("=== LICENSE VALIDATION FAILED ===");
            getLogger().severe("Reason: " + status.reason);

            if ("expired".equals(status.status)) {
                getLogger().severe("❌ Your license has expired! Please renew it.");
            } else if ("inactive".equals(status.status)) {
                getLogger().severe("❌ Your license is inactive! Please contact support.");
            } else {
                getLogger().severe("❌ Disabling plugin due to invalid license.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        } else {
            if ("soon".equals(status.status)) {
                getLogger().warning("License will expire in " + status.daysLeft + " days!");
            }
            getLogger().info("License validation successful! Welcome to Astra Guard.");
            getLogger().info("Plugin enabled for: " + status.pluginName);
        }

        PacketEvents.getAPI().init();
        configManager = new ConfigManager(this);
        configManager.init();
        checkManager = new CheckManager();
        TaskUtil.bootstrap(this);

        PacketEvents.getAPI().getEventManager()
                .registerListener(new PlayerListener(checkManager));

        getLogger().info("Astra Guard has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling Astra Guard...");
        PacketEvents.getAPI().terminate();
        getLogger().info("Astra Guard disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LicenseManager getLicenseManager() {
        return licenseManager;
    }

    public static Main getInstance() {
        return instance;
    }
}