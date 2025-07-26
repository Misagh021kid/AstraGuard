// dev/astra/guard/checks/impl/NettyC.java
package dev.astra.guard.checks.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.astra.guard.Main;
import dev.astra.guard.checks.Check;
import dev.astra.guard.utils.TaskUtil;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public final class NettyC implements Check {

    private final int MAX_BURST;
    private final int SOFT_BYTES;
    private final int HARD_BYTES;
    private final int MAX_BEES;
    private final int MAX_STRIKES;

    public NettyC() {
        var cfg       = Main.getInstance().getConfigManager();
        MAX_BURST     = cfg.getInt("netty.windowClickGuard.maxBurst",    20);
        SOFT_BYTES    = cfg.getInt("netty.windowClickGuard.nbtSoft",  65536);
        HARD_BYTES    = cfg.getInt("netty.windowClickGuard.nbtHard", 262144);
        MAX_BEES      = cfg.getInt("netty.windowClickGuard.maxBees",     30);
        MAX_STRIKES   = cfg.getInt("netty.windowClickGuard.maxStrikes",   5);
    }

    private static final Cache<UUID, LongAdder> BURST =
            CacheBuilder.newBuilder().expireAfterWrite(50, TimeUnit.MILLISECONDS).build();
    private static final Cache<UUID, LongAdder> STRIKES =
            CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    @Override public String name() { return "Netty-C"; }

    @Override
    public void handle(PacketReceiveEvent ev) {
        if (ev.getPacketType() != PacketType.Play.Client.CLICK_WINDOW) return;

        Player player = ev.getPlayer();
        if (player == null) return;
        UUID uid      = player.getUniqueId();

        LongAdder burst = BURST.getIfPresent(uid);
        if (burst == null) { burst = new LongAdder(); BURST.put(uid, burst); }
        burst.increment();

        if (burst.intValue() > MAX_BURST) {
            flag(player, ev, "burst>"+MAX_BURST);
            return;
        }

        WrapperPlayClientClickWindow pkt = new WrapperPlayClientClickWindow(ev);
        ItemStack stack = pkt.getCarriedItemStack();
        if (stack == null) return;

        NBTCompound tag = stack.getNBT();
        int nbtBytes = tag == null ? 0
                : tag.toString().getBytes(StandardCharsets.UTF_8).length;

        boolean beeBomb = false;
        if (tag != null) {
            NBTCompound block = tag.getCompoundTagOrNull("BlockEntityTag");
            if (block != null) {
                NBTList<NBTCompound> bees = block.getCompoundListTagOrNull("Bees");
                beeBomb = bees != null && bees.getTags().size() > MAX_BEES;
            }
        }

        if (nbtBytes >= HARD_BYTES || beeBomb) {
            flag(player, ev, "nbt="+nbtBytes+" bees="+beeBomb);
            return;
        }

        if (nbtBytes >= SOFT_BYTES) {
            LongAdder strikes = STRIKES.getIfPresent(uid);
            if (strikes == null) { strikes = new LongAdder(); STRIKES.put(uid, strikes); }
            strikes.increment();
            flag(player, ev,
                    "nbt="+nbtBytes+" ["+strikes.intValue()+"/"+MAX_STRIKES+']');
        }
    }

    private void flag(Player p, PacketReceiveEvent e, String detail) {
        TaskUtil.flag(p, name(), detail);
        e.setCancelled(true);
    }
}
