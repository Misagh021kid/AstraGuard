// dev/astra/guard/Main.java
package dev.astra.guard;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import dev.astra.guard.managers.LicenseManager;
import dev.astra.guard.checks.CheckManager;
import dev.astra.guard.config.ConfigManager;
import dev.astra.guard.listeners.PlayerListener;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private CheckManager checkManager;
    private ConfigManager configManager;
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
//        String licenseKey = getConfig().getString("license-key");
//        LicenseManager licenseManager = new LicenseManager(this);
//        boolean valid = licenseManager.checkLicense(licenseKey);
//
//        if (!valid) {
//            getLogger().severe("License Checked. invalid! Disabling plugin.");
//            getServer().getPluginManager().disablePlugin(this);
//        } else {
//            getLogger().info("License Checked Valid! Plugin enabled.");
//        }

        PacketEvents.getAPI().init();
        configManager = new ConfigManager(this);
        configManager.init();
        checkManager = new CheckManager();
        TaskUtil.bootstrap(this);

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


}
