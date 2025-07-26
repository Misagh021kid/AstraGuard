package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPickItem;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

public final class ItemB implements Check {

    @Override
    public String name() {
        return "Item-B";
    }

    @Override
    public void handle(PacketReceiveEvent e) {
        if (e.getPacketType() != PacketType.Play.Client.PICK_ITEM) {
            return;
        }

        WrapperPlayClientPickItem pickItem = new WrapperPlayClientPickItem(e);
        int slot = pickItem.getSlot();
        if (slot < 0) {
            Player player = e.getPlayer();
            if (player == null) return;
            kick(player, e, "invalid_pick_slot=" + slot);
        }
    }


    private void kick(Player player, PacketReceiveEvent e, String detail) {
        TaskUtil.flag(player, name(), detail);
        e.setCancelled(true);
    }
}