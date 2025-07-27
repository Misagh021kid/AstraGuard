package dev.astra.guard;

import com.github.retrooper.packetevents.PacketEvents;
import dev.astra.guard.commands.AstraCommand;
import dev.astra.guard.listeners.PacketLogger;
import dev.astra.guard.managers.AlertManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import dev.astra.guard.managers.LicenseManager;
import dev.astra.guard.checks.CheckManager;
import dev.astra.guard.config.ConfigManager;
import dev.astra.guard.listeners.PlayerListener;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public final class Main extends JavaPlugin {
    private CheckManager checkManager;
    private ConfigManager configManager;
    private AlertManager alertManager;
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
        configManager = new ConfigManager(this);
        alertManager = new AlertManager(this);
        configManager.init();

        Objects.requireNonNull(getCommand("astra")).setExecutor(new AstraCommand(this));

        new PacketLogger(this).register();

        String licenseKey = getConfig().getString("license-key");
        if (licenseKey == null || licenseKey.isEmpty()) {
            getLogger().severe("No license key found in config.yml! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (licenseKey.equalsIgnoreCase("LICENSE-KEY-HERE")) {
            getLogger().severe("License key not found please set licence key in config.yml!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        LicenseManager licenseManager = new LicenseManager(this);
        licenseManager.checkLicense(licenseKey);


        LicenseManager.LicenseStatus status = licenseManager.checkLicenseStatus(licenseKey);
        if (status.valid && !status.active) {
            getLogger().severe("License Blocked: " + status.reason);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!status.valid) {
            getLogger().severe("Licence Invalid: " + status.reason);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }



            switch (status.status.toLowerCase()) {
                case "active":
                    getLogger().info("License Checked: Valid");
                    break;

                case "soon":
                    getLogger().warning("License will expire soon: " + status.daysLeft + " days left.");
                    break;

                case "expired":
                    getLogger().warning("License Expired: " + status.reason);
                    getServer().getPluginManager().disablePlugin(this);
                    return;

                default:
                    getLogger().warning("Unknown license status: " + status.status);
                    getServer().getPluginManager().disablePlugin(this);
                    return;
            }
        new BukkitRunnable() {
            @Override
            public void run() {
                String licenseKey = getConfig().getString("license-key");
                if (licenseKey == null || licenseKey.isEmpty()) {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                            Bukkit.getPluginManager().disablePlugin(Main.getInstance()));
                    return;
                }

                LicenseManager licenseManager = new LicenseManager(Main.getInstance());
                LicenseManager.LicenseStatus status = licenseManager.checkLicenseStatus(licenseKey);
                licenseManager.checkLicense(licenseKey);

                if (!status.valid || !status.active) {
                    getLogger().severe("License is no longer valid or active. Disabling plugin...");
                    Bukkit.getScheduler().runTask(Main.getInstance(), () ->
                            Bukkit.getPluginManager().disablePlugin(Main.getInstance()));
                }
            }

        }.runTaskTimerAsynchronously(this, 0L, 20L * 60);

        PacketEvents.getAPI().init();
        configManager = new ConfigManager(this);
        configManager.init();
        checkManager = new CheckManager();
        TaskUtil taskUtil = new TaskUtil(this);

        PacketEvents.getAPI().getEventManager()
                .registerListener(new PlayerListener(checkManager));

    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static Main getInstance() {
        return instance;
    }
    public AlertManager getAlertManager() {
        return alertManager;
    }


}
