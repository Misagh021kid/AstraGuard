package dev.astra.guard.commands;

import dev.astra.guard.Main;
import dev.astra.guard.config.ConfigManager;
import dev.astra.guard.managers.AlertManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AstraCommand implements CommandExecutor {
    private final Main plugin;
    private final AlertManager alertManager;

    public AstraCommand(Main plugin) {
        this.plugin = plugin;
        ConfigManager cfg = plugin.getConfigManager();
        this.alertManager = plugin.getAlertManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (!sender.hasPermission("astraguard.admin")) {
            sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(usage());
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "alerts" -> handleToggleAlerts(sender);
            default -> {
                sender.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
                yield true;
            }
        };
    }

    private boolean handleReload(CommandSender sender) {
        try {
            plugin.getConfigManager().reload();
            alertManager.reload();
            sender.sendMessage(Component.text("Configuration reloaded.", NamedTextColor.GREEN));
            plugin.getLogger().info(sender.getName() + " reloaded the configuration.");
        } catch (Exception e) {
            sender.sendMessage(Component.text("Reload failed: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
        }
        return true;
    }

    private boolean handleToggleAlerts(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can toggle alerts.", NamedTextColor.YELLOW));
            return true;
        }

        boolean enabled = alertManager.toggle(player.getUniqueId());
        Component msg = enabled
                ? Component.text("Alerts: ENABLED", NamedTextColor.GREEN)
                : Component.text("Alerts: DISABLED", NamedTextColor.GRAY);
        player.sendMessage(msg);
        return true;
    }

    private Component usage() {
        return Component.text("Usage: /astra <reload|alerts>", NamedTextColor.YELLOW);
    }
}