// dev/astra/guard/Main.java
package dev.astra.guard;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import dev.astra.guard.checks.CheckManager;
import dev.astra.guard.config.ConfigManager;
import dev.astra.guard.listeners.PlayerListener;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private CheckManager checkManager;
    private ConfigManager configManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
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
}
