package dev.astra.guard.config;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration cfg;
    @Getter
    private int maxViolations;
    @Getter
    private long violationResetMinutes;
    @Getter
    private String kickMessage;
    @Getter
    private String flagLogFormat;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        plugin.saveDefaultConfig();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        cfg = plugin.getConfig();
        maxViolations = cfg.getInt("violations.max", 3);
        violationResetMinutes = cfg.getLong("violations.reset-minutes", 10);

        kickMessage = deserializeToLegacy(cfg.getString("messages.kick",
                "<red>AstraGuard: Crash attempt detected <gray>({check})"));

        flagLogFormat = cfg.getString("messages.flag-log",
                "{player} flagged by {check} ({detail}) [{count}/{max}]");
    }

    private String deserializeToLegacy(String s) {
        Component component = MiniMessage.miniMessage().deserialize(s);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public String formatLog(String template, String p, String c,
                            String d, int cnt, int max) {
        return template.replace("{player}", p)
                .replace("{check}", c)
                .replace("{detail}", d)
                .replace("{count}", String.valueOf(cnt))
                .replace("{max}", String.valueOf(max));
    }
}
