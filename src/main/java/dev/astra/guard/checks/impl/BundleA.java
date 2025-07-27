package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSelectBundleItem;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

public final class BundleA implements Check {

    @Override public String name() { return "Bundle-A"; }

    @Override
    public void handle(PacketReceiveEvent e) {
        if (e.getPacketType() != PacketType.Play.Client.SELECT_BUNDLE_ITEM) return;
        if (!e.getServerVersion().isNewerThan(ServerVersion.V_1_17))       return;

        Player player = e.getPlayer();
        if (player == null) return;

        try {
            WrapperPlayClientSelectBundleItem pkt = new WrapperPlayClientSelectBundleItem(e);
            int idx = pkt.getSelectedItemIndex();

            if (idx < 0 || idx > 127) {
                flag(player, e, "index="+idx);
            }
        } catch (IllegalArgumentException ex) {
            flag(player, e, "malformed packet"+ " ");
        }
    }

    private void flag(Player p, PacketReceiveEvent e, String detail) {
        TaskUtil.flag(p, name(), detail);
        e.setCancelled(true);
    }
}
