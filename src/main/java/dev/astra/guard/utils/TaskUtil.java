package dev.astra.guard.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheLoader;
import dev.astra.guard.Main;
import dev.astra.guard.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public final class TaskUtil {
    private static Main plugin;
    private static LoadingCache<UUID, LongAdder> violations;

    public TaskUtil(Main plugin) {
        TaskUtil.plugin = plugin;
        loadCache();
    }

    private void loadCache() {
        ConfigManager cfg = plugin.getConfigManager();
        violations = CacheBuilder.newBuilder()
                .expireAfterAccess(cfg.getViolationResetMinutes(), TimeUnit.MINUTES)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull LongAdder load(@NotNull UUID key) {
                        return new LongAdder();
                    }
                });
    }

    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void runSync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void runLater(Runnable task, long ticks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
    }

    public static void flag(Player player, String check, String detail) {
        ConfigManager cfg = plugin.getConfigManager();
        int max = cfg.getMaxViolations();
        UUID uuid = player.getUniqueId();

        LongAdder counter = violations.getUnchecked(uuid);
        counter.increment();
        int count = counter.intValue();

        String logMsg = cfg.formatLog(cfg.getFlagLogFormat(), player.getName(), check, detail, count, max);
        plugin.getLogger().warning(logMsg);

        Component alert = Component.text(logMsg);
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("astraguard.alerts"))
                .forEach(p -> p.sendMessage(alert));

        if (count >= max) {
            violations.invalidate(uuid);
            runSync(() -> {
                if (player.isOnline()) {
                    String kickMsg = cfg.getKickMessage().replace("{check}", check);
                    player.kick(Component.text(kickMsg));
                }
            });
        }
    }

    public void reloadConfig() {
        loadCache();
    }
}
