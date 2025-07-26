package dev.astra.guard.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacySection();

    private final JavaPlugin plugin;
    private FileConfiguration cfg;

    private int maxViolations;
    private long violationResetMinutes;
    private String kickMessage;
    private String flagLogFormat;
    private int softPayload;
    private int hardPayload;
    private int maxPayloadsPerTick;
    private String itemAMessage;

    public int getInt(String path, int def) {
        return cfg.getInt(path, def);
    }

    public double getDouble(String path, double def) {
        return cfg.getDouble(path, def);
    }

    public long getLong(String path, long def) {
        return cfg.getLong(path, def);
    }

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
        kickMessage = toLegacy(cfg.getString("messages.kick",
                "<red>AstraGuard: Crash attempt detected <gray>({check})"));
        flagLogFormat = cfg.getString("messages.flag-log",
                "{player} flagged by {check} ({detail}) [{count}/{max}]");
        softPayload = cfg.getInt("netty.payload.soft", 262_144);
        hardPayload = cfg.getInt("netty.payload.hard", 1_048_576);
        maxPayloadsPerTick = cfg.getInt("netty.payload.maxPerTick", 50);
    }

    private String toLegacy(String s) {
        Component c = MM.deserialize(s);
        return LEGACY.serialize(c);
    }

    public int getMaxViolations() {
        return maxViolations;
    }

    public long getViolationResetMinutes() {
        return violationResetMinutes;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getFlagLogFormat() {
        return flagLogFormat;
    }

    public int getSoftPayload() {
        return softPayload;
    }

    public int getHardPayload() {
        return hardPayload;
    }

    public int getMaxPayloadsPerTick() {
        return maxPayloadsPerTick;
    }


    public String getItemAMessage() {
        return itemAMessage;
    }

    public String formatLog(String tpl, String p, String ck, String d, int cnt, int max) {
        return tpl.replace("{player}", p).replace("{check}", ck)
                .replace("{detail}", d).replace("{count}", String.valueOf(cnt))
                .replace("{max}", String.valueOf(max));
    }
}