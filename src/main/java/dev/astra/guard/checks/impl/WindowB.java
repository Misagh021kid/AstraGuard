package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

public final class WindowB implements Check {

    @Override
    public String name() {
        return "Window-B";
    }

    @Override
    public void handle(PacketReceiveEvent event) {
        // Exploit only affects ≤ 1.20.4
        if (event.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) return;
        if (event.getPacketType() != PacketType.Play.Client.CLICK_WINDOW)          return;

        Player player = event.getPlayer();
        if (player == null) return;

        /* ───────── SAFE WRAPPER CONSTRUCTION ───────── */
        WrapperPlayClientClickWindow click;
        try {
            click = new WrapperPlayClientClickWindow(event);
        } catch (IllegalStateException ex) {
            flag(player, event, "malformed or unsupported NBT");
            return;
        }

        int windowId  = click.getWindowId();
        int clickType = click.getWindowClickType().ordinal();
        int slot      = click.getSlot();

        // Pattern: type 2 (shift-double-click) with negative slot
        if (windowId >= 0 && clickType == 2 && slot < 0) {
            flag(player, event,
                    "win=" + windowId + ", type=" + clickType + ", slot=" + slot);
        }
    }

    /* ───────── helper ───────── */
    private void flag(Player p, PacketReceiveEvent e, String detail) {
        TaskUtil.flag(p, name(), detail);
        e.setCancelled(true);
    }
}