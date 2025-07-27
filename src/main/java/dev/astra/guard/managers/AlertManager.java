package dev.astra.guard.managers;

import dev.astra.guard.Main;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AlertManager {
    private final Set<UUID> toggled;
    private final Main plugin;

    public AlertManager(Main plugin) {
        this.plugin = plugin;
        this.toggled = ConcurrentHashMap.newKeySet();
    }

    public boolean toggle(UUID uuid) {
        if (!toggled.remove(uuid)) {
            toggled.add(uuid);
            return true;
        }
        return false;
    }

    public boolean isEnabled(UUID uuid) {
        return toggled.contains(uuid);
    }

    public void reload() {
        toggled.clear();
    }
}
