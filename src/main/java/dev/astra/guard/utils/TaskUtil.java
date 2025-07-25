package dev.astra.guard.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.astra.guard.Main;
import dev.astra.guard.config.ConfigManager;
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
                .<UUID, LongAdder>build();
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

    public static void flag(Player p, String check, String detail) {
        ConfigManager c = plugin.getConfigManager();
        int max = c.getMaxViolations();
        LongAdder adder = violations.getIfPresent(p.getUniqueId());
        if (adder == null) {
            adder = new LongAdder();
            violations.put(p.getUniqueId(), adder);
        }
        adder.increment();
        int cnt = adder.intValue();
        plugin.getLogger().warning(c.formatLog(c.getFlagLogFormat(), p.getName(), check, detail, cnt, max));
        if (cnt < max) return;
        sync(() -> {
            if (p.isOnline()) p.kickPlayer(c.getKickMessage().replace("{check}", check));
        });
    }

    public static void reloadConfig() {
        rebuildCache();
    }
}
