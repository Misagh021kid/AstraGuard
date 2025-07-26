package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSelectBundleItem;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

public final class BundleA implements Check {
    public String name() {
        return "Bundle-A";
    }

    public void handle(PacketReceiveEvent e) {
        boolean bundlefound = e.getServerVersion().isNewerThan(ServerVersion.V_1_17);
        if (e.getPacketType() == PacketType.Play.Client.SELECT_BUNDLE_ITEM && bundlefound) {
            WrapperPlayClientSelectBundleItem packet = new WrapperPlayClientSelectBundleItem(e);

            if (packet.getSelectedItemIndex() < 0 && packet.getSelectedItemIndex() != -1) {
                kick(e.getPlayer(),e,"selectedindex:" +packet.getSelectedItemIndex());
            }
        }
    }
    private void kick(Player p, PacketReceiveEvent e, String detail) {
        TaskUtil.flag(p, name(), detail);
        e.setCancelled(true);
    }
}