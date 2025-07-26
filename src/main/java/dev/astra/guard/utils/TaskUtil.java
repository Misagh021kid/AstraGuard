package dev.astra.guard.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.astra.guard.Main;
import dev.astra.guard.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public final class TaskUtil {
    private static Main plugin;
    private static Cache<UUID, LongAdder> violations;

    private TaskUtil() {
    }

    public static void bootstrap(Main p) {
        plugin = p;
        rebuildCache();
    }

    private static void rebuildCache() {
        ConfigManager c = plugin.getConfigManager();
        violations = CacheBuilder.newBuilder()
                .expireAfterAccess(c.getViolationResetMinutes(), TimeUnit.MINUTES)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();

    }

    public static void async(Runnable r) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
    }

    public static void sync(Runnable r) {
        Bukkit.getScheduler().runTask(plugin, r);
    }

    public static void later(Runnable r, long t) {
        Bukkit.getScheduler().runTaskLater(plugin, r, t);
    }

    public static void flag(Player player, String check, String detail) {
        var cfg  = plugin.getConfigManager();
        int max  = cfg.getMaxViolations();
        UUID uid = player.getUniqueId();

        LongAdder adder = violations.getIfPresent(uid);
        if (adder == null) {
            adder = new LongAdder();
            violations.put(uid, adder);
        }
        adder.increment();
        int count = adder.intValue();

        if (count <= max) {
            plugin.getLogger().warning(cfg.formatLog(
                    cfg.getFlagLogFormat(), player.getName(), check, detail, count, max));
        }

        if (count >= max) {
            sync(() -> {
                if (player.isOnline()) {
                    String msg = cfg.getKickMessage().replace("{check}", check);
                    player.kick(Component.text(msg));
                }
            });
            adder.reset();
        }
    }

    public static void reloadConfig() {
        rebuildCache();
    }
}