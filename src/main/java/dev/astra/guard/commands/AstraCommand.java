package dev.astra.guard.commands;

import dev.astra.guard.Main;
import dev.astra.guard.managers.AlertManager;
import dev.astra.guard.utils.MiniMessageLegacyParser;
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
        this.alertManager = plugin.getAlertManager();
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {
        if (!sender.hasPermission("astraguard.admin")) {
            sender.sendMessage(colorize("<red>You do not have permission."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(colorize("<yellow>Usage: /astra <reload|alerts>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "reload" -> handleReload(sender);
            case "alerts" -> handleToggleAlerts(sender);
            default -> {
                sender.sendMessage(colorize("<red>Unknown subcommand."));
                yield true;
            }
        };

    }

    private boolean handleReload(CommandSender sender) {
        try {
            plugin.getConfigManager().reload();
            alertManager.reload();
            sender.sendMessage(colorize("&aConfiguration reloaded."));
            plugin.getLogger().info(sender.getName() + " reloaded the configuration.");
        } catch (Exception e) {
            sender.sendMessage(colorize("&cReload failed: " + e.getMessage()));
            plugin.getLogger().severe("Error reloading config: " + e.getMessage());
        }
        return true;
    }

    private boolean handleToggleAlerts(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(colorize("&eOnly players can toggle alerts."));
            return false;
        }

        boolean enabled = alertManager.toggle(player.getUniqueId());
        String msg = enabled
                ? "&aAstraGuardAlerts » ENABLED"
                : "&7AstraGuardAlerts » DISABLED";
        player.sendMessage(colorize(msg));
        return true;
    }

    private String colorize(String message) {
        return MiniMessageLegacyParser.parse(message);
    }
}
