package dev.astra.guard.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheLoader;
import dev.astra.guard.Main;
import dev.astra.guard.config.ConfigManager;
import dev.astra.guard.managers.AlertManager;
import dev.astra.guard.webhook.WebhookConfig;
import dev.astra.guard.webhook.WebhookUtil;
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
        if (plugin == null || violations == null) return;

        ConfigManager cfg = plugin.getConfigManager();
        int max = cfg.getMaxViolations();
        UUID uuid = player.getUniqueId();

        LongAdder counter;
        try {
            counter = violations.get(uuid);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load violation counter for " + player.getName());
            return;
        }

        counter.increment();
        int count = counter.intValue();

        String logMsg = cfg.formatLog(cfg.getFlagLogFormat(), player.getName(), check, detail, count, max);
        plugin.getLogger().warning(logMsg);

        Component alert = Component.text(logMsg);
        AlertManager manager = plugin.getAlertManager();
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("astraguard.alerts"))
                .filter(p -> manager.isEnabled(p.getUniqueId()))
                .forEach(p -> p.sendMessage(alert));


        WebhookUtil.sendCheckTriggeredWebhook(player.getName(), check, detail,count,max);

        if (count >= max) {
            violations.invalidate(uuid);
            runSync(() -> {
                if (player.isOnline()) {
                    String kickMsg = cfg.getKickMessage().replace("{check}", check);
                    player.kick(Component.text(kickMsg));
                    WebhookUtil.sendPlayerKickedWebhook(player.getName(),check,detail);
                }
            });
        }
    }

    public void reloadConfig() {
        loadCache();
    }
}
