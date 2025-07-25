package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

public final class ItemA implements Check {

    @Override
    public String name() {
        return "Item-A";
    }

    @Override
    public void handle(PacketReceiveEvent e) {
        PacketTypeCommon type = e.getPacketType();
        ItemStack item = null;


        if (type == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            WrapperPlayClientCreativeInventoryAction wrapper = new WrapperPlayClientCreativeInventoryAction(e);
            item = wrapper.getItemStack();
        }

        if (item != null && item.getNBT() != null) {
            String nbt = item.getNBT().toString();
            int nbtSize = nbt.length();

            int maxDataSize = e.getServerVersion().isOlderThan(ServerVersion.V_1_16) ? 8192 : 32768;

            if (nbtSize > maxDataSize) {
                e.setCancelled(true);
                Player player = e.getPlayer();
                TaskUtil.flag(player,"ItemA", "bytesSize: " + nbtSize);
            }
        }
    }
}
