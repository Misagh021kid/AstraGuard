package dev.astra.guard.commands;

import dev.astra.guard.Main;
import dev.astra.guard.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AstraCommand implements CommandExecutor {

    private final Main plugin;

    public AstraCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("astraguard.admin")) {
            sender.sendMessage("You dont have Permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /astra reload");
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            try {
                plugin.getConfigManager().reload();
                sender.sendMessage("Config Reloaded.");
                plugin.getLogger().info(sender.getName() + " reloaded the configuration.");
            } catch (Exception e) {
                sender.sendMessage("Failed to Reload: " + e.getMessage());
                plugin.getLogger().severe("Error reloading config: " + e.getMessage());
                e.printStackTrace();
            }
            return true;
        } else {
            sender.sendMessage("Unknown subcommand. Usage: /astra reload");
            return true;
        }
    }
}