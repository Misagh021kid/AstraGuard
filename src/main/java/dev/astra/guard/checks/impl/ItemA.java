package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import dev.astra.guard.Main;
import dev.astra.guard.checks.Check;
import dev.astra.guard.config.ConfigManager;
import dev.astra.guard.utils.TaskUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class ItemA implements Check {
    private static final ConfigManager configManager = new ConfigManager(Main.getInstance());
    private static final int MAX_FLAGS = configManager.getMaxFlagItemA();
    private static final Map<UUID, Integer> flagCounts = new ConcurrentHashMap<>();


    @Override
    public String name() {
        return "Item-A";
    }

    @Override
    public void handle(PacketReceiveEvent e) {
        PacketTypeCommon type = e.getPacketType();
        ItemStack item;


        if (type == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            WrapperPlayClientCreativeInventoryAction wrapper = new WrapperPlayClientCreativeInventoryAction(e);
            item = wrapper.getItemStack();

            if (item != null && item.getNBT() != null) {
                String nbt = item.getNBT().toString();

                int nbtSize = nbt.length();

                int maxDataSize = e.getServerVersion().isOlderThan(ServerVersion.V_1_16) ? 8192 : 32768;

                if (nbtSize > maxDataSize) {
                    e.setCancelled(true);
                    Player player = e.getPlayer();

                    flag(player, "bytesSize: " + nbtSize);
                }
            }
        } else if (type == PacketType.Play.Client.NAME_ITEM) {
            WrapperPlayClientNameItem wrapper = new WrapperPlayClientNameItem(e);
            String itemname = wrapper.getItemName();

            if (itemname != null && !itemname.isEmpty()) {
                e.setCancelled(true);

                Player player = e.getPlayer();
                org.bukkit.inventory.ItemStack bukkitItem = player.getInventory().getItemInMainHand();

                if (bukkitItem.hasItemMeta()) {
                    ItemMeta meta = bukkitItem.getItemMeta();
                    if (meta != null && meta.lore() != null) {
                        List<net.kyori.adventure.text.Component> lore = meta.lore();
                        net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText();
                        if (lore != null) {
                            boolean tooManyLines = lore.size() > 30;
                            boolean tooLongLine = lore.stream().anyMatch(line -> plain.serialize(line).length() > 150);

                            if (tooManyLines || tooLongLine) {
                                flag(player, "tooManyLines: " + (tooManyLines ? "lines" : "length"));
                            }
                        }
                    }
                }
            }
        }
    }

    private void flag(Player player, String reason) {
        UUID id = player.getUniqueId();
        int newCount = flagCounts.merge(id, 1, Integer::sum);
        TaskUtil.flag(player, name(), reason);

        if (newCount >= MAX_FLAGS) {
            kick(player);
            flagCounts.remove(id);
        }
    }
    private void kick(Player player) {
        player.kick(Component.text(configManager.getItemAMessage()));
    }
}
